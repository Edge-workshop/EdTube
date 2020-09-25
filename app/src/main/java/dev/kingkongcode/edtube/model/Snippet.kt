package dev.kingkongcode.edtube.model

import android.os.Parcel
import android.os.Parcelable
import dev.kingkongcode.edtube.util.Constants
import org.json.JSONObject

class Snippet : Parcelable{

    var publishedAt: String = Constants.instance.EMPTY_STRING
    var channelId: String = Constants.instance.EMPTY_STRING
    var title: String = Constants.instance.EMPTY_STRING
    var description: String = Constants.instance.EMPTY_STRING
    var thumbnails: Thumbnails = Thumbnails()
    var channelTitle: String = Constants.instance.EMPTY_STRING
    var ressourceId: YTRessource = YTRessource()

    constructor(){
        this.publishedAt = Constants.instance.EMPTY_STRING
        this.channelId = Constants.instance.EMPTY_STRING
        this.title = Constants.instance.EMPTY_STRING
        this.description = Constants.instance.EMPTY_STRING
        this.thumbnails = Thumbnails()
        this.channelTitle = Constants.instance.EMPTY_STRING
        this.ressourceId = YTRessource()
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

        jsonObject.optJSONObject("resourceId")?.let {
            this.ressourceId = YTRessource(it)
        }
    }

    constructor(p: Parcel){
        this.publishedAt = p.readString()!!
        this.channelId = p.readString()!!
        this.title = p.readString()!!
        this.description = p.readString()!!
        this.thumbnails = p.readParcelable(Thumbnails::class.java.classLoader)!!
        this.channelTitle = p.readString()!!
        this.ressourceId = p.readParcelable(YTRessource::class.java.classLoader)!!
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, p1: Int) {
        dest?.writeString(publishedAt)
        dest?.writeString(channelId)
        dest?.writeString(title)
        dest?.writeString(description)
        dest?.writeParcelable(thumbnails, 0)
        dest?.writeString(channelTitle)
        dest?.writeParcelable(ressourceId, 0)
    }

    companion object CREATOR: Parcelable.Creator<Snippet>{

        override fun createFromParcel(source: Parcel): Snippet {
            return Snippet(source)
        }

        override fun newArray(size: Int): Array<Snippet?> {
            return arrayOfNulls(size)
        }
    }


}