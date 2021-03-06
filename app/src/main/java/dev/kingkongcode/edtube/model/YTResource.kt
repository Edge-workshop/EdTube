package dev.kingkongcode.edtube.model

import android.os.Parcel
import android.os.Parcelable
import dev.kingkongcode.edtube.app.Constants
import org.json.JSONObject

data class YTResource(val kind: String, val videoId: String) : Parcelable {


    constructor(jsonObject: JSONObject) : this (
        jsonObject.optString("kind", Constants.EMPTY_STRING),
        jsonObject.optString("videoId")
    )

    constructor(p: Parcel) : this (
        p.readString()!!,
        p.readString()!!
    )

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