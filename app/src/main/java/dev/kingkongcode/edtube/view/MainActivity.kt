package dev.kingkongcode.edtube.view

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.animation.AnimationUtils
import dev.kingkongcode.edtube.R
import dev.kingkongcode.edtube.databinding.ActivityMainBinding
import dev.kingkongcode.edtube.app.BaseActivity

/***
 * SplashScreen Activity
 * */

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding

    private companion object {
        private const val TAG = "MainActivity"
        private const val INITIAL_COUNTDOWN_TIME: Long = 850
        private const val COUNTDOWN_INTERVAL: Long = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.i(TAG, "onCreate is called")
    }

    override fun onResume() {
        super.onResume()
        titleAnimation()
        setAutomaticTransition()
    }

    private fun titleAnimation() {
        /** scale animation on title **/
        val scale = AnimationUtils.loadAnimation(this, R.anim.scale_up)
        binding.tvTitle.startAnimation(scale)
    }

    private fun setAutomaticTransition() {
        //Code section to start timer to go automatically in Login page
        val countDownTimer = object : CountDownTimer(INITIAL_COUNTDOWN_TIME, COUNTDOWN_INTERVAL) {
            override fun onTick(p0: Long) {
            }

            override fun onFinish() {
                Log.i(TAG, "CountDownTimer onFinish is called, going to LoginActivity")
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.smooth_act_transition, R.anim.smooth_act_transition)
                finish()
            }
        }

        countDownTimer.start()
    }
}