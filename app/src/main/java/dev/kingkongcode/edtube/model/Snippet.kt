package dev.kingkongcode.edtube.model

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

data class Snippet(
    private var publishedAt: String,
    private var channelId: String,
    var title: String,
    var description: String,
    var thumbnails: Thumbnails?,
    private var channelTitle: String,
    var resourceId: YTResource?,
) : Parcelable {

    constructor(jsonObject: JSONObject) : this(
        jsonObject.optString("publishedAt"),
        jsonObject.optString("channelId"),
        jsonObject.optString("title"),
        jsonObject.optString("description"),
        jsonObject.optJSONObject("thumbnails")?.let {
            Thumbnails(it)
        },
        jsonObject.optString("channelTitle"),
        jsonObject.optJSONObject("resourceId")?.let {
            YTResource(it)
        }
    )

    constructor(p: Parcel) : this(
        p.readString()!!,
        p.readString()!!,
        p.readString()!!,
        p.readString()!!,
        p.readParcelable(Thumbnails::class.java.classLoader),
        p.readString()!!,
        p.readParcelable(YTResource::class.java.classLoader)
    )

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