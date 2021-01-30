package dev.kingkongcode.edtube.model

import android.os.Parcel
import android.os.Parcelable
import dev.kingkongcode.edtube.util.Constants
import org.json.JSONObject

class ThumbnailsSettings(json: JSONObject) : Parcelable {
    var url: String = json.optString("url",Constants.EMPTY_STRING)
    var width: Int = json.optInt("width")
    var height: Int = json.optInt("height")

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, p1: Int) {
        dest?.writeString(url)
        dest?.writeInt(width)
        dest?.writeInt(height)
    }

    companion object CREATOR: Parcelable.Creator<ThumbnailsSettings> {
        override fun createFromParcel(source: Parcel?): ThumbnailsSettings {
            return createFromParcel(source)
        }

        override fun newArray(size: Int): Array<ThumbnailsSettings?> {
            return arrayOfNulls(size)
        }
    }
}