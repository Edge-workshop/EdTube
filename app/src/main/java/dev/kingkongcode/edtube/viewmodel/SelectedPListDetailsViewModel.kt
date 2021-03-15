package dev.kingkongcode.edtube.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.kingkongcode.edtube.app.Constants
import dev.kingkongcode.edtube.app.server.APIManager
import dev.kingkongcode.edtube.model.ETUser
import dev.kingkongcode.edtube.model.PlaylistItem

class SelectedPListDetailsViewModel(private val application: Application) : ViewModel() {
    lateinit var youtubeVideoID: String
    lateinit var selectedListId: String
    lateinit var etUser: ETUser  // TODO need to be read only property
    var numberOfVideo: String = Constants.EMPTY_STRING
    var mPlayList = arrayListOf<PlaylistItem>() // TODO need to be read only property
    var errorMessage : String? = null // TODO need to be read only property
    var loading = MutableLiveData<Boolean>()
    // To keep user in this activity after clicking no in LogOutDialog. But bottom navigation will go back on home item selected
    var isExiting = true

    // TODO find solution to remove completion callback
    fun requestSelectedPlaylist(selectedID: String) {
        APIManager.instance.requestSelectedPlaylistDetails(this.application, selectedID, null, completion = { error, selectedPList ->
            Log.i(TAG,"APIManager requestSelectedPlaylistDetails response receive. -> error message:$error")

            error?.let { errorMsg -> this.errorMessage = errorMsg }

            // Section where we populate adapter with api response data
            selectedPList?.let { xSelectedPList ->
                this.mPlayList = xSelectedPList.clone() as ArrayList<PlaylistItem>
                val strVideoIdList = arrayListOf<String>()
                for (videoStrId in this.mPlayList ) {
                    strVideoIdList.add(videoStrId.snippet.resourceId?.videoId!!)
                }

                APIManager.instance.requestGetVideoDuration(this.application, strVideoIdList, completion = { error, durationVideoList ->
                    error?.let { errorMsg ->
                        this.errorMessage = errorMsg
                        this.loading.value = false
                    }

                    durationVideoList?.let { _durationVideoList ->
                        // Section where we get list from API server and to match video id from user selected list from first API call
                        for (durationPairObj in _durationVideoList) {
                            for (videoStrId in this.mPlayList) {
                                if (durationPairObj.first == videoStrId.snippet.resourceId?.videoId) {
                                    videoStrId.duration = durationPairObj.second
                                }
                            }
                        }

                        this.youtubeVideoID = this.mPlayList[0].snippet.resourceId?.videoId!!
                        this.loading.value = false
                    }
                })
            }
        })
    }

    companion object{
        private const val TAG = "SelectedPListViewModel"
    }
}