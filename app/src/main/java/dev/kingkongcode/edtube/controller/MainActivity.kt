package dev.kingkongcode.edtube.controller

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import dev.kingkongcode.edtube.R

class MainActivity : AppCompatActivity() {
    /***
     * SplashScreen Activity
     * */

    private val TAG = "MainActivity"
    private lateinit var tvTitle: TextView

    private lateinit var countDownTimer: CountDownTimer
    private val initialCountDownTime: Long = 850
    private val countDownInterval: Long = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i(TAG, "onCreate is called")

        //App Title
        tvTitle = findViewById(R.id.tvTitle)
        /** scale animtion on title **/
        val scale = AnimationUtils.loadAnimation(this, R.anim.scale_up)
        tvTitle.startAnimation(scale)

        //Code section to start timer to go automatically in Login page
        countDownTimer = object : CountDownTimer(initialCountDownTime, countDownInterval){
            override fun onTick(p0: Long) {}
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