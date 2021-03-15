package dev.kingkongcode.edtube.viewmodel

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import dev.kingkongcode.edtube.app.server.APIManager
import dev.kingkongcode.edtube.app.server.Config
import dev.kingkongcode.edtube.model.ETUser


class LoginViewModel(private val application: Application) : ViewModel()  {
    lateinit var mGoogleSignInClient: GoogleSignInClient
    val loading = MutableLiveData<Boolean>()
    val errorMsg = MutableLiveData<String?>()
    var success = MutableLiveData<Boolean>()

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

    fun googleSignInData(data: Intent?) {
        processSignInData(GoogleSignIn.getSignedInAccountFromIntent(data))
    }

    // TODO find replacement for completion call
    private fun processSignInData(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            account?.let { googleAccount ->
                val idToken = googleAccount.idToken
                val deviceCode = googleAccount.serverAuthCode

                APIManager.instance.requestAccessToken(
                    application,
                    idToken,
                    deviceCode,
                    completion = { error ->
                        if (error.isNullOrEmpty()) {
                            Log.i(
                                LoginViewModel.TAG,
                                "Google access request is completed and successful going to HomePage activity"
                            )
                            success.value = true
                        } else errorMsg.value = error
                    })
            }
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(LoginViewModel.TAG, "signInResult:failed code=" + e.statusCode)
            loading.value = false
        }
    }

    fun clearErrorMsg() {
        errorMsg.value = null
    }

    private companion object {
        const val TAG = "LoginViewModel"
    }
}