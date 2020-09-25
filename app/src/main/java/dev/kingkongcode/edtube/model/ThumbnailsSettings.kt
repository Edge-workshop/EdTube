package dev.kingkongcode.edtube.model

import android.os.Parcel
import android.os.Parcelable
import dev.kingkongcode.edtube.util.Constants
import org.json.JSONObject

class ThumbnailsSettings : Parcelable{

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

    constructor(p: Parcel){
        this.url = p.readString()!!
        this.width = p.readInt()
        this.height = p.readInt()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, p1: Int) {
        dest?.writeString(url)
        dest?.writeInt(width)
        dest?.writeInt(height)
    }

    companion object CREATOR: Parcelable.Creator<ThumbnailsSettings>{
        override fun createFromParcel(source: Parcel?): ThumbnailsSettings {
            return createFromParcel(source)
        }

        override fun newArray(size: Int): Array<ThumbnailsSettings?> {
            return arrayOfNulls(size)
        }
    }
}