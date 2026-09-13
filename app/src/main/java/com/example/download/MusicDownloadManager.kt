package com.example.download

import android.content.Context
import android.util.Log
import com.example.data.model.Song
import com.example.data.repository.MusicRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

sealed class DownloadStatus {
    object Idle : DownloadStatus()
    data class Progress(val songId: String, val progress: Float, val bytesDownloaded: Long, val totalBytes: Long) : DownloadStatus()
    data class Completed(val songId: String, val filePath: String) : DownloadStatus()
    data class Paused(val songId: String, val progress: Float) : DownloadStatus()
    data class Error(val songId: String, val message: String) : DownloadStatus()
}

class MusicDownloadManager(
    private val context: Context,
    private val repository: MusicRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val downloadJobs = mutableMapOf<String, Job>()
    private val _downloadStatuses = MutableStateFlow<Map<String, DownloadStatus>>(emptyMap())
    val downloadStatuses: StateFlow<Map<String, DownloadStatus>> = _downloadStatuses.asStateFlow()

    private val downloadsDir: File by lazy {
        File(context.filesDir, "music_downloads").apply {
            if (!exists()) mkdirs()
        }
    }

    fun startDownload(song: Song) {
        if (!song.isDownloadPermitted) {
            updateStatus(song.id, DownloadStatus.Error(song.id, "Download not permitted for this track license."))
            return
        }

        if (downloadJobs[song.id]?.isActive == true) return

        val job = scope.launch {
            try {
                updateStatus(song.id, DownloadStatus.Progress(song.id, 0f, 0, 0))
                val targetFile = File(downloadsDir, "${song.id}.mp3")

                var downloadedBytes = 0L
                if (targetFile.exists()) {
                    downloadedBytes = targetFile.length()
                }

                val url = URL(song.audioUrl)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 15000
                    if (downloadedBytes > 0) {
                        setRequestProperty("Range", "bytes=$downloadedBytes-")
                    }
                }
                connection.connect()

                val responseCode = connection.responseCode
                val isPartial = responseCode == HttpURLConnection.HTTP_PARTIAL
                val isOk = responseCode == HttpURLConnection.HTTP_OK

                if (!isOk && !isPartial) {
                    updateStatus(song.id, DownloadStatus.Error(song.id, "HTTP Error: $responseCode"))
                    return@launch
                }

                val contentLength = connection.contentLength.toLong()
                val totalBytes = if (isPartial) downloadedBytes + contentLength else contentLength

                val append = isPartial && downloadedBytes > 0
                val outputStream = FileOutputStream(targetFile, append)
                val inputStream: InputStream = connection.inputStream

                val buffer = ByteArray(8192)
                var bytesRead: Int
                var currentBytes = if (append) downloadedBytes else 0L

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    if (!isActive) {
                        outputStream.close()
                        inputStream.close()
                        return@launch
                    }
                    outputStream.write(buffer, 0, bytesRead)
                    currentBytes += bytesRead

                    val progress = if (totalBytes > 0) {
                        (currentBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                    } else 0.5f

                    updateStatus(song.id, DownloadStatus.Progress(song.id, progress, currentBytes, totalBytes))
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                // Mark completed in repository
                repository.updateDownloadStatus(song, isDownloaded = true, localPath = targetFile.absolutePath)
                updateStatus(song.id, DownloadStatus.Completed(song.id, targetFile.absolutePath))
            } catch (e: Exception) {
                Log.e("DownloadManager", "Error downloading song ${song.id}", e)
                updateStatus(song.id, DownloadStatus.Error(song.id, e.localizedMessage ?: "Download failed"))
            } finally {
                downloadJobs.remove(song.id)
            }
        }

        downloadJobs[song.id] = job
    }

    fun pauseDownload(songId: String) {
        val current = _downloadStatuses.value[songId]
        downloadJobs[songId]?.cancel()
        downloadJobs.remove(songId)
        if (current is DownloadStatus.Progress) {
            updateStatus(songId, DownloadStatus.Paused(songId, current.progress))
        }
    }

    fun cancelDownload(songId: String) {
        downloadJobs[songId]?.cancel()
        downloadJobs.remove(songId)
        val file = File(downloadsDir, "$songId.mp3")
        if (file.exists()) file.delete()
        updateStatus(songId, DownloadStatus.Idle)
    }

    suspend fun deleteDownload(song: Song) = withContext(Dispatchers.IO) {
        cancelDownload(song.id)
        val file = song.localFilePath?.let { File(it) } ?: File(downloadsDir, "${song.id}.mp3")
        if (file.exists()) {
            file.delete()
        }
        repository.updateDownloadStatus(song, isDownloaded = false, localPath = null)
        updateStatus(song.id, DownloadStatus.Idle)
    }

    fun getUsedStorageMb(): Double {
        val files = downloadsDir.listFiles() ?: return 0.0
        val totalBytes = files.sumOf { it.length() }
        return "%.2f".format(totalBytes.toDouble() / (1024 * 1024)).toDouble()
    }

    suspend fun clearAllDownloads() = withContext(Dispatchers.IO) {
        downloadJobs.values.forEach { it.cancel() }
        downloadJobs.clear()
        downloadsDir.listFiles()?.forEach { it.delete() }
        val downloadedSongs = repository.getDownloadedSongs()
        // will update all
        _downloadStatuses.value = emptyMap()
    }

    private fun updateStatus(songId: String, status: DownloadStatus) {
        _downloadStatuses.value = _downloadStatuses.value.toMutableMap().apply {
            put(songId, status)
        }
    }
}
