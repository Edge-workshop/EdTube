package dev.kingkongcode.edtube.model

import android.os.Parcel
import android.os.Parcelable
import dev.kingkongcode.edtube.app.Constants
import org.json.JSONObject

data class PlaylistItem(
    private var kind: String,
    private var eTag: String,
    var listId: String,
    var isVideoSelected: Boolean = false,
    var duration: String = Constants.EMPTY_STRING
) : Parcelable {
//    private var kind: String
//    private var eTag: String
//    var listId: String
    lateinit var id: YTResource
    lateinit var snippet: Snippet
    lateinit var detailsXItem: DetailsXItem
//    var isVideoSelected: Boolean = false
//    var duration: String = Constants.EMPTY_STRING

    constructor(jsonObject: JSONObject) : this (
        jsonObject.optString("kind"),
        jsonObject.optString("etag"),
        jsonObject.optString("id")
    ) {
        jsonObject.optJSONObject("id")?.run { id = YTResource(this) }
        jsonObject.optJSONObject("snippet")?.run { snippet = Snippet(this) }
        jsonObject.optJSONObject("contentDetails")?.run { detailsXItem = DetailsXItem(this) }
    }

    constructor(p: Parcel) : this (
        p.readString()?: Constants.EMPTY_STRING,
        p.readString()?: Constants.EMPTY_STRING,
        p.readString()?: Constants.EMPTY_STRING,
        ) {
        id = p.readParcelable(YTResource::class.java.classLoader)!!
        snippet = p.readParcelable(Snippet::class.java.classLoader)!!
        detailsXItem = p.readParcelable(DetailsXItem::class.java.classLoader)!!
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, p1: Int) {
        dest?.writeString(kind)
        dest?.writeString(eTag)
        dest?.writeString(listId)
        dest?.writeParcelable(id,0)
        dest?.writeParcelable(snippet, 0)
        dest?.writeParcelable(detailsXItem, 0)
    }

    companion object CREATOR: Parcelable.Creator<PlaylistItem> {
        override fun createFromParcel(source: Parcel): PlaylistItem {
            return PlaylistItem(source)
        }

        override fun newArray(size: Int): Array<PlaylistItem?> {
            return arrayOfNulls(size)
        }
    }
}