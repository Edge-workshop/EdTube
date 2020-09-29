package dev.kingkongcode.edtube.model

import android.os.Parcel
import android.os.Parcelable
import dev.kingkongcode.edtube.util.Constants
import org.json.JSONObject

class PlaylistItemActivity : Parcelable{

    //TODO remove activity in class name

    var kind: String = Constants.instance.EMPTY_STRING
    var etag: String = Constants.instance.EMPTY_STRING
    var listId: String = Constants.instance.EMPTY_STRING
    var id: YTRessource = YTRessource()
    var snippet: Snippet = Snippet()
    var detailsXItem: DetailsXItem = DetailsXItem()
    var isVideoSelected: Boolean = false

    constructor(){
        this.kind = Constants.instance.EMPTY_STRING
        this.etag = Constants.instance.EMPTY_STRING
        this.listId = Constants.instance.EMPTY_STRING
        this.id = YTRessource()
        this.snippet = Snippet()
        this.detailsXItem = DetailsXItem()
    }

    constructor(jsonObject: JSONObject){
        this.kind = jsonObject.optString("kind")
        this.etag = jsonObject.optString("etag")
        this.listId = jsonObject.optString("id")

        jsonObject.optJSONObject("id")?.let {
            this.id = YTRessource(it)
        }

        jsonObject.optJSONObject("snippet")?.let {
           this.snippet = Snippet(it)
        }

        jsonObject.optJSONObject("contentDetails")?.let {
            this.detailsXItem = DetailsXItem(it)
        }
    }

    constructor(p: Parcel){
        this.kind = p.readString()?: Constants.instance.EMPTY_STRING
        this.etag = p.readString()?: Constants.instance.EMPTY_STRING
        this.listId = p.readString()?: Constants.instance.EMPTY_STRING
        this.id = p.readParcelable(YTRessource::class.java.classLoader)!!
        this.snippet = p.readParcelable(Snippet::class.java.classLoader)!!
        this.detailsXItem = p.readParcelable(DetailsXItem::class.java.classLoader)!!

    }


    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, p1: Int) {
        dest?.writeString(kind)
        dest?.writeString(etag)
        dest?.writeString(listId)
        dest?.writeParcelable(id,0)
        dest?.writeParcelable(snippet, 0)
        dest?.writeParcelable(detailsXItem, 0)
    }

    companion object CREATOR: Parcelable.Creator<PlaylistItemActivity>{
        override fun createFromParcel(source: Parcel): PlaylistItemActivity {
            return PlaylistItemActivity(source)
        }

        override fun newArray(size: Int): Array<PlaylistItemActivity?> {
            return arrayOfNulls(size)
        }
    }


}