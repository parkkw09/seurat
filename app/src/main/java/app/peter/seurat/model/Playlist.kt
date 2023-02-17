package app.peter.seurat.model

import com.google.gson.annotations.SerializedName

data class Playlist(
    val kind: String,
    val etag: String,
    val id: String,
    val snippet: PlaylistSnippet
)

data class PlaylistSnippet(
    val publishedAt: String,
    val channelId: String,
    val title: String,
    val description: String,
    val thumbnails: PlaylistThumbnails,
    val channelTitle: String,
    val localized: Localized
)

data class PlaylistThumbnails(
    @SerializedName("default") val default: DetailThumbnailsInfo,
    @SerializedName("medium") val medium: DetailThumbnailsInfo,
    @SerializedName("high") val high: DetailThumbnailsInfo,
    @SerializedName("standard") val standard: DetailThumbnailsInfo,
    @SerializedName("maxres") val maxres: DetailThumbnailsInfo
)

// status
data class PlaylistStatus(
    val privacyStatus: String
)