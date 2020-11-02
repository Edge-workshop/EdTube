package dev.kingkongcode.edtube.controller

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.transition.Fade
import android.util.Log
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
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
import dev.kingkongcode.edtube.util.BaseActivity

private const val TAG = "LoginActivity"
private const val RC_SIGN_IN = 0
private const val RC_GET_TOKEN = 90

class LoginActivity : BaseActivity() {
    private lateinit var xMainView: ConstraintLayout

    private lateinit var progressBar: ProgressBar
    private lateinit var tvTitle: TextView

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var googleSignInBtn: SignInButton
    private lateinit var tvOR: TextView
    private lateinit var mGoogleSignInClient : GoogleSignInClient
    private lateinit var regSignInButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        Log.i(TAG, "onCreate is called")

        xMainView = findViewById(R.id.xMainView)
        //Title
        tvTitle = findViewById(R.id.tvTitle)
        //ProgressBar
        progressBar = findViewById(R.id.progressBar)
        //Edit text for username
        etUsername = findViewById(R.id.etUsername)
        //Edit text for password
        etPassword = findViewById(R.id.etPassword)
        //OR text
        tvOR = findViewById(R.id.tvOR)
        //Regular SignIn button
        regSignInButton = findViewById(R.id.btnRegSignIn)

        //Google SignIn button
        googleSignInBtn = findViewById(R.id.signInBtn)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(Config.current.YOUTUBE_AUTH_READONLY))
            .requestScopes(Scope(Config.current.YOUTUBE_AUTH_UPLOAD))
            .requestIdToken(Config.current.CLIENT_ID)
            .requestServerAuthCode(Config.current.CLIENT_ID)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        // ...
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.

        //obligatory check to make sure we're on 21+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val fade = Fade()
            window.enterTransition = fade
            window.exitTransition = fade
        }

        initiate()
    }

    override fun onStart() {
        super.onStart()
        showElementOnScreen()
        //Log.i(TAG, "onStart is called to check if user is already sign in with google auth")
        //this section of code is to check if user is already sign in
        //val account = GoogleSignIn.getLastSignedInAccount(this)
        //updateUI(account) fun to start an intent to next view
    }

    override fun onResume() {
        super.onResume()
    }

    private fun initiate() {
        regSignInButton.setOnClickListener {
//            val intent = Intent(this@LoginActivity, HomePage::class.java)
//            startActivity(intent)
            Toast.makeText(this, "NOT implemented yet", Toast.LENGTH_SHORT).show()
            Log.i(TAG, "Sign out")
            //TODO don't forget to remove sign out fun
            signOut()
        }

        googleSignInBtn.setOnClickListener {
            Log.i(TAG, "Google sign in button was click by user")
            progressBar.visibility = View.VISIBLE
            signIn()
        }
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
                        progressBar.visibility = View.INVISIBLE
                        Toast.makeText(this@LoginActivity, error, Toast.LENGTH_SHORT).show()
                    }

                    /**
                     * Start next activity when api call to google services is complete and success
                     * **/
                    Log.i(
                        TAG,
                        "Google access request is completed and successfull going to HomePage activity"
                    )
//                    val intent = Intent(this@LoginActivity, HomePage::class.java)
//                    progressBar.visibility = View.INVISIBLE
//                    startActivity(intent)

                    progressBar.visibility = View.INVISIBLE
                    showImageTransition(xMainView, tvTitle)
                })
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

    private fun showImageTransition(view: View, text: TextView) {
        //setup element for view transition
        val text = view.findViewById<TextView>(R.id.tvTitle)
        val imagePair = androidx.core.util.Pair.create(text as View, "appTitle")

//        val text2 = view.findViewById<TextView>(R.id.tvAppTitle)
//        val imagePair2 = androidx.core.util.Pair.create(text2 as View, "appTitle")

//        Check if we're running on Android 5.0 or higher (API 21)
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
            // Swap without transition
        }
    }

    fun showElementOnScreen() {
        etUsername.visibility = View.VISIBLE
        etPassword.visibility = View.VISIBLE
        tvOR.visibility = View.VISIBLE
        regSignInButton.visibility = View.VISIBLE
        googleSignInBtn.visibility = View.VISIBLE
    }

    fun hideElementOnScreen() {
        etUsername.visibility = View.INVISIBLE
        etPassword.visibility = View.INVISIBLE
        tvOR.visibility = View.INVISIBLE
        regSignInButton.visibility = View.INVISIBLE
        googleSignInBtn.visibility = View.INVISIBLE
    }
}


