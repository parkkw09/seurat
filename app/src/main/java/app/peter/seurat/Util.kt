package app.peter.seurat

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser

object Util {
    fun toPrettyFormat(any: Any?): String? {
        val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(any)
    }

    fun toPrettyFormat(jsonString: String?): String? {
        val json = JsonParser.parseString(jsonString).asJsonObject
        val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(json)
    }
}