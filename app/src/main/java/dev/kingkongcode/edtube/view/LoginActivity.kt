package dev.kingkongcode.edtube.view

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.transition.Fade
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import dev.kingkongcode.edtube.R
import dev.kingkongcode.edtube.databinding.ActivityLoginBinding
import dev.kingkongcode.edtube.app.BaseActivity
import dev.kingkongcode.edtube.viewmodel.LoginViewModel
import dev.kingkongcode.edtube.viewmodel.LoginViewModelFactory

class LoginActivity : BaseActivity() {
    private lateinit var viewBinding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate()")
        viewBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewModel = ViewModelProviders.of(this, LoginViewModelFactory(application)).get(LoginViewModel::class.java)
        viewModel.initGoogleSignIn()

        //obligatory check to make sure we're on 21+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val fade = Fade()
            window.enterTransition = fade
            window.exitTransition = fade
        }

        configAllButtons()
    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "onStart()")
        showElementOnScreen()
        //Log.i(TAG, "onStart is called to check if user is already sign in with google auth")
        //this section of code is to check if user is already sign in
        //val account = GoogleSignIn.getLastSignedInAccount(this)
        //updateUI(account) fun to start an intent to next view
        viewModel.success.observe(this, { success ->
            if (success) {
                showImageTransition()
                widgetElementIsActive(true)
            }
        })

        viewModel.loading.observe(this, { isLoading ->
            if (isLoading) {
                viewBinding.progressBar.visibility = View.VISIBLE
            } else viewBinding.progressBar.visibility = View.INVISIBLE
        })

        viewModel.errorMsg.observe(this, Observer { errorMsg ->
            errorMsg?.let { strError ->
                Toast.makeText(this@LoginActivity, strError, Toast.LENGTH_LONG).show()
                viewModel.clearErrorMsg()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume()")
    }



//    private fun getIdToken() {
//        // Show an account picker to let the user choose a Google account from the device.
//        // If the GoogleSignInOptions only asks for IDToken and/or profile and/or email then no
//        // consent screen will be shown here.
//        val signInIntent = mGoogleSignInClient.signInIntent
//        startActivityForResult(signInIntent, RC_GET_TOKEN)
//    }
//
//    private fun refreshIdToken() {
//        // Attempt to silently refresh the GoogleSignInAccount. If the GoogleSignInAccount
//        // already has a valid token this method may complete immediately.
//        //
//        // If the user has not previously signed in on this device or the sign-in has expired,
//        // this asynchronous branch will attempt to sign in the user silently and get a valid
//        // ID token. Cross-device single sign on will occur in this branch.
//        mGoogleSignInClient.silentSignIn()
//            .addOnCompleteListener(
//                this
//            ) { task -> handleSignInResult(task) }
//    }

    private fun configAllButtons() {
        Log.i(TAG, "configAllButtons()")
        viewBinding.btnRegSignIn.setOnClickListener {
            signOut(this@LoginActivity)
        }

        viewBinding.googleSignInBtn.setOnClickListener {
            Log.i(TAG, "Google sign in button was click by user")
            viewBinding.progressBar.visibility = View.VISIBLE
            signIn()
        }
    }

    private fun signIn() {
        Log.i(TAG, "signIn()")
        widgetElementIsActive(false)
        val signInIntent = viewModel.mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun signOut(activity: Activity) {
        Log.i(TAG, "signOut()")
        viewModel.mGoogleSignInClient.signOut()
            .addOnCompleteListener(activity) {
                Toast.makeText(activity.baseContext, activity.getString(R.string.signOut_success) , Toast.LENGTH_LONG).show()
                //finish()
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
//            handleSignInResult(viewModel.getGoogleSignInData(data))
            viewModel.googleSignInData(data)
        }
    }

    private fun showImageTransition() {
        Log.i(TAG, "showImageTransition()")
        //setup element for view transition
        val text = viewBinding.tvTitle
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
        Log.i(TAG, "showElementOnScreen()")
        viewBinding.etUsername.visibility = View.VISIBLE
        viewBinding.etPassword.visibility = View.VISIBLE
        viewBinding.tvOR.visibility = View.VISIBLE
        viewBinding.btnRegSignIn.visibility = View.VISIBLE
        viewBinding.googleSignInBtn.visibility = View.VISIBLE
    }

    private fun hideElementOnScreen() {
        Log.i(TAG, "hideElementOnScreen()")
        viewBinding.etUsername.visibility = View.INVISIBLE
        viewBinding.etPassword.visibility = View.INVISIBLE
        viewBinding.tvOR.visibility = View.INVISIBLE
        viewBinding.btnRegSignIn.visibility = View.INVISIBLE
        viewBinding.googleSignInBtn.visibility = View.INVISIBLE
    }

    private fun widgetElementIsActive(isActive: Boolean) {
        Log.i(TAG, "widgetElementIsActive($isActive)")
        if (isActive) {
            viewBinding.googleSignInBtn.isClickable = true
            viewBinding.btnRegSignIn.isClickable = true
        } else {
            viewBinding.googleSignInBtn.isClickable = false
            viewBinding.btnRegSignIn.isClickable = false
        }
    }

    private companion object {
        private const val TAG = "LoginActivity"
        private const val RC_SIGN_IN = 0
        private const val RC_GET_TOKEN = 90
    }
}


