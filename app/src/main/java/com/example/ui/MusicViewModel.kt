package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Album
import com.example.data.model.AppThemeSetting
import com.example.data.model.Artist
import com.example.data.model.AudioQuality
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.data.repository.MusicRepository
import com.example.download.DownloadStatus
import com.example.download.MusicDownloadManager
import com.example.player.PlayerManager
import com.example.player.PlayerRepeatMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SearchFilter(val label: String) {
    ALL("All"),
    SONGS("Songs"),
    ARTISTS("Artists"),
    ALBUMS("Albums")
}

class MusicViewModel(
    private val repository: MusicRepository,
    private val downloadManager: MusicDownloadManager,
    private val playerManager: PlayerManager
) : ViewModel() {

    // Theme & Settings
    val themeSetting = MutableStateFlow(AppThemeSetting.DARK)
    val audioQuality = MutableStateFlow(AudioQuality.HIGH)
    val wifiOnlyDownloads = MutableStateFlow(false)
    val notificationsEnabled = MutableStateFlow(true)
    val visualizerEnabled = MutableStateFlow(true)
    val customApiKey = MutableStateFlow("")

    // Home feeds
    val trendingSongs: StateFlow<List<Song>> = repository.getTrendingMusic()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recommendedSongs: StateFlow<List<Song>> = repository.getRecommendedMusic()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val popularArtists: StateFlow<List<Artist>> = repository.getPopularArtists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val popularAlbums: StateFlow<List<Album>> = repository.getPopularAlbums()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<Playlist>> = repository.getPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<Song>> = repository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayed: StateFlow<List<Song>> = repository.getRecentlyPlayed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadedSongs: StateFlow<List<Song>> = repository.getDownloadedSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search state
    val searchQuery = MutableStateFlow("")
    val searchFilter = MutableStateFlow(SearchFilter.ALL)
    val searchSuggestions = MutableStateFlow(
        listOf("Midnight Horizon", "Aurora Echoes", "Synthwave", "Chillwave", "Lofi Beats", "Cybernetic", "Acoustic", "Soundscape")
    )
    val isSearching = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val songSearchResults: StateFlow<List<Song>> = searchQuery
        .debounce { query -> if (query.isBlank()) 0L else 300L }
        .onEach { query -> if (query.isNotBlank()) isSearching.value = true }
        .flatMapLatest { query ->
            if (query.isBlank()) {
                isSearching.value = false
                flowOf(emptyList())
            } else {
                repository.searchMusic(query)
                    .onEach { isSearching.value = false }
                    .catch {
                        isSearching.value = false
                        emit(emptyList())
                    }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun clearSearchQuery() {
        searchQuery.value = ""
        isSearching.value = false
    }

    fun searchTracks(query: String) {
        searchQuery.value = query
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val artistSearchResults: StateFlow<List<Artist>> = searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList()) else repository.searchArtists(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val albumSearchResults: StateFlow<List<Album>> = searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList()) else repository.searchAlbums(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selection for Detail screens
    val selectedSong = MutableStateFlow<Song?>(null)
    val selectedArtist = MutableStateFlow<Artist?>(null)
    val selectedAlbum = MutableStateFlow<Album?>(null)
    val selectedPlaylist = MutableStateFlow<Playlist?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedArtistSongs: StateFlow<List<Song>> = selectedArtist
        .flatMapLatest { artist ->
            if (artist == null) flowOf(emptyList()) else repository.getArtistSongs(artist.name)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedAlbumSongs: StateFlow<List<Song>> = selectedAlbum
        .flatMapLatest { album ->
            if (album == null) flowOf(emptyList()) else repository.getAlbumSongs(album.title)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedPlaylistSongs: StateFlow<List<Song>> = selectedPlaylist
        .flatMapLatest { pl ->
            if (pl == null) flowOf(emptyList()) else repository.getPlaylistSongs(pl.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Player state delegated from PlayerManager
    val currentSong: StateFlow<Song?> = playerManager.currentSong
    val isPlaying: StateFlow<Boolean> = playerManager.isPlaying
    val isBuffering: StateFlow<Boolean> = playerManager.isBuffering
    val playbackPosition: StateFlow<Long> = playerManager.playbackPosition
    val duration: StateFlow<Long> = playerManager.duration
    val isShuffleEnabled: StateFlow<Boolean> = playerManager.isShuffleEnabled
    val repeatMode: StateFlow<PlayerRepeatMode> = playerManager.repeatMode
    val queue: StateFlow<List<Song>> = playerManager.queue
    val currentIndex: StateFlow<Int> = playerManager.currentIndex
    val visualizerAmplitudes: StateFlow<List<Float>> = playerManager.visualizerAmplitudes

    // Downloads
    val downloadStatuses: StateFlow<Map<String, DownloadStatus>> = downloadManager.downloadStatuses

    // Playback actions
    fun playSong(song: Song, customQueue: List<Song> = emptyList()) {
        playerManager.playSong(song, customQueue)
    }

    fun playAll(songs: List<Song>, shuffle: Boolean = false) {
        if (songs.isEmpty()) return
        val list = if (shuffle) songs.shuffled() else songs
        playerManager.playSong(list.first(), list)
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
    }

    fun skipToNext() {
        playerManager.skipToNext()
    }

    fun skipToPrevious() {
        playerManager.skipToPrevious()
    }

    fun toggleShuffle() {
        playerManager.toggleShuffle()
    }

    fun toggleRepeat() {
        playerManager.toggleRepeat()
    }

    fun stopPlayback() {
        playerManager.stop()
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            repository.toggleFavorite(song)
        }
    }

    // Playlist actions
    fun createPlaylist(name: String, description: String = "") {
        viewModelScope.launch {
            repository.createPlaylist(name, description)
        }
    }

    fun renamePlaylist(id: Long, newName: String) {
        viewModelScope.launch {
            repository.renamePlaylist(id, newName)
            if (selectedPlaylist.value?.id == id) {
                selectedPlaylist.value = selectedPlaylist.value?.copy(name = newName)
            }
        }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(id)
            if (selectedPlaylist.value?.id == id) {
                selectedPlaylist.value = null
            }
        }
    }

    fun addSongToPlaylist(playlistId: Long, song: Song) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, song)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: String) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    // Download actions
    fun startDownload(song: Song) {
        downloadManager.startDownload(song)
    }

    fun pauseDownload(songId: String) {
        downloadManager.pauseDownload(songId)
    }

    fun cancelDownload(songId: String) {
        downloadManager.cancelDownload(songId)
    }

    fun deleteDownload(song: Song) {
        viewModelScope.launch {
            downloadManager.deleteDownload(song)
        }
    }

    fun clearAllDownloads() {
        viewModelScope.launch {
            downloadManager.clearAllDownloads()
        }
    }

    fun getUsedStorageMb(): Double {
        return downloadManager.getUsedStorageMb()
    }

    fun clearPlaybackHistory() {
        viewModelScope.launch {
            repository.clearRecentlyPlayed()
        }
    }

    class Factory(
        private val repository: MusicRepository,
        private val downloadManager: MusicDownloadManager,
        private val playerManager: PlayerManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
                return MusicViewModel(repository, downloadManager, playerManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
