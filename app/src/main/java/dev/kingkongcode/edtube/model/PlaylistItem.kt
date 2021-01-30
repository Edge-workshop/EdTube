package dev.kingkongcode.edtube.model

import android.os.Parcel
import android.os.Parcelable
import dev.kingkongcode.edtube.util.Constants
import org.json.JSONObject

class PlaylistItem : Parcelable {
    private var kind: String
    private var eTag: String
    var listId: String
    lateinit var id: YTResource
    lateinit var snippet: Snippet
    lateinit var detailsXItem: DetailsXItem
    var isVideoSelected: Boolean = false
    var duration: String = Constants.EMPTY_STRING

    constructor(jsonObject: JSONObject) {
        this.kind = jsonObject.optString("kind")
        this.eTag = jsonObject.optString("etag")
        this.listId = jsonObject.optString("id")

        jsonObject.optJSONObject("id")?.let {
            this.id = YTResource(it)
        }

        jsonObject.optJSONObject("snippet")?.let {
           this.snippet = Snippet(it)
        }

        jsonObject.optJSONObject("contentDetails")?.let {
            this.detailsXItem = DetailsXItem(it)
        }
    }

    constructor(p: Parcel) {
        this.kind = p.readString()?: Constants.EMPTY_STRING
        this.eTag = p.readString()?: Constants.EMPTY_STRING
        this.listId = p.readString()?: Constants.EMPTY_STRING
        this.id = p.readParcelable(YTResource::class.java.classLoader)!!
        this.snippet = p.readParcelable(Snippet::class.java.classLoader)!!
        this.detailsXItem = p.readParcelable(DetailsXItem::class.java.classLoader)!!
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