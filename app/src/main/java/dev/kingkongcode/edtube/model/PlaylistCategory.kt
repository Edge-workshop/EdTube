package dev.kingkongcode.edtube.model

import org.json.JSONObject

class PlaylistCategory(jsonObject: JSONObject) {
    var nextPageToken: String = jsonObject.optString("nextPageToken")
    var items = ArrayList<PlaylistItem>()

    init {
        jsonObject.optJSONArray("items")?.let {
            for (i in 0 until it.length()){
                val jsonObj = it.optJSONObject(i)
                this.items.add(PlaylistItem(jsonObj))
            }
        }
    }
}