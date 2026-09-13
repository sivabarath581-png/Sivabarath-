package com.example

import com.example.data.local.SongEntity
import com.example.data.model.AppThemeSetting
import com.example.data.model.AudioQuality
import com.example.data.model.Song
import com.example.data.remote.LegalMusicCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MusicAppUnitTest {

    @Test
    fun legalMusicCatalog_containsDemoTracks() {
        val demoSongs = LegalMusicCatalog.demoSongs
        assertTrue("Catalog should contain demo songs", demoSongs.isNotEmpty())
        assertTrue("Catalog should contain at least 6 curated songs", demoSongs.size >= 6)

        demoSongs.forEach { song ->
            assertNotNull(song.id)
            assertNotNull(song.title)
            assertNotNull(song.artist)
            assertNotNull(song.audioUrl)
            assertTrue("Audio duration should be positive", song.durationSeconds > 0)
            assertTrue("Demo songs must be marked download permitted", song.isDownloadPermitted)
            assertTrue(
                "Demo song must have legal license",
                song.license.isNotBlank()
            )
        }
    }

    @Test
    fun song_durationFormatted_calculatesCorrectly() {
        val song = Song(
            id = "test_1",
            title = "Test Track",
            artist = "Test Artist",
            album = "Test Album",
            artworkUrl = "https://example.com/art.jpg",
            audioUrl = "https://example.com/audio.mp3",
            durationSeconds = 215 // 3 minutes 35 seconds
        )

        assertEquals("3:35", song.durationFormatted)

        val shortSong = song.copy(durationSeconds = 5)
        assertEquals("0:05", shortSong.durationFormatted)

        val exactMinuteSong = song.copy(durationSeconds = 120)
        assertEquals("2:00", exactMinuteSong.durationFormatted)
    }

    @Test
    fun songEntity_toAndFromSong_preservesData() {
        val song = Song(
            id = "s_100",
            title = "Cyber Horizon",
            artist = "Aura Synthetics",
            album = "Neon Drift",
            artworkUrl = "https://example.com/art.jpg",
            audioUrl = "https://example.com/audio.mp3",
            durationSeconds = 210,
            genre = "Synthwave",
            license = "Creative Commons BY-NC",
            isDownloadPermitted = true,
            isFavorite = true,
            isDownloaded = false,
            localFilePath = null,
            downloadSizeMb = 4.2
        )

        val entity = SongEntity.fromSong(song)
        val mappedBack = entity.toSong()

        assertEquals(song.id, mappedBack.id)
        assertEquals(song.title, mappedBack.title)
        assertEquals(song.artist, mappedBack.artist)
        assertEquals(song.album, mappedBack.album)
        assertEquals(song.artworkUrl, mappedBack.artworkUrl)
        assertEquals(song.audioUrl, mappedBack.audioUrl)
        assertEquals(song.durationSeconds, mappedBack.durationSeconds)
        assertEquals(song.genre, mappedBack.genre)
        assertEquals(song.license, mappedBack.license)
        assertEquals(song.isDownloadPermitted, mappedBack.isDownloadPermitted)
        assertEquals(song.isFavorite, mappedBack.isFavorite)
    }

    @Test
    fun audioQuality_attributes_areCorrect() {
        assertEquals("128 kbps", AudioQuality.NORMAL.bitRate)
        assertEquals("256 kbps", AudioQuality.HIGH.bitRate)
        assertEquals("320 kbps", AudioQuality.LOSSLESS.bitRate)
    }

    @Test
    fun appThemeSetting_values_areAvailable() {
        assertEquals(3, AppThemeSetting.values().size)
        assertTrue(AppThemeSetting.DARK.label.isNotEmpty())
        assertTrue(AppThemeSetting.LIGHT.label.isNotEmpty())
        assertTrue(AppThemeSetting.SYSTEM.label.isNotEmpty())
    }

    @Test
    fun searchFiltering_findsMatchesInCatalog() {
        val query = "Midnight"
        val matches = LegalMusicCatalog.demoSongs.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.artist.contains(query, ignoreCase = true) ||
                    it.album.contains(query, ignoreCase = true) ||
                    it.genre.contains(query, ignoreCase = true)
        }
        assertTrue("Search query 'Midnight' should find matching songs", matches.isNotEmpty())
        assertTrue(matches.any { it.title.contains("Midnight") })
    }

    @Test
    fun retrofitTrackMapping_convertsToSongSuccessfully() {
        val jamendoTrack = com.example.data.remote.JamendoTrack(
            id = "192837",
            name = "Starlight Symphony",
            duration = 195,
            artistId = "554",
            artistName = "Cosmic Wave",
            albumName = "Interstellar",
            albumId = "882",
            albumImage = "https://example.com/album.jpg",
            image = "https://example.com/track.jpg",
            audio = "https://example.com/stream.mp3",
            audioDownload = "https://example.com/download.mp3",
            audioDownloadAllowed = true
        )

        val song = Song(
            id = jamendoTrack.id,
            title = jamendoTrack.name,
            artist = jamendoTrack.artistName,
            album = jamendoTrack.albumName ?: "Single",
            artworkUrl = jamendoTrack.image ?: jamendoTrack.albumImage ?: "",
            audioUrl = jamendoTrack.audio ?: "",
            durationSeconds = jamendoTrack.duration,
            genre = "Indie",
            license = "Creative Commons / Jamendo",
            isDownloadPermitted = jamendoTrack.audioDownloadAllowed ?: true
        )

        assertEquals("192837", song.id)
        assertEquals("Starlight Symphony", song.title)
        assertEquals("Cosmic Wave", song.artist)
        assertEquals("Interstellar", song.album)
        assertEquals(195L, song.durationSeconds)
        assertTrue(song.isDownloadPermitted)
    }
}
