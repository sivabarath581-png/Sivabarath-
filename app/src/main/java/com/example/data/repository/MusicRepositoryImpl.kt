package com.example.data.repository

import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.PlaylistEntity
import com.example.data.local.PlaylistSongCrossRef
import com.example.data.local.RecentlyPlayedEntity
import com.example.data.local.SongEntity
import com.example.data.model.Album
import com.example.data.model.Artist
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.data.remote.LegalMusicCatalog
import com.example.data.remote.MusicApiService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MusicRepositoryImpl(
    private val database: AppDatabase,
    private val apiService: MusicApiService,
    private val apiKey: String = "",
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : MusicRepository {

    private val songDao = database.songDao()
    private val playlistDao = database.playlistDao()
    private val recentlyPlayedDao = database.recentlyPlayedDao()

    override suspend fun initializeSeedData(): Unit = withContext(ioDispatcher) {
        try {
            // Seed base songs if local DB is empty
            val initialSongs = LegalMusicCatalog.demoSongs
            songDao.insertOrUpdateSongs(initialSongs.map { SongEntity.fromSong(it) })

            // Check if playlists exist, if not create default ones
            val defaultPlaylists = listOf(
                PlaylistEntity(name = "Sivabarath Top Hits", description = "Finest high-energy beats and chill vibes", artworkUrl = initialSongs[0].artworkUrl),
                PlaylistEntity(name = "Focus & Deep Work", description = "Atmospheric synthwave and soothing melodies", artworkUrl = initialSongs[1].artworkUrl),
                PlaylistEntity(name = "Late Night Reverie", description = "Calming ambient soundscapes for relaxation", artworkUrl = initialSongs[2].artworkUrl)
            )

            for (p in defaultPlaylists) {
                val playlistId = playlistDao.insertPlaylist(p)
                // Add some initial songs to this playlist
                initialSongs.take(3).forEach { song ->
                    playlistDao.addSongToPlaylist(PlaylistSongCrossRef(playlistId = playlistId, songId = song.id))
                }
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Error seeding initial data", e)
        }
        Unit
    }

    override fun getTrendingMusic(): Flow<List<Song>> = flow {
        // Try fetching online if API key is provided, else use catalog
        val songs = if (apiKey.isNotBlank()) {
            try {
                val response = apiService.getPopularTracks(clientId = apiKey)
                if (response.isSuccessful && response.body()?.results?.isNotEmpty() == true) {
                    response.body()!!.results!!.map { item ->
                        Song(
                            id = item.id,
                            title = item.name,
                            artist = item.artistName,
                            album = item.albumName ?: "Single",
                            artworkUrl = item.image ?: item.albumImage ?: LegalMusicCatalog.demoSongs[0].artworkUrl,
                            audioUrl = item.audio ?: "",
                            durationSeconds = item.duration,
                            genre = "Pop",
                            license = "Jamendo / Legal Online Audio",
                            isDownloadPermitted = item.audioDownloadAllowed ?: true
                        )
                    }
                } else {
                    LegalMusicCatalog.demoSongs
                }
            } catch (e: Exception) {
                LegalMusicCatalog.demoSongs
            }
        } else {
            LegalMusicCatalog.demoSongs
        }

        // Merge with local DB for favorite/downloaded flags
        val enriched = songs.map { song ->
            val local = songDao.getSongByIdDirect(song.id)
            if (local != null) {
                song.copy(
                    isFavorite = local.isFavorite,
                    isDownloaded = local.isDownloaded,
                    localFilePath = local.localFilePath
                )
            } else {
                songDao.insertOrUpdateSong(SongEntity.fromSong(song))
                song
            }
        }
        emit(enriched)
    }.flowOn(ioDispatcher)

    override fun getRecommendedMusic(): Flow<List<Song>> = flow {
        // Return curated recommended songs
        val base = LegalMusicCatalog.demoSongs.shuffled()
        val enriched = base.map { song ->
            val local = songDao.getSongByIdDirect(song.id)
            if (local != null) {
                song.copy(
                    isFavorite = local.isFavorite,
                    isDownloaded = local.isDownloaded,
                    localFilePath = local.localFilePath
                )
            } else song
        }
        emit(enriched)
    }.flowOn(ioDispatcher)

    override fun getPopularArtists(): Flow<List<Artist>> = flow {
        emit(LegalMusicCatalog.demoArtists)
    }.flowOn(ioDispatcher)

    override fun getPopularAlbums(): Flow<List<Album>> = flow {
        emit(LegalMusicCatalog.demoAlbums)
    }.flowOn(ioDispatcher)

    override fun searchMusic(query: String): Flow<List<Song>> = flow {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            emit(emptyList())
            return@flow
        }

        var results: List<Song> = emptyList()

        val effectiveApiKey = if (apiKey.isNotBlank()) apiKey else "56d30c95"
        try {
            val response = apiService.searchTracks(clientId = effectiveApiKey, query = trimmed)
            if (response.isSuccessful && response.body()?.results?.isNotEmpty() == true) {
                results = response.body()!!.results!!.map { item ->
                    Song(
                        id = item.id,
                        title = item.name,
                        artist = item.artistName,
                        album = item.albumName ?: "Single",
                        artworkUrl = item.image ?: item.albumImage ?: LegalMusicCatalog.demoSongs[0].artworkUrl,
                        audioUrl = item.audio ?: "",
                        durationSeconds = item.duration,
                        genre = "Indie",
                        license = "Creative Commons / Jamendo",
                        isDownloadPermitted = item.audioDownloadAllowed ?: true
                    )
                }
            }
        } catch (e: Exception) {
            Log.w("MusicRepository", "Retrofit API search failed or offline, falling back to catalog", e)
        }

        // If no online results or no key, search local catalog
        if (results.isEmpty()) {
            results = LegalMusicCatalog.demoSongs.filter {
                it.title.contains(trimmed, ignoreCase = true) ||
                        it.artist.contains(trimmed, ignoreCase = true) ||
                        it.album.contains(trimmed, ignoreCase = true) ||
                        it.genre.contains(trimmed, ignoreCase = true)
            }
        }

        // Enrich with local state
        val enriched = results.map { song ->
            val local = songDao.getSongByIdDirect(song.id)
            if (local != null) {
                song.copy(
                    isFavorite = local.isFavorite,
                    isDownloaded = local.isDownloaded,
                    localFilePath = local.localFilePath
                )
            } else {
                songDao.insertOrUpdateSong(SongEntity.fromSong(song))
                song
            }
        }
        emit(enriched)
    }.flowOn(ioDispatcher)

    override fun searchArtists(query: String): Flow<List<Artist>> = flow {
        val trimmed = query.trim()
        val list = if (trimmed.isEmpty()) {
            LegalMusicCatalog.demoArtists
        } else {
            LegalMusicCatalog.demoArtists.filter {
                it.name.contains(trimmed, ignoreCase = true) || it.genre.contains(trimmed, ignoreCase = true)
            }
        }
        emit(list)
    }.flowOn(ioDispatcher)

    override fun searchAlbums(query: String): Flow<List<Album>> = flow {
        val trimmed = query.trim()
        val list = if (trimmed.isEmpty()) {
            LegalMusicCatalog.demoAlbums
        } else {
            LegalMusicCatalog.demoAlbums.filter {
                it.title.contains(trimmed, ignoreCase = true) || it.artist.contains(trimmed, ignoreCase = true)
            }
        }
        emit(list)
    }.flowOn(ioDispatcher)

    override fun getSongById(songId: String): Flow<Song?> = flow {
        val local = songDao.getSongByIdDirect(songId)
        if (local != null) {
            emit(local.toSong())
        } else {
            val catalog = LegalMusicCatalog.demoSongs.find { it.id == songId }
            emit(catalog)
        }
    }.flowOn(ioDispatcher)

    override fun getArtistById(artistId: String): Flow<Artist?> = flow {
        emit(LegalMusicCatalog.demoArtists.find { it.id == artistId })
    }.flowOn(ioDispatcher)

    override fun getAlbumById(albumId: String): Flow<Album?> = flow {
        emit(LegalMusicCatalog.demoAlbums.find { it.id == albumId })
    }.flowOn(ioDispatcher)

    override fun getArtistSongs(artistName: String): Flow<List<Song>> = flow {
        val songs = LegalMusicCatalog.demoSongs.filter { it.artist.equals(artistName, ignoreCase = true) }
        val enriched = songs.map { s ->
            val local = songDao.getSongByIdDirect(s.id)
            if (local != null) s.copy(isFavorite = local.isFavorite, isDownloaded = local.isDownloaded) else s
        }
        emit(enriched)
    }.flowOn(ioDispatcher)

    override fun getAlbumSongs(albumTitle: String): Flow<List<Song>> = flow {
        val songs = LegalMusicCatalog.demoSongs.filter { it.album.equals(albumTitle, ignoreCase = true) }
        val enriched = songs.map { s ->
            val local = songDao.getSongByIdDirect(s.id)
            if (local != null) s.copy(isFavorite = local.isFavorite, isDownloaded = local.isDownloaded) else s
        }
        emit(enriched)
    }.flowOn(ioDispatcher)

    override fun getFavorites(): Flow<List<Song>> {
        return songDao.getFavoriteSongs().map { list -> list.map { it.toSong() } }
    }

    override suspend fun toggleFavorite(song: Song): Boolean = withContext(ioDispatcher) {
        val current = songDao.getSongByIdDirect(song.id)
        val newFav = if (current != null) !current.isFavorite else !song.isFavorite
        songDao.insertOrUpdateSong(SongEntity.fromSong(song.copy(isFavorite = newFav)))
        newFav
    }

    override fun getRecentlyPlayed(): Flow<List<Song>> {
        return recentlyPlayedDao.getRecentlyPlayedSongs().map { list -> list.map { it.toSong() } }
    }

    override suspend fun recordRecentlyPlayed(song: Song) = withContext(ioDispatcher) {
        songDao.insertOrUpdateSong(SongEntity.fromSong(song))
        recentlyPlayedDao.insertRecentlyPlayed(
            RecentlyPlayedEntity(songId = song.id, timestamp = System.currentTimeMillis())
        )
    }

    override suspend fun clearRecentlyPlayed() = withContext(ioDispatcher) {
        recentlyPlayedDao.clearAll()
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { entities ->
            entities.map { entity ->
                val count = playlistDao.getSongCountForPlaylist(entity.id)
                Playlist(
                    id = entity.id,
                    name = entity.name,
                    description = entity.description,
                    artworkUrl = entity.artworkUrl,
                    songCount = count,
                    createdAt = entity.createdAt
                )
            }
        }
    }

    override suspend fun createPlaylist(name: String, description: String): Long = withContext(ioDispatcher) {
        val artwork = LegalMusicCatalog.demoSongs.random().artworkUrl
        playlistDao.insertPlaylist(PlaylistEntity(name = name, description = description, artworkUrl = artwork))
    }

    override suspend fun renamePlaylist(playlistId: Long, newName: String) = withContext(ioDispatcher) {
        playlistDao.renamePlaylist(playlistId, newName)
    }

    override suspend fun deletePlaylist(playlistId: Long) = withContext(ioDispatcher) {
        playlistDao.removeAllSongsFromPlaylist(playlistId)
        playlistDao.deletePlaylist(playlistId)
    }

    override fun getPlaylistSongs(playlistId: Long): Flow<List<Song>> {
        return playlistDao.getSongsForPlaylist(playlistId).map { list -> list.map { it.toSong() } }
    }

    override suspend fun addSongToPlaylist(playlistId: Long, song: Song) = withContext(ioDispatcher) {
        songDao.insertOrUpdateSong(SongEntity.fromSong(song))
        playlistDao.addSongToPlaylist(PlaylistSongCrossRef(playlistId = playlistId, songId = song.id))
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: String) = withContext(ioDispatcher) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    override fun getDownloadedSongs(): Flow<List<Song>> {
        return songDao.getDownloadedSongs().map { list -> list.map { it.toSong() } }
    }

    override suspend fun updateDownloadStatus(
        song: Song,
        isDownloaded: Boolean,
        localPath: String?
    ) = withContext(ioDispatcher) {
        val updated = song.copy(isDownloaded = isDownloaded, localFilePath = localPath)
        songDao.insertOrUpdateSong(SongEntity.fromSong(updated))
    }
}
