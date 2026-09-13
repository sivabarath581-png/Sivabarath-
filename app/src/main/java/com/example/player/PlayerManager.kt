package com.example.player

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.sin
import kotlin.random.Random

enum class PlayerRepeatMode {
    OFF, ALL, ONE
}

class PlayerManager private constructor(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null

    val exoPlayer: ExoPlayer by lazy {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build().apply {
                addListener(playerListener)
            }
    }

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _playbackPosition = MutableStateFlow(0L)
    val playbackPosition: StateFlow<Long> = _playbackPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(PlayerRepeatMode.OFF)
    val repeatMode: StateFlow<PlayerRepeatMode> = _repeatMode.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    // 16-band animated audio visualizer amplitudes (0f to 1f)
    private val _visualizerAmplitudes = MutableStateFlow(List(16) { 0.1f })
    val visualizerAmplitudes: StateFlow<List<Float>> = _visualizerAmplitudes.asStateFlow()

    private var onSongPlayedListener: ((Song) -> Unit)? = null

    fun setOnSongPlayedListener(listener: (Song) -> Unit) {
        onSongPlayedListener = listener
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
            if (isPlaying) {
                startProgressTracker()
            } else {
                stopProgressTracker()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    _isBuffering.value = true
                }
                Player.STATE_READY -> {
                    _isBuffering.value = false
                    _duration.value = exoPlayer.duration.coerceAtLeast(0L)
                }
                Player.STATE_ENDED -> {
                    _isBuffering.value = false
                    handleTrackEnded()
                }
                Player.STATE_IDLE -> {
                    _isBuffering.value = false
                }
            }
        }
    }

    fun playSong(song: Song, newQueue: List<Song> = emptyList()) {
        val activeQueue = if (newQueue.isNotEmpty()) newQueue else if (_queue.value.isEmpty()) listOf(song) else _queue.value
        val songIndex = activeQueue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)

        _queue.value = activeQueue
        _currentIndex.value = songIndex
        _currentSong.value = song

        // Check if downloaded file exists
        val playbackUri = if (song.isDownloaded && song.localFilePath != null && File(song.localFilePath).exists()) {
            Uri.fromFile(File(song.localFilePath))
        } else {
            Uri.parse(song.audioUrl)
        }

        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .setAlbumTitle(song.album)
            .setArtworkUri(Uri.parse(song.artworkUrl))
            .build()

        val mediaItem = MediaItem.Builder()
            .setMediaId(song.id)
            .setUri(playbackUri)
            .setMediaMetadata(mediaMetadata)
            .build()

        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()

        _playbackPosition.value = 0L
        _duration.value = song.durationSeconds * 1000L

        onSongPlayedListener?.invoke(song)
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            if (exoPlayer.playbackState == Player.STATE_ENDED) {
                exoPlayer.seekTo(0)
            }
            exoPlayer.play()
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        _playbackPosition.value = positionMs
    }

    fun skipToNext() {
        val q = _queue.value
        if (q.isEmpty()) return

        val nextIndex = if (_isShuffleEnabled.value) {
            Random.nextInt(q.size)
        } else {
            (_currentIndex.value + 1) % q.size
        }
        playSong(q[nextIndex], q)
    }

    fun skipToPrevious() {
        val q = _queue.value
        if (q.isEmpty()) return

        if (exoPlayer.currentPosition > 3000L) {
            seekTo(0)
            return
        }

        val prevIndex = if (_currentIndex.value - 1 < 0) q.size - 1 else _currentIndex.value - 1
        playSong(q[prevIndex], q)
    }

    fun toggleShuffle() {
        _isShuffleEnabled.value = !_isShuffleEnabled.value
    }

    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            PlayerRepeatMode.OFF -> PlayerRepeatMode.ALL
            PlayerRepeatMode.ALL -> PlayerRepeatMode.ONE
            PlayerRepeatMode.ONE -> PlayerRepeatMode.OFF
        }
    }

    fun stop() {
        exoPlayer.stop()
        _isPlaying.value = false
        _currentSong.value = null
        stopProgressTracker()
    }

    private fun handleTrackEnded() {
        when (_repeatMode.value) {
            PlayerRepeatMode.ONE -> {
                exoPlayer.seekTo(0)
                exoPlayer.play()
            }
            PlayerRepeatMode.ALL -> {
                skipToNext()
            }
            PlayerRepeatMode.OFF -> {
                if (_currentIndex.value < _queue.value.size - 1) {
                    skipToNext()
                } else {
                    _isPlaying.value = false
                }
            }
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            var step = 0
            while (isActive) {
                val currentPos = exoPlayer.currentPosition.coerceAtLeast(0L)
                _playbackPosition.value = currentPos
                val dur = exoPlayer.duration
                if (dur > 0) {
                    _duration.value = dur
                }

                // Beat-reactive rhythmic visualizer amplitudes based on playback
                if (_isPlaying.value) {
                    step++
                    val amplitudes = List(16) { i ->
                        val wave = sin((step * 0.2) + (i * 0.4)).toFloat()
                        val noise = (Random.nextFloat() * 0.35f)
                        val bassBoost = if (i < 4) 0.25f else 0f
                        ((wave * 0.4f + 0.5f + noise + bassBoost) * 0.9f).coerceIn(0.08f, 0.98f)
                    }
                    _visualizerAmplitudes.value = amplitudes
                } else {
                    _visualizerAmplitudes.value = List(16) { 0.08f }
                }

                delay(80)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
        _visualizerAmplitudes.value = List(16) { 0.08f }
    }

    companion object {
        @Volatile
        private var INSTANCE: PlayerManager? = null

        fun getInstance(context: Context): PlayerManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PlayerManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
