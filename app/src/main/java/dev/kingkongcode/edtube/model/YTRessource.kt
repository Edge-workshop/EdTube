package dev.kingkongcode.edtube.model

import android.os.Parcel
import android.os.Parcelable
import dev.kingkongcode.edtube.util.Constants
import org.json.JSONObject

class YTRessource : Parcelable{

    var kind: String = Constants.instance.EMPTY_STRING
    var videoId: String = Constants.instance.EMPTY_STRING

    constructor(){
        this.kind = Constants.instance.EMPTY_STRING
        this.videoId = Constants.instance.EMPTY_STRING
    }

    constructor(json: JSONObject){
        this.kind = json.optString("kind",Constants.instance.EMPTY_STRING)
        this.videoId = json.optString("videoId")
    }

    constructor(p: Parcel){
        this.kind = p.readString()!!
        this.videoId = p.readString()!!
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, p1: Int) {
        dest?.writeString(kind)
        dest?.writeString(videoId)
    }

    companion object CREATOR: Parcelable.Creator<YTRessource>{
        override fun createFromParcel(source: Parcel): YTRessource {
            return YTRessource(source)
        }

        override fun newArray(size: Int): Array<YTRessource?> {
            return arrayOfNulls(size)
        }
    }
}