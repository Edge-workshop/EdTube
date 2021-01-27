package dev.kingkongcode.edtube.controller

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import dev.kingkongcode.edtube.R
import dev.kingkongcode.edtube.databinding.ActivitySelectedPListDetailsBinding
import dev.kingkongcode.edtube.model.ETUser
import dev.kingkongcode.edtube.dialogs.MyCustomDialog
import dev.kingkongcode.edtube.model.PlaylistItem
import dev.kingkongcode.edtube.server.APIManager
import dev.kingkongcode.edtube.util.BaseActivity
import dev.kingkongcode.edtube.util.ConvertDurationIsoToString
import java.util.*
import kotlin.collections.ArrayList

class SelectedPListDetailsActivity : BaseActivity() {
    private lateinit var binding: ActivitySelectedPListDetailsBinding
    private lateinit var etUser: ETUser

    private lateinit var youtubeVideoID: String

    private var mPlayList = arrayListOf<PlaylistItem>()
    private lateinit var playlistAdapter: SelectedPListDetailsActivity.PlaylistAdapter

    /**to keep user in this activity after clicking no in LogOutDialog. But bottom navigation will go
     * back on home item selected **/
    private var isExiting = true

    private companion object {
        private const val TAG = "SelectedPListDetails"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectedPListDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.i(TAG,"onCreate is called")

        binding.progressBar.visibility = View.VISIBLE
        settingPlaylistVideo()
        retrieveAndDisplayUserData()
        settingClickListener()
        settingBottomNavigation()
    }

    @SuppressLint("SetTextI18n")
    private fun retrieveAndDisplayUserData() {
        var videoNbr: String
        val extras: Bundle? = intent.extras
        extras?.let { xtra ->
            //Getting user info
            etUser = xtra.getParcelable("ETUser")!!
            //Code to display the right String in user lang
            val completeStringTitle = if (Locale.getDefault().isO3Language == "eng") {
                "${etUser.firstName}' s Playlist."
            } else "La Playlist à ${etUser.firstName}."
            binding.tvPlaylistTitle.text = completeStringTitle

            //Code to retrieve profile pic from google sign in or else default pic
            Glide.with(this).load(etUser.userPhoto).
            diskCacheStrategy(DiskCacheStrategy.NONE).
            error(R.drawable.profile_pic_na).into(binding.ivProfilePic)

            binding.ivProfilePic.setOnClickListener {
                Log.i(TAG,"User click on profile icon custom dialog show")
                MyCustomDialog(etUser).show(supportFragmentManager,"MyCustomFragment")
            }

            //Retrieving user selected list id
            requestSelectedPList(extras.getString("selectedListID")!!)
            extras.getString("videoNbr")!!.also { videoNbr = it }
            val firstPartText = getString(R.string.number_of_video)
            (firstPartText+"\t\t"+videoNbr).also { firstPart -> binding.tvNbrOfVideo.text = firstPart }
        }
    }

    private fun settingPlaylistVideo() {
        //Code section initiate RecycleView and PlaylistAdapter
        val linearLayoutManager = LinearLayoutManager(this@SelectedPListDetailsActivity)
        binding.rvListVideo.layoutManager = linearLayoutManager
        playlistAdapter = PlaylistAdapter(this.mPlayList)
        binding.rvListVideo.adapter = playlistAdapter
    }

    private fun requestSelectedPList(selectedID: String) {
        Log.i(TAG,"Function requestSelectedPList was called")
        lateinit var tempPlayList: ArrayList<PlaylistItem>

        APIManager.instance.requestSelectedPlaylistDetails(this@SelectedPListDetailsActivity, selectedID, null, completion = { error, selectedPList ->
            Log.i(TAG,"APIManager requestSelectedPlaylistDetails response receive in activity")

            error?.let { errorMsg -> Toast.makeText(this@SelectedPListDetailsActivity, errorMsg, Toast.LENGTH_SHORT).show() }

            //Code section where we populate adapter with api response data
            selectedPList?.let { xSelectedPList ->
                tempPlayList = xSelectedPList.clone() as ArrayList<PlaylistItem>
                val strVideoIdList = arrayListOf<String>()
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

                        playlistAdapter.update(tempPlayList)
                        //Code section where we initiate first thumbnails and store youtube video id that is link with thumbnail
                        if (tempPlayList[0].snippet.thumbnails.high.url.isNotEmpty()){
                            Glide.with(this@SelectedPListDetailsActivity).load(tempPlayList[0].snippet.thumbnails.high.url).into(binding.ivSelectedThumbnail)
                        }

                        binding.ivSinglePlay.visibility = View.VISIBLE
                        youtubeVideoID = tempPlayList[0].snippet.resourceId.videoId
                    }
                })
            }
        })
    }

    private fun settingClickListener() {
        //Code section when user click into main thumbnail image
        binding.ivSelectedThumbnail.setOnClickListener {
            val intent = Intent(this@SelectedPListDetailsActivity,VideoViewActivity::class.java)
            intent.putExtra("youtubeVideoID",youtubeVideoID)
            startActivity(intent)
        }

        binding.ibPlayAllBtn.setOnClickListener {
            val allVideoId = arrayListOf<String>()
            for (videoId in this.mPlayList){
                allVideoId.add(videoId.snippet.resourceId.videoId)
            }

            val intent = Intent(this@SelectedPListDetailsActivity,VideoViewActivity::class.java)
            intent.putExtra("playAll",allVideoId)
            startActivity(intent)
        }
    }

    private fun settingBottomNavigation() {
        //Code section for Bottom Navigation menu item
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.home_page_menu_home -> {
                    if (isExiting) {
                        val intent = Intent(this@SelectedPListDetailsActivity,HomePageActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else isExiting = true

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

    private fun showLogOutDialog() {
        // build alert dialog
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.apply {
            // set message of alert dialog
            dialogBuilder.setMessage("Do you want to close this application ?")
            setCancelable(false)
            setPositiveButton("YES") { dialog, id ->
                signOut()
            }
            setNegativeButton("NO") { dialog, id ->
                isExiting = false
                binding.bottomNavigation.selectedItemId = R.id.home_page_menu_home
                dialog.cancel()
            }
            create()
            setTitle("EdTube")
            show()
        }
    }

    private fun signOut() {
        Log.i(TAG,"Function signOut was called")
        lateinit var mGoogleSignInClient : GoogleSignInClient
        mGoogleSignInClient.signOut()
            .addOnCompleteListener(this) {
                Toast.makeText(this, getString(R.string.signOut_success), Toast.LENGTH_LONG).show()
                finish()
            }
    }

    private inner class PlaylistAdapter(private var dataSet: MutableList<PlaylistItem>) : RecyclerView.Adapter<SelectedPListDetailsActivity.PlaylistAdapter.VideoViewHolder>() {
        private var oldIndexSelected = 0
        private var isJustStarted = true

        private inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var tvTrackNumber: TextView = itemView.findViewById(R.id.tvTrackNumber)
            var tvTrackTitle: TextView = itemView.findViewById(R.id.tvTrackTitle)
            var tvDuration: TextView = itemView.findViewById(R.id.tvDuration)

            fun bind(video: PlaylistItem, position: Int) {
                tvTrackNumber.text = (position + 1).toString()
                tvTrackTitle.text = video.snippet.title
                tvDuration.text = ConvertDurationIsoToString.convert(video.duration)
            }
        }

        @Override
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : PlaylistAdapter.VideoViewHolder {
            val layout =  R.layout.video_details_row
            val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
            binding.progressBar.visibility = View.INVISIBLE
            return VideoViewHolder(view)
        }

        @Override
        override fun getItemCount() = dataSet.size

        fun update(updatedList: MutableList<PlaylistItem>) {
            dataSet.clear()
            dataSet.addAll(updatedList)
            notifyDataSetChanged()
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
                if (video.snippet.thumbnails.high.url.isNotEmpty()) {
                    Glide.with(holder.itemView.context).load(video.snippet.thumbnails.high.url).into(binding.ivSelectedThumbnail)
                    youtubeVideoID = video.snippet.resourceId.videoId
                }

                //Code section to reset proper value in data set in adapter
                dataSet[oldIndexSelected].isVideoSelected = false
                dataSet[position].isVideoSelected = true
                oldIndexSelected = position
                notifyDataSetChanged()
            }
        }
    }
}
