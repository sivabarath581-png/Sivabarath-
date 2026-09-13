package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.Song

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val artworkUrl: String,
    val audioUrl: String,
    val durationSeconds: Long,
    val genre: String,
    val license: String,
    val isDownloadPermitted: Boolean,
    val isFavorite: Boolean,
    val isDownloaded: Boolean,
    val localFilePath: String?,
    val downloadSizeMb: Double,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toSong(): Song = Song(
        id = id,
        title = title,
        artist = artist,
        album = album,
        artworkUrl = artworkUrl,
        audioUrl = audioUrl,
        durationSeconds = durationSeconds,
        genre = genre,
        license = license,
        isDownloadPermitted = isDownloadPermitted,
        isFavorite = isFavorite,
        isDownloaded = isDownloaded,
        localFilePath = localFilePath,
        downloadSizeMb = downloadSizeMb
    )

    companion object {
        fun fromSong(song: Song): SongEntity = SongEntity(
            id = song.id,
            title = song.title,
            artist = song.artist,
            album = song.album,
            artworkUrl = song.artworkUrl,
            audioUrl = song.audioUrl,
            durationSeconds = song.durationSeconds,
            genre = song.genre,
            license = song.license,
            isDownloadPermitted = song.isDownloadPermitted,
            isFavorite = song.isFavorite,
            isDownloaded = song.isDownloaded,
            localFilePath = song.localFilePath,
            downloadSizeMb = song.downloadSizeMb
        )
    }
}

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val artworkUrl: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "playlist_song_cross_ref",
    primaryKeys = ["playlistId", "songId"],
    indices = [Index("songId")]
)
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val songId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "recently_played",
    indices = [Index("timestamp")]
)
data class RecentlyPlayedEntity(
    @PrimaryKey val songId: String,
    val timestamp: Long = System.currentTimeMillis()
)
