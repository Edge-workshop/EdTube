package dev.kingkongcode.edtube.model

import android.os.Parcel
import android.os.Parcelable
import dev.kingkongcode.edtube.util.Constants
import org.json.JSONObject

class DetailsXItem : Parcelable {

    var itemCount: Int = 0
    var itemCountStr: String = Constants.instance.EMPTY_STRING

    constructor(){
        this.itemCount = 0
        this.itemCountStr = this.itemCount.toString()
    }

    constructor(json: JSONObject){
        this.itemCount = json.optInt("itemCount", 0)
        this.itemCountStr = this.itemCount.toString()
    }

    constructor(p: Parcel){
        this.itemCount = p.readInt()
        this.itemCountStr = p.readString()!!
    }

    override fun describeContents(): Int {
       return 0
    }

    override fun writeToParcel(dest: Parcel?, p1: Int) {
        dest?.writeInt(itemCount)
        dest?.writeString(itemCountStr)
    }

    companion object CREATOR: Parcelable.Creator<DetailsXItem>{
        override fun createFromParcel(source: Parcel?): DetailsXItem {
            return createFromParcel(source)
        }

        override fun newArray(size: Int): Array<DetailsXItem?> {
            return arrayOfNulls(size)
        }
    }


}