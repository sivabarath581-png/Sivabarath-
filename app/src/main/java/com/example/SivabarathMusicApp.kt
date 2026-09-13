package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.data.local.AppDatabase
import com.example.data.remote.NetworkModule
import com.example.data.repository.MusicRepository
import com.example.data.repository.MusicRepositoryImpl
import com.example.download.MusicDownloadManager
import com.example.player.PlayerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SivabarathMusicApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: MusicRepository
        private set

    lateinit var downloadManager: MusicDownloadManager
        private set

    lateinit var playerManager: PlayerManager
        private set

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        instance = this

        createNotificationChannel()

        database = AppDatabase.getInstance(this)
        val apiService = NetworkModule.provideMusicApiService()

        // Read API key from BuildConfig if provided, default is empty (runs legal catalog)
        val apiKey = "" // Configurable via Settings or BuildConfig

        repository = MusicRepositoryImpl(
            database = database,
            apiService = apiService,
            apiKey = apiKey
        )

        downloadManager = MusicDownloadManager(
            context = this,
            repository = repository
        )

        playerManager = PlayerManager.getInstance(this).apply {
            setOnSongPlayedListener { song ->
                applicationScope.launch {
                    repository.recordRecentlyPlayed(song)
                }
            }
        }

        // Initialize seed data (legal royalty-free catalog & playlists)
        applicationScope.launch {
            repository.initializeSeedData()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sivabarath Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls and media notification for background playback"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "sivabarath_music_channel"
        lateinit var instance: SivabarathMusicApp
            private set
    }
}
