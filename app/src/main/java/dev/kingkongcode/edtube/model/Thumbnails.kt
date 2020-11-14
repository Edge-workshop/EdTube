package dev.kingkongcode.edtube.model

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

class Thumbnails : Parcelable {
    var default: ThumbnailsSettings = ThumbnailsSettings()
    var medium: ThumbnailsSettings = ThumbnailsSettings()
    var high: ThumbnailsSettings = ThumbnailsSettings()
    var standard: ThumbnailsSettings = ThumbnailsSettings()

    constructor() {
        this.default= ThumbnailsSettings()
        this.medium= ThumbnailsSettings()
        this.high= ThumbnailsSettings()
        this.standard= ThumbnailsSettings()
    }

    constructor(jsonObject: JSONObject) {
        jsonObject.optJSONObject("default")?.let {
            this.default= ThumbnailsSettings(it)
        }

        jsonObject.optJSONObject("medium")?.let {
            this.medium= ThumbnailsSettings(it)
        }

        jsonObject.optJSONObject("high")?.let {
            this.high= ThumbnailsSettings(it)
        }

        jsonObject.optJSONObject("standard")?.let {
            this.standard= ThumbnailsSettings(it)
        }
    }

    constructor(p: Parcel) {
        this.default = p.readParcelable(ThumbnailsSettings::class.java.classLoader)!!
        this.medium = p.readParcelable(ThumbnailsSettings::class.java.classLoader)!!
        this.high = p.readParcelable(ThumbnailsSettings::class.java.classLoader)!!
        this.standard = p.readParcelable(ThumbnailsSettings::class.java.classLoader)!!
    }

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