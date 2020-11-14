package dev.kingkongcode.edtube.model

import dev.kingkongcode.edtube.util.Constants
import org.json.JSONObject

class PlaylistCategory {
    var kind: String = Constants.instance.EMPTY_STRING
    var etag: String = Constants.instance.EMPTY_STRING
    var nextPageToken: String
    var items = ArrayList<PlaylistItem>()

    constructor(jsonObject: JSONObject) {
        this.kind = jsonObject.optString("kind")
        this.etag = jsonObject.optString("etag")
        this.nextPageToken = jsonObject.optString("nextPageToken")

        jsonObject.optJSONArray("items")?.let {
            for (i in 0 until it.length()){
                val jsonObj = it.optJSONObject(i)
                this.items.add(PlaylistItem(jsonObj))
            }
        }
    }
}