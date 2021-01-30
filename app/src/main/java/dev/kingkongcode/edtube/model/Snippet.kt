package dev.kingkongcode.edtube.model

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

class Snippet : Parcelable {
    private var publishedAt: String
    private var channelId: String
    var title: String
    var description: String
    lateinit var thumbnails: Thumbnails
    private var channelTitle: String
    lateinit var resourceId: YTResource

    constructor(jsonObject: JSONObject) {
        this.publishedAt = jsonObject.optString("publishedAt")
        this.channelId = jsonObject.optString("channelId")
        this.title = jsonObject.optString("title")
        this.description = jsonObject.optString("description")

        jsonObject.optJSONObject("thumbnails")?.let {
            this.thumbnails = Thumbnails(it)
        }

        this.channelTitle = jsonObject.optString("channelTitle")

        jsonObject.optJSONObject("resourceId")?.let {
            this.resourceId = YTResource(it)
        }
    }

    constructor(p: Parcel) {
        this.publishedAt = p.readString()!!
        this.channelId = p.readString()!!
        this.title = p.readString()!!
        this.description = p.readString()!!
        this.thumbnails = p.readParcelable(Thumbnails::class.java.classLoader)!!
        this.channelTitle = p.readString()!!
        this.resourceId = p.readParcelable(YTResource::class.java.classLoader)!!
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
        dest?.writeParcelable(resourceId, 0)
    }

    companion object CREATOR: Parcelable.Creator<Snippet> {

        override fun createFromParcel(source: Parcel): Snippet {
            return Snippet(source)
        }

        override fun newArray(size: Int): Array<Snippet?> {
            return arrayOfNulls(size)
        }
    }
}