package dev.kingkongcode.edtube.controller

import android.content.res.Configuration
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

private const val TAG = "VideoViewActivity"

class VideoViewActivity : YouTubeBaseActivity() {
    private lateinit var btnBack: ImageButton

    private lateinit var ytYoutubePlayer: YouTubePlayerView
    private lateinit var onInitializedListener: YouTubePlayer.OnInitializedListener
    private var youtubeVideoID: String? = null
    private var allVideoIDStr: ArrayList<String>? = null

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
            youtubeVideoID = extras.getString("youtubeVideoID")
            //to retrieve list of string id
            allVideoIDStr = extras.getStringArrayList("playAll")
        }

        initiate()
    }

    @Override
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    private fun initiate() {
        Log.i(TAG, "Function initiate is called")

        val playbackEventListener = object: YouTubePlayer.PlaybackEventListener {
            override fun onSeekTo(p0: Int) {
            }

            override fun onBuffering(p0: Boolean) {
            }

            override fun onPlaying() { Log.i(TAG,"Good, video is playing ok") }

            override fun onStopped() { Log.i(TAG,"Video has stopped") }

            override fun onPaused() { Log.i(TAG,"Video has paused") }
        }

        val playerStateChangeListener = object: YouTubePlayer.PlayerStateChangeListener {
            override fun onAdStarted() { Log.i(TAG,"Click Ad now, make the video creator rich!") }

            override fun onLoading() {
            }

            override fun onVideoStarted() { Log.i(TAG,"Video has started") }

            override fun onLoaded(p0: String?) {
            }

            override fun onVideoEnded() { Log.i(TAG,"Congratulations! You've completed another video.") }

            override fun onError(p0: YouTubePlayer.ErrorReason?) { Log.i(TAG,"error") }
        }

        onInitializedListener =  object : YouTubePlayer.OnInitializedListener {
            override fun onInitializationSuccess(
                p0: YouTubePlayer.Provider?,
                youtubePlayer: YouTubePlayer?,
                p2: Boolean
            ) {
                //Code section where to launch video
                youtubeVideoID?.let {
                    youtubePlayer?.loadVideo(youtubeVideoID) /** launch automatically when it's single video **/
                }

                allVideoIDStr?.let {
                    youtubePlayer?.cueVideos(allVideoIDStr) /** do not launch automatically when it's all list of videos **/
                }

                youtubePlayer?.setPlayerStateChangeListener(playerStateChangeListener)
                youtubePlayer?.setPlaybackEventListener(playbackEventListener)
            }

            override fun onInitializationFailure(
                p0: YouTubePlayer.Provider?,
                p1: YouTubeInitializationResult?
            ) {
                Toast.makeText(
                    this@VideoViewActivity,
                    getString(R.string.youtube_video_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        ytYoutubePlayer.initialize(Config.current.API_KEY, onInitializedListener)
    }
}