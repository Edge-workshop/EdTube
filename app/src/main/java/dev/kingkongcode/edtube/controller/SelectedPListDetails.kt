package dev.kingkongcode.edtube.controller

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerView
import dev.kingkongcode.edtube.R
import dev.kingkongcode.edtube.model.PlaylistItemActivity
import dev.kingkongcode.edtube.server.APIManager
import dev.kingkongcode.edtube.server.Config
import dev.kingkongcode.edtube.util.HideSystemUi
import dev.kingkongcode.edtube.util.PaginationList

class SelectedPListDetails : AppCompatActivity() {

    private val TAG = "SelectedPListDetails"

    private lateinit var mGoogleSignInClient : GoogleSignInClient

    private lateinit var tvPlaylistTitle: TextView
    private lateinit var tvNbrOfVideo: TextView
    private lateinit var ibPlayBtn: ImageButton
    private lateinit var ivSelectedThumbnail: ImageView

    private lateinit var youtubeVideoID: String

    private lateinit var rvListVideo: RecyclerView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var mPlayList: ArrayList<PlaylistItemActivity>
    private lateinit var playlistAdapter: SelectedPListDetails.PlaylistAdapter

    private lateinit var bottomNavigation: BottomNavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selected_p_list_details)

        HideSystemUi.hideSystemUi(this)

        //Main Selected List Title
        tvPlaylistTitle = findViewById(R.id.tvPlaylistTitle)
        //Number of video text
        tvNbrOfVideo = findViewById(R.id.tvNbrOfVideo)
        //Play all button
        ibPlayBtn = findViewById(R.id.ibPlayBtn)
        //Main selected thumbnail
        ivSelectedThumbnail = findViewById(R.id.ivSelectedThumbnail)

        //List of all video in selected list
        rvListVideo = findViewById(R.id.rvListVideo)
        linearLayoutManager = LinearLayoutManager(this)
        rvListVideo.layoutManager = linearLayoutManager
        //Bottom Navigation bar
        bottomNavigation = findViewById(R.id.bottomNavigation)

        initiate()
    }

    override fun onResume() {
        super.onResume()
        HideSystemUi.hideSystemUi(this)
    }

    private fun initiate() {

        val extras: Bundle? = intent.extras
        if (extras != null) {
            requestSelectedPList(extras.getString("selectedListID")!!)
        }

        ivSelectedThumbnail.setOnClickListener {
            val intent = Intent(this@SelectedPListDetails,VideoViewActivity::class.java)
            intent.putExtra("youtubeVideoID",youtubeVideoID)
            startActivity(intent)
        }

        bottomNavigation.setOnNavigationItemSelectedListener {
            when(it.itemId){

                R.id.home_page_menu_home -> {
                    val intent = Intent(this@SelectedPListDetails,HomePage::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.home_page_menu_search -> {
                    Toast.makeText(this, "NOT Implemented yet", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.home_page_menu_log_out -> {
                    signOut()
                    true
                }
                else -> false
            }
        }

    }

    private fun requestSelectedPList(selectedID: String){
        
        APIManager.instance.requestSelectedPlaylistDetails(this@SelectedPListDetails,selectedID, completion = { error, selectedPList ->  

            error?.let { Toast.makeText(this@SelectedPListDetails,error,Toast.LENGTH_SHORT).show() }

            selectedPList?.let {
                this.mPlayList =  it

                playlistAdapter = PlaylistAdapter(
                    this, this.mPlayList
                )
                rvListVideo.adapter = playlistAdapter

                if (!it[0].snippet.thumbnails.high.url.isNullOrEmpty()){
                    Glide.with(this).load(it[0].snippet.thumbnails.high.url).into(ivSelectedThumbnail)
                }

                youtubeVideoID = it[0].snippet.ressourceId.videoId

            }
        })

    }

    private fun signOut() {
        mGoogleSignInClient.signOut()
            .addOnCompleteListener(this) {
                Toast.makeText(this, getString(R.string.signOut_succes), Toast.LENGTH_LONG).show()
                finish()
            }
    }


    private inner class PlaylistAdapter(private val mContext: Context, private var dataSet: List<PlaylistItemActivity>) : RecyclerView.Adapter<SelectedPListDetails.PlaylistAdapter.VideoViewHolder>(), View.OnClickListener {


        private inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            var tvTrackNumber = itemView.findViewById<TextView>(R.id.tvTrackNumber)
            var tvTrackTitle = itemView.findViewById<TextView>(R.id.tvTrackTitle)
            var tvDuration = itemView.findViewById<TextView>(R.id.tvDuration)

            fun bind(video: PlaylistItemActivity, position: Int) {
                tvTrackNumber.text = (position + 1).toString()
                tvTrackTitle.text = video.snippet.title
                tvDuration.text = "N/A"
            }

        }

        @Override
        override fun onClick(v: View) {}

        @Override
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistAdapter.VideoViewHolder {
            val layout =  R.layout.video_details_row
            val view = LayoutInflater.from(mContext).inflate(layout, parent, false)
            return VideoViewHolder(view)
        }


        @Override
        override fun getItemCount(): Int {
            return dataSet.size
        }

        @Override
        override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
            val video = dataSet[position]
            (holder as VideoViewHolder).bind(video, position)

//            if (position == 0){
//                Log.i(TAG,"color text change")
//                holder.tvTrackTitle.setTextColor(ContextCompat.getColor(this@SelectedPListDetails,R.color.greenLetter))
//            }

            holder.itemView.setOnClickListener {
                if (!video.snippet.thumbnails.standard.url.isNullOrEmpty()){
                    Glide.with(mContext).load(video.snippet.thumbnails.standard.url).into(ivSelectedThumbnail)
                    youtubeVideoID = video.snippet.ressourceId.videoId
                }
            }
        }


    }




}