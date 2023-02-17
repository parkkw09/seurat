package app.peter.seurat.model

import com.google.gson.annotations.SerializedName

data class PlaylistItem(
    val kind: String,
    val etag: String,
    val id: String,
    val snippet: PlaylistItemSnippet
)

data class PlaylistItemSnippet(
    val publishedAt: String,
    val channelId: String,
    val title: String,
    val description: String,
    val thumbnails: PlaylistItemThumbnails,
    val channelTitle: String,
    val playlistId: String,
    val position: Int,
    val resourceId: PlaylistItemResourceIdentity,
    val videoOwnerChannelTitle: String,
    val videoOwnerChannelId: String
)

data class PlaylistItemThumbnails(
    @SerializedName("default") val default: DetailThumbnailsInfo,
    @SerializedName("medium") val medium: DetailThumbnailsInfo,
    @SerializedName("high") val high: DetailThumbnailsInfo,
    @SerializedName("standard") val standard: DetailThumbnailsInfo,
    @SerializedName("maxres") val maxres: DetailThumbnailsInfo
)

data class PlaylistItemResourceIdentity(
    val kind: String,
    val videoId: String
)