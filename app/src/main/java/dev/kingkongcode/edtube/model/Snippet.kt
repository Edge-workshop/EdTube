package dev.kingkongcode.edtube.model

import dev.kingkongcode.edtube.util.Constants
import org.json.JSONObject

class Snippet {

    var publishedAt: String = Constants.instance.EMPTY_STRING
    var channelId: String = Constants.instance.EMPTY_STRING
    var title: String = Constants.instance.EMPTY_STRING
    var description: String = Constants.instance.EMPTY_STRING
    var thumbnails: Thumbnails = Thumbnails()
    var channelTitle: String = Constants.instance.EMPTY_STRING

    constructor(){
        this.publishedAt = Constants.instance.EMPTY_STRING
        this.channelId = Constants.instance.EMPTY_STRING
        this.title = Constants.instance.EMPTY_STRING
        this.description = Constants.instance.EMPTY_STRING
        this.thumbnails = Thumbnails()
        this.channelTitle = Constants.instance.EMPTY_STRING
    }

    constructor(jsonObject: JSONObject){
        this.publishedAt = jsonObject.optString("publishedAt")
        this.channelId = jsonObject.optString("channelId")
        this.title = jsonObject.optString("title")
        this.description = jsonObject.optString("description")

        jsonObject.optJSONObject("thumbnails")?.let {
            this.thumbnails = Thumbnails(it)
        }

        this.channelTitle = jsonObject.optString("channelTitle")
    }

}