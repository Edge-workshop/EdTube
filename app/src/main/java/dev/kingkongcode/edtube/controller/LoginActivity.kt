package dev.kingkongcode.edtube.controller

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import dev.kingkongcode.edtube.R
import dev.kingkongcode.edtube.server.APIManager
import dev.kingkongcode.edtube.server.Config
import dev.kingkongcode.edtube.util.Constants
import dev.kingkongcode.edtube.util.HideSystemUi


class LoginActivity : AppCompatActivity() {

    private val TAG = "LoginActivity"
    private lateinit var progressBar: ProgressBar

    private lateinit var googleSignInBtn : SignInButton
    private lateinit var mGoogleSignInClient : GoogleSignInClient
    private val RC_SIGN_IN = 0
    private val RC_GET_TOKEN = 90

    private lateinit var regSignInButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        Log.i(TAG, "onCreate is called")

        HideSystemUi.hideSystemUi(this)

        //ProgressBar
        progressBar = findViewById(R.id.progressBar)

        //Regular SignIn button
        regSignInButton = findViewById(R.id.btnRegSignIn)
        regSignInButton.setOnClickListener {
//            val intent = Intent(this@LoginActivity, HomePage::class.java)
//            startActivity(intent)
            Toast.makeText(this,"NOT implemented yet",Toast.LENGTH_SHORT).show()
            Log.i(TAG, "Sign out")
            signOut()
        }

        //Google SignIn button
        googleSignInBtn = findViewById(R.id.signInBtn)
        googleSignInBtn.setOnClickListener {
            Log.i(TAG, "Google sign in button was click by user")
            progressBar.visibility = View.VISIBLE
            signIn()
        }


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(Config.current.YOUTUBE_AUTH_READONLY))
            .requestScopes(Scope(Config.current.YOUTUBE_AUTH_UPLOAD))
            .requestIdToken(Constants().APP_CLIENT_ID)
            .requestServerAuthCode(Constants().APP_CLIENT_ID)
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        // ...
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.

    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "onStart is called to check if user is already sign in with google auth")
        //this section of code is to check if user is already sign in
        val account = GoogleSignIn.getLastSignedInAccount(this)
        //updateUI(account) fun to start an intent to next view
    }

    private fun getIdToken() {
        // Show an account picker to let the user choose a Google account from the device.
        // If the GoogleSignInOptions only asks for IDToken and/or profile and/or email then no
        // consent screen will be shown here.
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_GET_TOKEN)
    }

    private fun refreshIdToken() {
        // Attempt to silently refresh the GoogleSignInAccount. If the GoogleSignInAccount
        // already has a valid token this method may complete immediately.
        //
        // If the user has not previously signed in on this device or the sign-in has expired,
        // this asynchronous branch will attempt to sign in the user silently and get a valid
        // ID token. Cross-device single sign on will occur in this branch.
        mGoogleSignInClient.silentSignIn()
            .addOnCompleteListener(
                this
            ) { task -> handleSignInResult(task) }
    }

    private fun signIn() {
        Log.i(TAG, "Function signIn is called")
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i(TAG, "onActivityResult requestCode=$requestCode and resultCode=$resultCode")

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val idToken = account?.idToken
            val deviceCode = account?.serverAuthCode

            // Signed in successfully, show authenticated UI.
            APIManager.instance.requestAccessToken(
                this,
                idToken,
                deviceCode,
                completion = { error ->
                    error?.let {
                        progressBar.visibility = View.INVISIBLE
                        Toast.makeText(this@LoginActivity,"Access token request problem not yet localiseString login activity",Toast.LENGTH_SHORT).show()
                    }

                    /**
                     * Start next activity when api call to google services is complete and success
                     * **/
                    Log.i(TAG, "Google access request is completed and successfull going to HomePage activity")
                    val intent = Intent(this@LoginActivity, HomePage::class.java)
                    progressBar.visibility = View.INVISIBLE
                    startActivity(intent)

                })
            // updateUI(account)

        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
        }
    }

    private fun signOut() {
        mGoogleSignInClient.signOut()
            .addOnCompleteListener(this) {
                Toast.makeText(this, getString(R.string.signOut_succes), Toast.LENGTH_LONG).show()
                //finish()
            }
    }

}


