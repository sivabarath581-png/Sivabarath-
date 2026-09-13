package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MusicApiService {
    @GET("tracks/")
    suspend fun searchTracks(
        @Query("client_id") clientId: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 20,
        @Query("namesearch") query: String,
        @Query("include") include: String = "musicinfo",
        @Query("audioformat") audioFormat: String = "mp32"
    ): Response<JamendoTrackResponse>

    @GET("tracks/")
    suspend fun getPopularTracks(
        @Query("client_id") clientId: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 25,
        @Query("boost") boost: String = "popularity_month",
        @Query("include") include: String = "musicinfo",
        @Query("audioformat") audioFormat: String = "mp32"
    ): Response<JamendoTrackResponse>
}

@JsonClass(generateAdapter = true)
data class JamendoTrackResponse(
    @Json(name = "headers") val headers: JamendoHeaders?,
    @Json(name = "results") val results: List<JamendoTrack>?
)

@JsonClass(generateAdapter = true)
data class JamendoHeaders(
    @Json(name = "status") val status: String?,
    @Json(name = "code") val code: Int?,
    @Json(name = "error_message") val errorMessage: String?
)

@JsonClass(generateAdapter = true)
data class JamendoTrack(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "duration") val duration: Long,
    @Json(name = "artist_id") val artistId: String?,
    @Json(name = "artist_name") val artistName: String,
    @Json(name = "album_name") val albumName: String?,
    @Json(name = "album_id") val albumId: String?,
    @Json(name = "album_image") val albumImage: String?,
    @Json(name = "image") val image: String?,
    @Json(name = "audio") val audio: String?,
    @Json(name = "audiodownload") val audioDownload: String?,
    @Json(name = "audiodownload_allowed") val audioDownloadAllowed: Boolean?
)
