package dev.kingkongcode.edtube.model

import org.json.JSONObject


data class PlaylistCategory(var nextPageToken: String, var items: ArrayList<PlaylistItem>) {

    constructor(json: JSONObject) : this (
        json.optString("nextPageToken"),
        arrayListOf<PlaylistItem>().apply {
            json.optJSONArray("items")?.let {
                var temp = arrayListOf<PlaylistItem>()
                for (i in 0 until it.length()){
                    val jsonObj = it.optJSONObject(i)
                    temp.add(PlaylistItem(jsonObj))
                }
            }
        }
    )
}

//data class PlaylistCategory(val jsonObject: JSONObject) {
//    var nextPageToken: String = jsonObject.optString("nextPageToken")
//    var items = ArrayList<PlaylistItem>()
//
//    init {
//        jsonObject.optJSONArray("items")?.let {
//            for (i in 0 until it.length()){
//                val jsonObj = it.optJSONObject(i)
//                this.items.add(PlaylistItem(jsonObj))
//            }
//        }
//    }
//}