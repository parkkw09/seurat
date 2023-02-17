package app.peter.seurat.model

import com.google.gson.annotations.SerializedName

data class Subscription(
    val kind: String,
    val etag: String,
    val id: String,
    val snippet: SubscriptionSnippet,
    val contentDetails: SubscriptionContentDetails,
)

data class SubscriptionSnippet(
    val publishedAt: String,
    val title: String,
    val description: String,
    val resourceId: ResourceIdentity,
    val channelId: String,
    val thumbnails: Thumbnails,
)

data class ResourceIdentity(
    val kind: String,
    val channelId: String
)

data class Thumbnails(
    @SerializedName("default") val default: ThumbnailsInfo,
    @SerializedName("medium") val medium: ThumbnailsInfo,
    @SerializedName("high") val high: ThumbnailsInfo
)

data class SubscriptionContentDetails(
    val totalItemCount: Int,
    val newItemCount: Int,
    val activityType: String
)