package dev.kingkongcode.edtube.model

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

class DetailsXItem(json: JSONObject) : Parcelable {
    private var itemCount: Int = json.optInt("itemCount", 0)
    var itemCountStr: String

    init {
        this.itemCountStr = this.itemCount.toString()
    }

    override fun describeContents(): Int {
       return 0
    }

    override fun writeToParcel(dest: Parcel?, p1: Int) {
        dest?.writeInt(itemCount)
        dest?.writeString(itemCountStr)
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