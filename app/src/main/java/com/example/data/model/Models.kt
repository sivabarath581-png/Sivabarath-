package com.example.data.model

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val artworkUrl: String,
    val audioUrl: String,
    val durationSeconds: Long,
    val genre: String = "Pop",
    val license: String = "Creative Commons Legal Audio",
    val isDownloadPermitted: Boolean = true,
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val localFilePath: String? = null,
    val downloadSizeMb: Double = 3.8
) {
    val durationFormatted: String
        get() {
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
}

data class Artist(
    val id: String,
    val name: String,
    val imageUrl: String,
    val bio: String,
    val monthlyListeners: String = "1.2M",
    val genre: String = "Indie Pop"
)

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val artworkUrl: String,
    val year: String = "2024",
    val trackCount: Int = 10,
    val genre: String = "Electronic"
)

data class Playlist(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val artworkUrl: String = "",
    val songCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

enum class AudioQuality(val label: String, val bitRate: String) {
    NORMAL("Normal", "128 kbps"),
    HIGH("High", "256 kbps"),
    LOSSLESS("Lossless HD", "320 kbps")
}

enum class AppThemeSetting(val label: String) {
    DARK("Pure Obsidian Dark"),
    LIGHT("Clean Light"),
    SYSTEM("System Default")
}
