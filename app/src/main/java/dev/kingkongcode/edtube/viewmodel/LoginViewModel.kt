package dev.kingkongcode.edtube.viewmodel


import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import dev.kingkongcode.edtube.R
import dev.kingkongcode.edtube.app.server.Config
import dev.kingkongcode.edtube.view.LoginActivity


class LoginViewModel : ViewModel() {
    lateinit var mGoogleSignInClient : GoogleSignInClient

    fun initGoogleSignIn(context: Context) {
        //Google SignIn button
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(Config.YOUTUBE_AUTH_READONLY))
            .requestScopes(Scope(Config.YOUTUBE_AUTH_UPLOAD))
            .requestIdToken(Config.CLIENT_ID)
            .requestServerAuthCode(Config.CLIENT_ID)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso)
        // ...
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
    }

    fun regularSingInPress(activity: Activity) {
        Toast.makeText(activity.baseContext, "NOT implemented yet", Toast.LENGTH_SHORT).show()
        println("Sign out")
        signOut(activity)
    }

    //TODO don't forget to remove func no sign out in this page only for test
    fun signOut(activity: Activity) {
        mGoogleSignInClient.signOut()
            .addOnCompleteListener(activity) {
                Toast.makeText(activity.baseContext, activity.getString(R.string.signOut_success) , Toast.LENGTH_LONG).show()
                //finish()
            }
    }

}