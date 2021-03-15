package dev.kingkongcode.edtube.model

import dev.kingkongcode.edtube.app.Constants
import org.json.JSONObject


data class PlaylistCategory(var nextPageToken: String, var items: ArrayList<PlaylistItem>) {
    constructor(json: JSONObject): this(
        json.optString("nextPageToken", Constants.EMPTY_STRING),
        arrayListOf<PlaylistItem>().apply {
            json.optJSONArray("items")?.let {
                for (i in 0 until it.length()){
                    val jsonObj = it.optJSONObject(i)
                    this.add(PlaylistItem(jsonObj))
                }
            }
        }
    )
}
