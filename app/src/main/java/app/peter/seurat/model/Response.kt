package app.peter.seurat.model

import com.google.gson.annotations.SerializedName

data class Response<T>(
    val id: String,
    @SerializedName("xxx") val foo: String,
    val items: T
)