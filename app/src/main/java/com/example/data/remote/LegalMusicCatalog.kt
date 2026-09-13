package com.example.data.remote

import com.example.data.model.Album
import com.example.data.model.Artist
import com.example.data.model.Song

object LegalMusicCatalog {

    val demoArtists: List<Artist> = listOf(
        Artist(
            id = "art_1",
            name = "Sivabarath Waves",
            imageUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?auto=format&fit=crop&w=600&q=80",
            bio = "Ambient, synthwave and melodic soundscapes created for deep focus and uplifting rhythms.",
            monthlyListeners = "2.4M",
            genre = "Electronic & Chillwave"
        ),
        Artist(
            id = "art_2",
            name = "Aurora Echoes",
            imageUrl = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?auto=format&fit=crop&w=600&q=80",
            bio = "Ethereal cinematic vocals blended with modern neo-classical instrumentation.",
            monthlyListeners = "1.8M",
            genre = "Cinematic Neo-Classical"
        ),
        Artist(
            id = "art_3",
            name = "Starlight Syndicate",
            imageUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?auto=format&fit=crop&w=600&q=80",
            bio = "Groovy lofi, jazz-infused hip-hop beats, and late-night lounge melodies.",
            monthlyListeners = "3.1M",
            genre = "Lofi & Jazz Hop"
        ),
        Artist(
            id = "art_4",
            name = "Velvet Horizons",
            imageUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?auto=format&fit=crop&w=600&q=80",
            bio = "Warm acoustic guitars, indie folk stories, and sun-drenched harmonies.",
            monthlyListeners = "950K",
            genre = "Indie Acoustic"
        ),
        Artist(
            id = "art_5",
            name = "Cyberpulse",
            imageUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?auto=format&fit=crop&w=600&q=80",
            bio = "High-energy cyber techno and futuristic club grooves for late-night drives.",
            monthlyListeners = "1.5M",
            genre = "Cyber Electro"
        )
    )

    val demoAlbums: List<Album> = listOf(
        Album(
            id = "alb_1",
            title = "Midnight Odyssey",
            artist = "Sivabarath Waves",
            artworkUrl = "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?auto=format&fit=crop&w=600&q=80",
            year = "2024",
            trackCount = 4,
            genre = "Synthwave"
        ),
        Album(
            id = "alb_2",
            title = "Celestial Drift",
            artist = "Aurora Echoes",
            artworkUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=600&q=80",
            year = "2024",
            trackCount = 3,
            genre = "Cinematic"
        ),
        Album(
            id = "alb_3",
            title = "Coffee & Raindrops",
            artist = "Starlight Syndicate",
            artworkUrl = "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?auto=format&fit=crop&w=600&q=80",
            year = "2023",
            trackCount = 3,
            genre = "Lofi Hip-Hop"
        ),
        Album(
            id = "alb_4",
            title = "Neon Resonance",
            artist = "Cyberpulse",
            artworkUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&w=600&q=80",
            year = "2024",
            trackCount = 2,
            genre = "Electro"
        )
    )

    // Legal, open audio URLs (SoundHelix high-quality royalty-free licensed MP3s)
    val demoSongs: List<Song> = listOf(
        Song(
            id = "siva_track_1",
            title = "Midnight Horizon",
            artist = "Sivabarath Waves",
            album = "Midnight Odyssey",
            artworkUrl = "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?auto=format&fit=crop&w=600&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            durationSeconds = 372,
            genre = "Synthwave",
            license = "Creative Commons 3.0 (Legal Royalty-Free)",
            isDownloadPermitted = true,
            isFavorite = true,
            downloadSizeMb = 5.2
        ),
        Song(
            id = "siva_track_2",
            title = "Neon Dreamscape",
            artist = "Sivabarath Waves",
            album = "Midnight Odyssey",
            artworkUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?auto=format&fit=crop&w=600&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            durationSeconds = 423,
            genre = "Chillwave",
            license = "Creative Commons 3.0 (Legal Royalty-Free)",
            isDownloadPermitted = true,
            isFavorite = false,
            downloadSizeMb = 6.1
        ),
        Song(
            id = "siva_track_3",
            title = "Aurora Borealis Echoes",
            artist = "Aurora Echoes",
            album = "Celestial Drift",
            artworkUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=600&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            durationSeconds = 345,
            genre = "Cinematic",
            license = "Open Audio License (Royalty-Free)",
            isDownloadPermitted = true,
            isFavorite = true,
            downloadSizeMb = 4.8
        ),
        Song(
            id = "siva_track_4",
            title = "Coffee In The Rain",
            artist = "Starlight Syndicate",
            album = "Coffee & Raindrops",
            artworkUrl = "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?auto=format&fit=crop&w=600&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
            durationSeconds = 302,
            genre = "Lofi",
            license = "CC-BY 4.0 (Author Permitted)",
            isDownloadPermitted = true,
            isFavorite = false,
            downloadSizeMb = 4.1
        ),
        Song(
            id = "siva_track_5",
            title = "Sunset Boulevard Reverie",
            artist = "Velvet Horizons",
            album = "Golden Hour Sessions",
            artworkUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?auto=format&fit=crop&w=600&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
            durationSeconds = 351,
            genre = "Acoustic Folk",
            license = "Creative Commons Legal Audio",
            isDownloadPermitted = true,
            isFavorite = false,
            downloadSizeMb = 4.9
        ),
        Song(
            id = "siva_track_6",
            title = "Cybernetic Velocity",
            artist = "Cyberpulse",
            album = "Neon Resonance",
            artworkUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&w=600&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
            durationSeconds = 290,
            genre = "Electro",
            license = "Public Domain Dedication (CC0)",
            isDownloadPermitted = true,
            isFavorite = false,
            downloadSizeMb = 3.9
        ),
        Song(
            id = "siva_track_7",
            title = "Cosmic Sanctuary",
            artist = "Sivabarath Waves",
            album = "Midnight Odyssey",
            artworkUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?auto=format&fit=crop&w=600&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3",
            durationSeconds = 328,
            genre = "Ambient",
            license = "Creative Commons 3.0",
            isDownloadPermitted = true,
            isFavorite = false,
            downloadSizeMb = 4.5
        ),
        Song(
            id = "siva_track_8",
            title = "Luminescent Whispers",
            artist = "Aurora Echoes",
            album = "Celestial Drift",
            artworkUrl = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?auto=format&fit=crop&w=600&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
            durationSeconds = 310,
            genre = "Neo-Classical",
            license = "Royalty-Free Audio Commons",
            isDownloadPermitted = true,
            isFavorite = false,
            downloadSizeMb = 4.3
        )
    )
}
