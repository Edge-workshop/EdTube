package dev.kingkongcode.edtube.model

import dev.kingkongcode.edtube.util.Constants
import org.json.JSONObject

class DetailsXItem {

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
}