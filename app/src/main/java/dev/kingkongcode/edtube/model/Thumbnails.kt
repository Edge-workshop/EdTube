package dev.kingkongcode.edtube.model

import org.json.JSONObject

class Thumbnails {

    var default: ThumbnailsSettings = ThumbnailsSettings()
    var medium: ThumbnailsSettings = ThumbnailsSettings()
    var high: ThumbnailsSettings = ThumbnailsSettings()
    var standard: ThumbnailsSettings = ThumbnailsSettings()

    constructor(){
        this.default= ThumbnailsSettings()
        this.medium= ThumbnailsSettings()
        this.high= ThumbnailsSettings()
        this.standard= ThumbnailsSettings()
    }

    constructor(jsonObject: JSONObject){

        jsonObject.optJSONObject("default")?.let {
            this.default= ThumbnailsSettings(it)
        }
        jsonObject.optJSONObject("medium")?.let {
            this.medium= ThumbnailsSettings(it)
        }
        jsonObject.optJSONObject("high")?.let {
            this.high= ThumbnailsSettings(it)
        }
        jsonObject.optJSONObject("standard")?.let {
            this.standard= ThumbnailsSettings(it)
        }

//        this.default= ThumbnailsSettings(jsonObject.optJSONObject("default"))
//        this.medium= ThumbnailsSettings(jsonObject)
//        this.high= ThumbnailsSettings(jsonObject)
//        this.standard= ThumbnailsSettings(jsonObject)
    }
}