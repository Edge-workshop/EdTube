package dev.kingkongcode.edtube.controller

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import dev.kingkongcode.edtube.R
import dev.kingkongcode.edtube.model.ETUser
import dev.kingkongcode.edtube.dialogs.MyCustomDialog
import dev.kingkongcode.edtube.model.PlaylistItem
import dev.kingkongcode.edtube.server.APIManager
import dev.kingkongcode.edtube.util.BaseActivity
import dev.kingkongcode.edtube.util.Constants
import dev.kingkongcode.edtube.util.ConvertDurationIsoToString
import java.time.Period
import java.util.*
import kotlin.collections.ArrayList
import kotlin.time.Duration

private const val TAG = "SelectedPListDetails"

class SelectedPListDetailsActivity : BaseActivity() {
    private lateinit var progressBar: ProgressBar

    private lateinit var mGoogleSignInClient : GoogleSignInClient
    private lateinit var etUser: ETUser
    private lateinit var ivProfilePic: ImageView

    private lateinit var tvPlaylistTitle: TextView
    private lateinit var tvNbrOfVideo: TextView
    private lateinit var ibPlayAllBtn: ImageButton
    private lateinit var ivSelectedThumbnail: ImageView
    private lateinit var ivSinglePlay: ImageView

    private lateinit var youtubeVideoID: String

    private lateinit var rvListVideo: RecyclerView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private var mPlayList = arrayListOf<PlaylistItem>()
    private var tempPlayList = arrayListOf<PlaylistItem>()
    private lateinit var playlistAdapter: SelectedPListDetailsActivity.PlaylistAdapter

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selected_p_list_details)
        Log.i(TAG,"onCreate is called")

        //Progressbar
        progressBar = findViewById(R.id.progressBar)
        //Profile photo
        ivProfilePic = findViewById(R.id.ivProfileAvatar)
        //Main Selected List Title
        tvPlaylistTitle = findViewById(R.id.tvPlaylistTitle)
        //Number of video text
        tvNbrOfVideo = findViewById(R.id.tvNbrOfVideo)
        //Play all button
        ibPlayAllBtn = findViewById(R.id.ibPlayBtn)
        //Main selected thumbnail
        ivSelectedThumbnail = findViewById(R.id.ivSelectedThumbnail)
        //Single Play
        ivSinglePlay = findViewById(R.id.ivSinglePlay)

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
        Log.i(TAG,"onResume is called")
    }

    private fun initiate() {
        Log.i(TAG,"Function initiate is called")
        progressBar.visibility = View.VISIBLE

        //Code section initiate RecycleView and PlaylistAdapter
        playlistAdapter = PlaylistAdapter(this@SelectedPListDetailsActivity, this.mPlayList)
        rvListVideo.adapter = playlistAdapter

        var videoNbr: String
        val extras: Bundle? = intent.extras
        if (extras != null) {
            //Getting user info
            etUser = extras.getParcelable<ETUser>("ETUser")!!
            //Code to display the right String in user lang
            val completeStringTitle = if (Locale.getDefault().isO3Language == "eng") {
                "${etUser.firstName}' s Playlist."
            } else "La Playlist à ${etUser.firstName}."
            tvPlaylistTitle.text = completeStringTitle

            //Code to retreive profile pic from google sign in or else default pic
            Glide.with(this).load(etUser.userPhoto).
            diskCacheStrategy(DiskCacheStrategy.NONE).
            error(R.drawable.profile_pic_na).into(ivProfilePic)

            ivProfilePic.setOnClickListener {
                Log.i(TAG,"User click on profil icon custom dialog show")
                MyCustomDialog(etUser,this@SelectedPListDetailsActivity).show(supportFragmentManager,"MyCustomFragment")
            }

            //Retrieving user selected list id
            requestSelectedPList(extras.getString("selectedListID")!!)
            videoNbr = extras.getString("videoNbr")!!
            val firstPartText = getString(R.string.number_of_video)
            tvNbrOfVideo.text = firstPartText+"\t\t"+videoNbr
        }

        //Code section when user click into main thumbnail image
        ivSelectedThumbnail.setOnClickListener {
            val intent = Intent(this@SelectedPListDetailsActivity,VideoViewActivity::class.java)
            intent.putExtra("youtubeVideoID",youtubeVideoID)
            startActivity(intent)
        }

        ibPlayAllBtn.setOnClickListener {
            val allVideoId = arrayListOf<String>()
            for (videoId in this.mPlayList){
                allVideoId.add(videoId.snippet.resourceId.videoId)
            }

            val intent = Intent(this@SelectedPListDetailsActivity,VideoViewActivity::class.java)
            intent.putExtra("playAll",allVideoId)
            startActivity(intent)
        }

        //Code section for Bottom Navigation menu item
        bottomNavigation.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.home_page_menu_home -> {
                    val intent = Intent(this@SelectedPListDetailsActivity,HomePageActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }

                R.id.home_page_menu_search -> {
                    val intent = Intent(this@SelectedPListDetailsActivity,SearchVideoActivity::class.java)
                    intent.putExtra("ETUser",etUser)
                    startActivity(intent)
                    finish()
                    true
                }

                R.id.home_page_menu_log_out -> {
                    showLogOutDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun requestSelectedPList(selectedID: String) {
        Log.i(TAG,"Function requestSelectedPList was called")

        APIManager.instance.requestSelectedPlaylistDetails(this@SelectedPListDetailsActivity,selectedID, null, completion = { error, selectedPList ->
            Log.i(TAG,"APIManager requestSelectedPlaylistDetails response receive in activity")

            error?.let { Toast.makeText(this@SelectedPListDetailsActivity,error,Toast.LENGTH_SHORT).show() }

            //Code section where we populate adapter with api response data
            selectedPList?.let { xSelectedPList ->
                tempPlayList = xSelectedPList.clone() as ArrayList<PlaylistItem>
                var strVideoIdList = arrayListOf<String>()
                for (videoStrId in tempPlayList ) {
                    strVideoIdList.add(videoStrId.snippet.resourceId.videoId)
                }

                APIManager.instance.requestGetVideoDuration(this@SelectedPListDetailsActivity, strVideoIdList, completion = {error, durationVideoList ->
                    error?.let { Toast.makeText(this@SelectedPListDetailsActivity, it, Toast.LENGTH_LONG).show() }

                    durationVideoList?.let {
                        //Code section where we get list from API server and to match video id from user selected list from first API call
                        for (durationPairObj in it) {
                            for (videoStrId in tempPlayList) {
                                if (durationPairObj.first == videoStrId.snippet.resourceId.videoId) {
                                    videoStrId.duration = durationPairObj.second
                                }
                            }
                        }

                        this.mPlayList.clear()
                        this.mPlayList.addAll(tempPlayList)
                        rvListVideo.adapter?.notifyDataSetChanged()
                        //Code section where we initiate first thumbnails and store youtube video id that is link with thumbnail
                        if (!tempPlayList[0].snippet.thumbnails.high.url.isNullOrEmpty()){
                            Glide.with(this@SelectedPListDetailsActivity).load(tempPlayList[0].snippet.thumbnails.high.url).into(ivSelectedThumbnail)
                        }

                        ivSinglePlay.visibility = View.VISIBLE
                        youtubeVideoID = tempPlayList[0].snippet.resourceId.videoId
                    }
                })
            }
        })
    }

    private fun showLogOutDialog() {
        // build alert dialog
        val dialogBuilder = AlertDialog.Builder(this)
        // set message of alert dialog
        dialogBuilder.setMessage("Do you want to close this application ?")
            // if the dialog is cancelable
            .setCancelable(false)
            // positive button text and action
            .setPositiveButton("YES", DialogInterface.OnClickListener {
                    dialog, id -> signOut()
            })
            // negative button text and action
            .setNegativeButton("NO", DialogInterface.OnClickListener {
                    dialog, id -> dialog.cancel()
                bottomNavigation.selectedItemId = R.id.home_page_menu_home
            })

        // create dialog box
        val alert = dialogBuilder.create()
        // set title for alert dialog box
        alert.setTitle("EdTube")
        // show alert dialog
        alert.show()
    }

    private fun signOut() {
        Log.i(TAG,"Funtion signOut was called")
        mGoogleSignInClient.signOut()
            .addOnCompleteListener(this) {
                Toast.makeText(this, getString(R.string.signOut_succes), Toast.LENGTH_LONG).show()
                finish()
            }
    }

    private inner class PlaylistAdapter(private val mContext: Context, private var dataSet: List<PlaylistItem>) : RecyclerView.Adapter<SelectedPListDetailsActivity.PlaylistAdapter.VideoViewHolder>(), View.OnClickListener {
        private var oldIndexSelected = 0
        private var isJustStarted = true

        private inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var tvTrackNumber = itemView.findViewById<TextView>(R.id.tvTrackNumber)
            var tvTrackTitle = itemView.findViewById<TextView>(R.id.tvTrackTitle)
            var tvDuration = itemView.findViewById<TextView>(R.id.tvDuration)

            fun bind(video: PlaylistItem, position: Int) {
                tvTrackNumber.text = (position + 1).toString()
                tvTrackTitle.text = video.snippet.title
                tvDuration.text = ConvertDurationIsoToString.convert(video.duration)
            }
        }

        @Override
        override fun onClick(v: View) {
        }

        @Override
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : PlaylistAdapter.VideoViewHolder {
            val layout =  R.layout.video_details_row
            val view = LayoutInflater.from(mContext).inflate(layout, parent, false)
            progressBar.visibility = View.INVISIBLE
            return VideoViewHolder(view)
        }

        @Override
        override fun getItemCount(): Int {
            return dataSet.size
        }

        @Override
        override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
            //Code section to highlight first row on first creation
            if (isJustStarted) {
                dataSet[0].isVideoSelected = true
                isJustStarted = false
            }

            val video = dataSet[position]
            holder.bind(video, position)

            //Code section show user what was selected in RecycleView playlist
            if (video.isVideoSelected) {
                holder.tvTrackTitle.setTextColor(ContextCompat.getColor(this@SelectedPListDetailsActivity,R.color.greenLetter))
            } else holder.tvTrackTitle.setTextColor(ContextCompat.getColor(this@SelectedPListDetailsActivity,R.color.white))

            holder.itemView.setOnClickListener {
                Log.i(TAG,"User click on specific playlist title: ${video.snippet.title} row position: $position")

                //Code section to send image thumbnails to main view when user click on specific row
                if (!video.snippet.thumbnails.high.url.isNullOrEmpty()) {
                    Glide.with(mContext).load(video.snippet.thumbnails.high.url).into(ivSelectedThumbnail)
                    youtubeVideoID = video.snippet.resourceId.videoId
                }

                //Code section to reset proper value in dataset in adapter
                dataSet[oldIndexSelected].isVideoSelected = false
                dataSet[position].isVideoSelected = true
                oldIndexSelected = position
                notifyDataSetChanged()
            }
        }
    }
}
