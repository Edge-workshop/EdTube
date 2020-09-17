package dev.kingkongcode.edtube.model

import dev.kingkongcode.edtube.util.Constants
import org.json.JSONObject

class ThumbnailsSettings {

    var url: String = Constants.instance.EMPTY_STRING
    var width: Int = 0
    var height: Int = 0

    constructor(){
        this.url = Constants.instance.EMPTY_STRING
        this.width = 0
        this.height = 0
    }

    constructor(json: JSONObject){
        this.url = json.optString("url",Constants.instance.EMPTY_STRING)
        this.width = json.optInt("width")
        this.height = json.optInt("height")
    }
}