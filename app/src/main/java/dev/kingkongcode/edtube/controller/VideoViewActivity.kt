package dev.kingkongcode.edtube.controller

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerView
import dev.kingkongcode.edtube.R
import dev.kingkongcode.edtube.server.Config
import dev.kingkongcode.edtube.util.HideSystemUi

class VideoViewActivity : YouTubeBaseActivity() {
    //TODO find solution to keep track on videostreaming time with savedInstanceState to keep track when changing device orientaton
    private val TAG = "VideoViewActivity"

    private lateinit var btnBack: ImageButton

    private lateinit var ytYoutubePlayer: YouTubePlayerView
    private lateinit var onInitializedListener: YouTubePlayer.OnInitializedListener
    private lateinit var youtubeVideoID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_view)
        Log.i(TAG,"onCreate is called")

        //Code section Full screen
        HideSystemUi.hideSystemUi(this)

        //Back button
        btnBack = findViewById(R.id.btnBack)
        btnBack.setOnClickListener { onBackPressed() }
        //YouTube player view
        ytYoutubePlayer = findViewById(R.id.ytYoutubePlayer)

        val extras: Bundle? = intent.extras
        if (extras != null) {
            //Code to retrieve youtubeVideoID
            youtubeVideoID = extras.getString("youtubeVideoID")!!
        }

        initiate()
    }

    private fun initiate() {
        Log.i(TAG, "Function initiate is called")

        onInitializedListener =  object : YouTubePlayer.OnInitializedListener{
            override fun onInitializationSuccess(
                p0: YouTubePlayer.Provider?,
                youtubePlayer: YouTubePlayer?,
                p2: Boolean
            ) {
                //Code section where to lauch video
                youtubePlayer?.loadVideo(youtubeVideoID)
            }

            override fun onInitializationFailure(
                p0: YouTubePlayer.Provider?,
                p1: YouTubeInitializationResult?
            ) {
                Toast.makeText(
                    this@VideoViewActivity,
                    "Something went wrong when loading video from YouTube!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        ytYoutubePlayer.initialize(Config.current.API_KEY, onInitializedListener)
    }

}