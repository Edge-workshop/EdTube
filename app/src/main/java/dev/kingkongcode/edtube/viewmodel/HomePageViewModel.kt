package dev.kingkongcode.edtube.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import dev.kingkongcode.edtube.app.PaginationList
import dev.kingkongcode.edtube.app.server.APIManager
import dev.kingkongcode.edtube.app.server.Config
import dev.kingkongcode.edtube.model.ETUser
import dev.kingkongcode.edtube.model.PlaylistItem

class HomePageViewModel(private val application: Application) : ViewModel() {
    var userPlayList = arrayListOf<PlaylistItem>()
    var currentPage: Int = 1
    var totalPage: Int = 1
    lateinit var paginationDisplay: String
    private lateinit var edTubeUser: ETUser
    var loading = MutableLiveData<Boolean>()
    var errorMsg = MutableLiveData<String?>()
    lateinit var mGoogleSignInClient: GoogleSignInClient

    // TODO test and find solution in case acct? is null and create an empty user object
    fun setUserProfile() {
        val acct = GoogleSignIn.getLastSignedInAccount(application.applicationContext)
        acct?.let { googleAccount ->
            Log.i(TAG, "setting User object with all is personal info")
            this.edTubeUser = ETUser(googleAccount.givenName,
                googleAccount.familyName,
                googleAccount.email,
                googleAccount.photoUrl,
            )
        }
    }

    fun getUserProfile() : ETUser {
        return this.edTubeUser
    }

    // TODO replace completion call
    fun setUserPlaylist() {
        Log.i(TAG,"Request user list to api manager")
        //this.loading.value = true
        APIManager.instance.requestUserPlaylist(application.applicationContext, completion = { error, userPlaylist ->
            Log.i(TAG,"APIManager requestUserPlaylist response receive in activity")
            error?.let { _errorMsg ->
                this.errorMsg.value = _errorMsg
            }

            //Code section when we populate GridView adapter
            userPlaylist?.let { playListCategory ->
                //Code section to display user current page and max page
                this.userPlayList = playListCategory.items
                val (a, b) = PaginationList.showNbrPage(this.userPlayList, this.currentPage) // a= String text showing current/total b= Int total page
                this.paginationDisplay = a
                this.totalPage = b
            }

            this.loading.value = false
        })
    }

    fun showNumberOfPage() : Pair<String, Int> {
        return PaginationList.showNbrPage(this.userPlayList, this.currentPage)
    }

    fun initGoogleSignIn() {
        //Google SignIn button
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(Config.YOUTUBE_AUTH_READONLY))
            .requestScopes(Scope(Config.YOUTUBE_AUTH_UPLOAD))
            .requestIdToken(Config.CLIENT_ID)
            .requestServerAuthCode(Config.CLIENT_ID)
            .requestEmail()
            .build()
        this.mGoogleSignInClient = GoogleSignIn.getClient(application, gso)
        // ...
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
    }

    companion object {
        const val TAG = "HomePageViewModel"
    }
}