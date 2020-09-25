package dev.kingkongcode.edtube.controller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
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

    private lateinit var btnBack: ImageButton

    private lateinit var ytYoutubePlayer: YouTubePlayerView
    private lateinit var onInitializedListener: YouTubePlayer.OnInitializedListener
    private lateinit var youtubeVideoID: String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_view)

        HideSystemUi.hideSystemUi(this)

        //Back button
        btnBack = findViewById(R.id.btnBack)
        //YouTube player view
        ytYoutubePlayer = findViewById(R.id.ytYoutubePlayer)

        val extras: Bundle? = intent.extras
        if (extras != null) {
            youtubeVideoID = extras.getString("youtubeVideoID")!!
        }

        initiate()
    }

    private fun initiate() {

        btnBack.setOnClickListener { onBackPressed() }

        onInitializedListener =  object : YouTubePlayer.OnInitializedListener{
            override fun onInitializationSuccess(
                p0: YouTubePlayer.Provider?,
                youtubePlayer: YouTubePlayer?,
                p2: Boolean
            ) {
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