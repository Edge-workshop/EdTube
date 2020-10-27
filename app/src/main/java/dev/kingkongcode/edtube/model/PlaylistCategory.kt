package dev.kingkongcode.edtube.model

import dev.kingkongcode.edtube.util.Constants
import org.json.JSONObject

class PlaylistCategory {

    var kind: String = Constants.instance.EMPTY_STRING
    var etag: String = Constants.instance.EMPTY_STRING
    lateinit var nextPageToken: String
    var items = ArrayList<PlaylistItemActivity>()

    constructor(){
        this.kind = Constants.instance.EMPTY_STRING
        this.etag = Constants.instance.EMPTY_STRING
        this.items = ArrayList()
    }

    constructor(jsonObject: JSONObject){
        this.kind = jsonObject.optString("kind")
        this.etag = jsonObject.optString("etag")
        this.nextPageToken = jsonObject.optString("nextPageToken")

        jsonObject.optJSONArray("items")?.let {
            for (i in 0 until it.length()){
                val jsonObj = it.optJSONObject(i)
                this.items.add(PlaylistItemActivity(jsonObj))
            }
        }

    }

}