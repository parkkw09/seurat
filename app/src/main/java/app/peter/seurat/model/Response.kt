package app.peter.seurat.model

import com.google.gson.annotations.SerializedName

data class YoutubeResponse<T>(
    @SerializedName("kind") val kind: String,
    val etag: String,
    val pageInfo: PageInfo,
    val items: List<T>
)

data class PageInfo(
    val totalResults: Int,
    val resultsPerPage: Int
)

data class ThumbnailsInfo(
    val url: String
)

data class DetailThumbnailsInfo(
    val url: String,
    val width: Int,
    val height: Int
)

data class Localized(
    val title: String,
    val description: String
)