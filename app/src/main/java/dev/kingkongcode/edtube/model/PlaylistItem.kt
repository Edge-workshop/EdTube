package dev.kingkongcode.edtube.model

import dev.kingkongcode.edtube.util.Constants
import org.json.JSONObject

class PlaylistItem {

    var kind: String = Constants.instance.EMPTY_STRING
    var etag: String = Constants.instance.EMPTY_STRING
    var id: String = Constants.instance.EMPTY_STRING
    var snippet: Snippet = Snippet()
    var detailsXItem: DetailsXItem = DetailsXItem()

    constructor(){
        this.kind = Constants.instance.EMPTY_STRING
        this.etag = Constants.instance.EMPTY_STRING
        this.id = Constants.instance.EMPTY_STRING
        this.snippet = Snippet()
        this.detailsXItem = DetailsXItem()
    }

    constructor(jsonObject: JSONObject){
        this.kind = jsonObject.optString("kind")
        this.etag = jsonObject.optString("etag")
        this.id = jsonObject.optString("id")

        jsonObject.optJSONObject("snippet")?.let {
           this.snippet = Snippet(it)
        }

        jsonObject.optJSONObject("contentDetails")?.let {
            this.detailsXItem = DetailsXItem(it)
        }
    }






}