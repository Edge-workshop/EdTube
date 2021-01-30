package dev.kingkongcode.edtube.controller

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.transition.Fade
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import dev.kingkongcode.edtube.R
import dev.kingkongcode.edtube.databinding.ActivityLoginBinding
import dev.kingkongcode.edtube.server.APIManager
import dev.kingkongcode.edtube.server.Config
import dev.kingkongcode.edtube.util.BaseActivity

class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var mGoogleSignInClient : GoogleSignInClient

    private companion object {
        private const val TAG = "LoginActivity"
        private const val RC_SIGN_IN = 0
        private const val RC_GET_TOKEN = 90
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.i(TAG, "onCreate is called")

        initGoogleSignIn()
        //obligatory check to make sure we're on 21+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val fade = Fade()
            window.enterTransition = fade
            window.exitTransition = fade
        }

        settingAllButtons()
    }

    override fun onStart() {
        super.onStart()
        showElementOnScreen()
        //Log.i(TAG, "onStart is called to check if user is already sign in with google auth")
        //this section of code is to check if user is already sign in
        //val account = GoogleSignIn.getLastSignedInAccount(this)
        //updateUI(account) fun to start an intent to next view
    }

    private fun initGoogleSignIn() {
        //Google SignIn button
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(Config.YOUTUBE_AUTH_READONLY))
            .requestScopes(Scope(Config.YOUTUBE_AUTH_UPLOAD))
            .requestIdToken(Config.CLIENT_ID)
            .requestServerAuthCode(Config.CLIENT_ID)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        // ...
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
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

    private fun settingAllButtons() {
        binding.btnRegSignIn.setOnClickListener {
            //val intent = Intent(this@LoginActivity, HomePage::class.java)
            //startActivity(intent)
            Toast.makeText(this, "NOT implemented yet", Toast.LENGTH_SHORT).show()
            Log.i(TAG, "Sign out")
            //TODO don't forget to remove sign out fun
            signOut()
        }

        binding.googleSignInBtn.setOnClickListener {
            Log.i(TAG, "Google sign in button was click by user")
            binding.progressBar.visibility = View.VISIBLE
            signIn()
        }
    }

    private fun signIn() {
        Log.i(TAG, "Function signIn is called")
        widgetElementIsActive(false)
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    //TODO don't forget to remove func no sign out in this page only for test
    private fun signOut() {
        mGoogleSignInClient.signOut()
            .addOnCompleteListener(this) {
                Toast.makeText(this, getString(R.string.signOut_success), Toast.LENGTH_LONG).show()
                //finish()
            }
    }

    private fun showImageTransition() {
        //setup element for view transition
        val text = binding.tvTitle
        val imagePair = androidx.core.util.Pair.create(text as View, "appTitle")

        //Check if we're running on Android 5.0 or higher (API 21)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Apply activity transition
            hideElementOnScreen()
            val option = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this@LoginActivity,
                imagePair
            )
            val intent = Intent(this@LoginActivity, HomePageActivity::class.java)
            ActivityCompat.startActivity(this@LoginActivity, intent, option.toBundle())
        } else {
            val intent = Intent(this@LoginActivity, HomePageActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showElementOnScreen() {
        binding.etUsername.visibility = View.VISIBLE
        binding.etPassword.visibility = View.VISIBLE
        binding.tvOR.visibility = View.VISIBLE
        binding.btnRegSignIn.visibility = View.VISIBLE
        binding.googleSignInBtn.visibility = View.VISIBLE
    }

    private fun hideElementOnScreen() {
        binding.etUsername.visibility = View.INVISIBLE
        binding.etPassword.visibility = View.INVISIBLE
        binding.tvOR.visibility = View.INVISIBLE
        binding.btnRegSignIn.visibility = View.INVISIBLE
        binding.googleSignInBtn.visibility = View.INVISIBLE
    }

    private fun widgetElementIsActive(isActive: Boolean) {
        if (isActive) {
            binding.googleSignInBtn.isClickable = true
            binding.btnRegSignIn.isClickable = true
        } else {
            binding.googleSignInBtn.isClickable = false
            binding.btnRegSignIn.isClickable = false
        }
    }

    //TODO  to put a dialog box protection when user is trying to log in without internet connection
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i(TAG, "onActivityResult requestCode= $requestCode and resultCode= $resultCode")
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
                        binding.progressBar.visibility = View.INVISIBLE
                        Toast.makeText(this@LoginActivity, error, Toast.LENGTH_SHORT).show()
                    }

                    /**
                     * Start next activity when api call to google services is complete and success
                     * **/
                    Log.i(
                        TAG,
                        "Google access request is completed and successful going to HomePage activity"
                    )

                    binding.progressBar.visibility = View.INVISIBLE
                    showImageTransition()
                    widgetElementIsActive(true)
                })
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
        }

        binding.progressBar.visibility = View.INVISIBLE
    }
}


