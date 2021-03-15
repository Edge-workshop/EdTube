package dev.kingkongcode.edtube.model

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

data class DetailsXItem(var itemCount: Int) : Parcelable {
    var itemCountStr: String = this.itemCount.toString()

    constructor(jsonObject: JSONObject): this(
        jsonObject.optInt("itemCount", 0)
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel?, p1: Int) {
        dest?.writeInt(this.itemCount)
        dest?.writeString(this.itemCountStr)
    }

    companion object CREATOR: Parcelable.Creator<DetailsXItem> {
        override fun createFromParcel(source: Parcel?): DetailsXItem {
            return createFromParcel(source)
        }

        override fun newArray(size: Int): Array<DetailsXItem?> {
            return arrayOfNulls(size)
        }
    }
}

