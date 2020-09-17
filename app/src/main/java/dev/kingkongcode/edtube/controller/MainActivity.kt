package dev.kingkongcode.edtube.controller

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import dev.kingkongcode.edtube.R
import dev.kingkongcode.edtube.util.HideSystemUi

class MainActivity : AppCompatActivity() {
    /***
     * SplashScreen Activity
     * */

    private val TAG = "MainActivity"

    private lateinit var countDownTimer: CountDownTimer
    private val initialCountDownTime: Long = 2000
    private val countDownInterval: Long = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG,"onCreate is called")

        setContentView(R.layout.activity_main)

        //hideSystemUi()
        HideSystemUi.hideSystemUi(this)

        //Code section to start timer to go automatically in Login page
        countDownTimer = object : CountDownTimer(initialCountDownTime, countDownInterval){
            override fun onTick(p0: Long) {

            }
            override fun onFinish() {
                Log.i(TAG,"CountDownTimer onFinish is called, going to LoginActivity")
                val intent = Intent(this@MainActivity,LoginActivity::class.java)
                startActivity(intent)
            }
        }
        countDownTimer.start()
    }

}