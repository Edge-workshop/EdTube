package dev.kingkongcode.edtube.model

import dev.kingkongcode.edtube.util.Constants
import org.json.JSONArray
import org.json.JSONObject

class PlaylistCategory {

    var kind: String = Constants.instance.EMPTY_STRING
    var etag: String = Constants.instance.EMPTY_STRING
    var items = ArrayList<PlaylistItem>()

    constructor(){
        this.kind = Constants.instance.EMPTY_STRING
        this.etag = Constants.instance.EMPTY_STRING
        this.items = ArrayList()
    }

    constructor(jsonObject: JSONObject){
        this.kind = jsonObject.optString("kind")
        this.etag = jsonObject.optString("etag")

        jsonObject.optJSONArray("items")?.let {
            for (i in 0 until it.length()){
                val jsonObj = it.optJSONObject(i)
                this.items.add(PlaylistItem(jsonObj))
            }
        }

    }

}