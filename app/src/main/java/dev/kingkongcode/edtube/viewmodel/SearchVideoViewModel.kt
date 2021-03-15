package dev.kingkongcode.edtube.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.kingkongcode.edtube.app.server.APIManager
import dev.kingkongcode.edtube.model.ETUser
import dev.kingkongcode.edtube.model.PlaylistItem

class SearchVideoViewModel(private val application: Application) : ViewModel() {
    var mSearchResultList = arrayListOf<PlaylistItem>()
    lateinit var etUser: ETUser
    var clearButtonShowing = false
    var loading = MutableLiveData<Boolean>()
    var errorMessage: String? = null

    fun setQueryListResult(searchQuery: String) {
        APIManager.instance.requestSearchVideo(application.applicationContext, searchQuery, completion = { error, searchResultList ->
            error?.let { errorMsg ->  this.errorMessage = errorMsg }

            searchResultList?.let { xSelectedPList ->
                val strVideoIdList = arrayListOf<String>()
                for (video in xSelectedPList){
                    if (video.id.kind == "youtube#video"){
                        this.mSearchResultList.add(video)
                        strVideoIdList.add(video.id.videoId)
                    }
                }

                APIManager.instance.requestGetVideoDuration(application.applicationContext, strVideoIdList, completion = { error, durationVideoList ->
                    error?.let { errorMsg -> this.errorMessage = errorMsg }

                    durationVideoList?.let {
                        // Section where we get list from API server and to match video id from user selected list from first API call
                        for (durationPairObj in it) {
                            for (videoStrId in this.mSearchResultList) {
                                if (durationPairObj.first == videoStrId.id.videoId) {
                                    videoStrId.duration = durationPairObj.second
                                }
                            }
                        }

                        this.loading.value = false
                    }
                })
            }
        })
    }
}