package dev.kingkongcode.edtube.view

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
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import dev.kingkongcode.edtube.R
import dev.kingkongcode.edtube.databinding.ActivitySelectedPListDetailsBinding
import dev.kingkongcode.edtube.dialogs.MyCustomDialog
import dev.kingkongcode.edtube.model.PlaylistItem
import dev.kingkongcode.edtube.app.server.APIManager
import dev.kingkongcode.edtube.app.BaseActivity
import dev.kingkongcode.edtube.app.ConvertDurationIsoToString
import dev.kingkongcode.edtube.viewmodel.SelectedPListDetailsViewModel
import dev.kingkongcode.edtube.viewmodel.SelectedPListViewModelFactory
import java.util.*
import kotlin.collections.ArrayList

class SelectedPListDetailsActivity : BaseActivity() {
    private lateinit var viewBinding: ActivitySelectedPListDetailsBinding
    private lateinit var viewModel: SelectedPListDetailsViewModel
    private lateinit var playlistAdapter: SelectedPListDetailsActivity.PlaylistAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivitySelectedPListDetailsBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        Log.i(TAG,"onCreate is called")

        viewModel = ViewModelProviders.of(this, SelectedPListViewModelFactory(application)).get(SelectedPListDetailsViewModel::class.java)

        viewBinding.progressBar.visibility = View.VISIBLE
        retrieveData()
        displayUserInfo()
        configAllLiveDataObserver()
        configPlaylistVideoView()
        viewModel.requestSelectedPlaylist(viewModel.selectedListId)
        configAllClickListener()
        settingBottomNavigation()
    }

    // TODO find sollution to put this block of code in ViewModel activity -> replace intent.extra with something else
    @SuppressLint("SetTextI18n")
    private fun retrieveData() {
        val extras: Bundle? = intent.extras
        extras?.let { xtra ->
            // Getting user info
            viewModel.etUser = xtra.getParcelable("ETUser")!!
            // Getting user selected list id
            viewModel.selectedListId = extras.getString("selectedListID")!!
            // Getting number of video in selected list
            extras.getString("videoNbr")!!.also { viewModel.numberOfVideo = it }
        }
    }

    private fun displayUserInfo() {
        //Code to display the right String in user lang
        val completeStringTitle = if (Locale.getDefault().isO3Language == "eng") {
            "${viewModel.etUser.firstName}' s Playlist."
        } else "La Playlist à ${viewModel.etUser.firstName}."
        viewBinding.tvPlaylistTitle.text = completeStringTitle

        //Code to retrieve profile pic from google sign in or else default pic
        Glide.with(this).load(viewModel.etUser.userPhoto).
        diskCacheStrategy(DiskCacheStrategy.NONE).
        error(R.drawable.profile_pic_na).into(viewBinding.ivProfilePic)

        val firstPartText = getString(R.string.number_of_video)
        (firstPartText+"\t\t"+viewModel.numberOfVideo).also { firstPart -> viewBinding.tvNbrOfVideo.text = firstPart }
    }

    private fun configPlaylistVideoView() {
        Log.i(TAG, "settingPlaylistVideo()")
        //Code section initiate RecycleView and PlaylistAdapter
        val linearLayoutManager = LinearLayoutManager(this@SelectedPListDetailsActivity)
        viewBinding.rvListVideo.layoutManager = linearLayoutManager
        playlistAdapter = PlaylistAdapter(viewModel.mPlayList)
        viewBinding.rvListVideo.adapter = playlistAdapter

    }

    private fun displayPlaylistVideo() {
        if (viewModel.errorMessage.isNullOrEmpty()) {
            playlistAdapter.update(viewModel.mPlayList)
            if (viewModel.mPlayList[0].snippet.thumbnails?.high?.url!!.isNotEmpty()){
                Glide.with(this@SelectedPListDetailsActivity).load(viewModel.mPlayList[0].snippet.thumbnails?.high?.url).into(viewBinding.ivSelectedThumbnail)
            }

            viewBinding.ivSinglePlay.visibility = View.VISIBLE
            viewModel.youtubeVideoID = viewModel.mPlayList[0].snippet.resourceId?.videoId!!
        } else {
            Toast.makeText(this@SelectedPListDetailsActivity, viewModel.errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    private fun configAllClickListener() {
        //Code section when user click into main thumbnail image
        viewBinding.ivSelectedThumbnail.setOnClickListener {
            val intent = Intent(this@SelectedPListDetailsActivity,VideoViewActivity::class.java)
            intent.putExtra("youtubeVideoID", viewModel.youtubeVideoID)
            startActivity(intent)
        }

        viewBinding.ibPlayAllBtn.setOnClickListener {
            Log.i(TAG, "Play all video is pressed by user")
            val allVideoId = arrayListOf<String>()
            for (video in viewModel.mPlayList){
                allVideoId.add(video.snippet.resourceId?.videoId!!)
            }

            val intent = Intent(this@SelectedPListDetailsActivity,VideoViewActivity::class.java)
            intent.putExtra("playAll",allVideoId)
            startActivity(intent)
        }

        viewBinding.ivProfilePic.setOnClickListener {
            Log.i(TAG,"User click on profile icon custom dialog show")
            MyCustomDialog(viewModel.etUser).show(supportFragmentManager,"MyCustomFragment")
        }
    }

    private fun configAllLiveDataObserver() {
        viewModel.loading.observe(this@SelectedPListDetailsActivity, { isLoading ->
            if (!isLoading) {
                displayPlaylistVideo()
                viewBinding.progressBar.visibility = View.INVISIBLE
            } else viewBinding.progressBar.visibility = View.VISIBLE
        })
    }

    private fun settingBottomNavigation() {
        //Code section for Bottom Navigation menu item
        viewBinding.bottomNavigation.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.home_page_menu_home -> {
                    if (viewModel.isExiting) {
                        val intent = Intent(this@SelectedPListDetailsActivity,HomePageActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else viewModel.isExiting = true

                    true
                }

                R.id.home_page_menu_search -> {
                    val intent = Intent(this@SelectedPListDetailsActivity,SearchVideoActivity::class.java)
                    intent.putExtra("ETUser",viewModel.etUser)
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
                viewModel.isExiting = false
                viewBinding.bottomNavigation.selectedItemId = R.id.home_page_menu_home
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
            viewBinding.progressBar.visibility = View.INVISIBLE
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
            if (this.isJustStarted) {
                dataSet[0].isVideoSelected = true
                this.isJustStarted = false
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
                if (video.snippet.thumbnails?.high?.url!!.isNotEmpty()) {
                    Glide.with(holder.itemView.context).load(video.snippet.thumbnails?.high?.url).into(viewBinding.ivSelectedThumbnail)
                    viewModel.youtubeVideoID = video.snippet.resourceId?.videoId!!
                }

                //Code section to reset proper value in data set in adapter
                dataSet[this.oldIndexSelected].isVideoSelected = false
                dataSet[position].isVideoSelected = true
                this.oldIndexSelected = position
                notifyDataSetChanged()
            }
        }
    }

    private companion object {
        private const val TAG = "SelectedPListDetails"
    }
}
