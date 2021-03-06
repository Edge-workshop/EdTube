package dev.kingkongcode.edtube.model

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

data class Thumbnails(
    var default: ThumbnailsSettings?,
    var medium: ThumbnailsSettings?,
    var high: ThumbnailsSettings?,
    var standard: ThumbnailsSettings?
) : Parcelable {


    constructor(jsonObject: JSONObject) : this (
        jsonObject.optJSONObject("default")?.let {
            ThumbnailsSettings(it)
        },
        jsonObject.optJSONObject("medium")?.let {
            ThumbnailsSettings(it)
        },
        jsonObject.optJSONObject("high")?.let {
            ThumbnailsSettings(it)
        },
        jsonObject.optJSONObject("standard")?.let {
            ThumbnailsSettings(it)
        }
    )

    constructor(p: Parcel) : this (
        p.readParcelable(ThumbnailsSettings::class.java.classLoader),
        p.readParcelable(ThumbnailsSettings::class.java.classLoader),
        p.readParcelable(ThumbnailsSettings::class.java.classLoader),
        p.readParcelable(ThumbnailsSettings::class.java.classLoader)
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, p1: Int) {
        dest?.writeParcelable(default, 0)
        dest?.writeParcelable(medium, 0)
        dest?.writeParcelable(high, 0)
        dest?.writeParcelable(standard, 0)
    }

    companion object CREATOR: Parcelable.Creator<Thumbnails> {
        override fun createFromParcel(source: Parcel): Thumbnails {
            return Thumbnails(source)
        }

        override fun newArray(size: Int): Array<Thumbnails?> {
            return arrayOfNulls(size)
        }
    }
}