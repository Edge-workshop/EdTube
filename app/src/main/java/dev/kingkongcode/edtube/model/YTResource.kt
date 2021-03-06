package dev.kingkongcode.edtube.model

import android.os.Parcel
import android.os.Parcelable
import dev.kingkongcode.edtube.util.Constants
import org.json.JSONObject

class YTResource : Parcelable {
    var kind: String
    var videoId: String

    constructor(json: JSONObject) {
        this.kind = json.optString("kind",Constants.EMPTY_STRING)
        this.videoId = json.optString("videoId")
    }

    constructor(p: Parcel) {
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

    companion object CREATOR: Parcelable.Creator<YTResource> {
        override fun createFromParcel(source: Parcel): YTResource {
            return YTResource(source)
        }

        override fun newArray(size: Int): Array<YTResource?> {
            return arrayOfNulls(size)
        }
    }
}