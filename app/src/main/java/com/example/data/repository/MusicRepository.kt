package com.example.data.repository

import com.example.data.model.Album
import com.example.data.model.Artist
import com.example.data.model.Playlist
import com.example.data.model.Song
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    fun getTrendingMusic(): Flow<List<Song>>
    fun getRecommendedMusic(): Flow<List<Song>>
    fun getPopularArtists(): Flow<List<Artist>>
    fun getPopularAlbums(): Flow<List<Album>>
    fun searchMusic(query: String): Flow<List<Song>>
    fun searchArtists(query: String): Flow<List<Artist>>
    fun searchAlbums(query: String): Flow<List<Album>>
    fun getSongById(songId: String): Flow<Song?>
    fun getArtistById(artistId: String): Flow<Artist?>
    fun getAlbumById(albumId: String): Flow<Album?>
    fun getArtistSongs(artistName: String): Flow<List<Song>>
    fun getAlbumSongs(albumTitle: String): Flow<List<Song>>

    fun getFavorites(): Flow<List<Song>>
    suspend fun toggleFavorite(song: Song): Boolean

    fun getRecentlyPlayed(): Flow<List<Song>>
    suspend fun recordRecentlyPlayed(song: Song)
    suspend fun clearRecentlyPlayed()

    fun getPlaylists(): Flow<List<Playlist>>
    suspend fun createPlaylist(name: String, description: String = ""): Long
    suspend fun renamePlaylist(playlistId: Long, newName: String)
    suspend fun deletePlaylist(playlistId: Long)
    fun getPlaylistSongs(playlistId: Long): Flow<List<Song>>
    suspend fun addSongToPlaylist(playlistId: Long, song: Song)
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: String)

    fun getDownloadedSongs(): Flow<List<Song>>
    suspend fun updateDownloadStatus(song: Song, isDownloaded: Boolean, localPath: String?)

    suspend fun initializeSeedData()
}
