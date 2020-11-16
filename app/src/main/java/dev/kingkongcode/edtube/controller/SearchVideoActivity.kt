package dev.kingkongcode.edtube.controller

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import dev.kingkongcode.edtube.R
import dev.kingkongcode.edtube.databinding.ActivitySearchVideoBinding
import dev.kingkongcode.edtube.model.ETUser
import dev.kingkongcode.edtube.dialogs.MyCustomDialog
import dev.kingkongcode.edtube.model.PlaylistItem
import dev.kingkongcode.edtube.server.APIManager
import dev.kingkongcode.edtube.util.BaseActivity
import dev.kingkongcode.edtube.util.ConvertDurationIsoToString

private const val TAG = "SearchVideoActivity"

class SearchVideoActivity : BaseActivity() {
    private lateinit var binding: ActivitySearchVideoBinding
    private lateinit var linearLayoutManager: LinearLayoutManager
    private var mSearchResultList = arrayListOf<PlaylistItem>()
    private lateinit var playlistAdapter: SearchVideoActivity.VideoListAdapter
    private var tempSearchFilter = arrayListOf<PlaylistItem>()

    private lateinit var etUser: ETUser
    private lateinit var mGoogleSignInClient : GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.i(TAG,"onCreate is called")

        linearLayoutManager = LinearLayoutManager(this)
        binding.rvYTVideoList.layoutManager = linearLayoutManager
        binding.bottomNavigation.selectedItemId = R.id.home_page_menu_search

        //Initialize RecycleView and adapter
        playlistAdapter = VideoListAdapter(this@SearchVideoActivity,this.mSearchResultList)
        binding.rvYTVideoList.adapter = playlistAdapter

        initiate()
    }

    private fun initiate() {
        Log.i(TAG,"Function initiate is called")
        val extras: Bundle? = intent.extras

        if (extras != null){
            //Getting user info
            etUser = extras.getParcelable("ETUser")!!

            //Code to retreive profile pic from google sign in or else default pic
            Glide.with(this).load(etUser.userPhoto).
            diskCacheStrategy(DiskCacheStrategy.NONE).
            error(R.drawable.profile_pic_na).into(binding.ivProfilePic)

            binding.ivProfilePic.setOnClickListener {
                Log.i(TAG,"User click on profil icon custom dialog show")
                MyCustomDialog(etUser,this@SearchVideoActivity).show(supportFragmentManager,"MyCustomFragment")
            }
        }

        //Code ro hide keyboard on all editText
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        binding.mainView.setOnClickListener {
            imm.hideSoftInputFromWindow(binding.etSearchBar.windowToken, 0)
        }

        //Code section where we specify action on soft keyboard's Ok button
        binding.etSearchBar.setOnEditorActionListener { _, i, keyEvent ->
            if ((keyEvent != null && (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER)) || (i == EditorInfo.IME_ACTION_DONE)) {
                binding.ibConfirmOrDelete.performClick()
                true
            }
            false
        }

        //Bouton Search
        var isDeleting = false
        binding.ibConfirmOrDelete.setOnClickListener {
            if (!binding.etSearchBar.text.isNullOrEmpty() && !isDeleting) {
                isDeleting = true
                binding.progressBar.visibility = View.VISIBLE
                binding.ibConfirmOrDelete.setImageResource(R.drawable.ic_morph_reverse)

                APIManager.instance.requestSearchVideo(this@SearchVideoActivity, binding.etSearchBar.text.toString(), completion = { error, searchResultList ->
                    error?.let { Toast.makeText(this@SearchVideoActivity,error,Toast.LENGTH_SHORT).show() }

                    searchResultList?.let { xSelectedPList ->
                        var strVideoIdList = arrayListOf<String>()
                        this.mSearchResultList.clear()
                        for (video in xSelectedPList){
                            if (video.id.kind == "youtube#video"){
                                tempSearchFilter.add(video)
                                strVideoIdList.add(video.id.videoId)
                            }
                        }

                        APIManager.instance.requestGetVideoDuration(this@SearchVideoActivity, strVideoIdList, completion = {error, durationVideoList ->
                            error?.let { Toast.makeText(this@SearchVideoActivity, it, Toast.LENGTH_LONG).show() }

                            durationVideoList?.let {
                                //Code section where we get list from API server and to match video id from user selected list from first API call
                                for (durationPairObj in it) {
                                    for (videoStrId in tempSearchFilter) {
                                        if (durationPairObj.first == videoStrId.id.videoId) {
                                            videoStrId.duration = durationPairObj.second
                                        }
                                    }
                                }

                                this.mSearchResultList.clear()
                                this.mSearchResultList.addAll(tempSearchFilter)
                                this.binding.rvYTVideoList.adapter?.notifyDataSetChanged()
                                binding.progressBar.visibility = View.INVISIBLE
                            }
                        })
                    }
                })

                //Code section to automatically hide editText Keyboard
                binding.mainView.performClick()
            } else if (!binding.etSearchBar.text.isNullOrEmpty() && isDeleting) {
                binding.etSearchBar.text.clear()
                isDeleting = false
                binding.ibConfirmOrDelete.setImageResource(R.drawable.ic_morph)
                tempSearchFilter.clear()

                //Code section to automatically hide editText Keyboard
                binding.mainView.performClick()
            } else Toast.makeText(this,getString(R.string.write_search_word),Toast.LENGTH_SHORT).show()
        }

        //Code section for Bottom Navigation menu item
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.home_page_menu_home -> {
                    val intent = Intent(this@SearchVideoActivity,HomePageActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }

                R.id.home_page_menu_search -> {
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
        // set message of alert dialog
        dialogBuilder.setMessage(getString(R.string.exit_dialog))
            // if the dialog is cancelable
            .setCancelable(false)
            // positive button text and action
            .setPositiveButton(getString(R.string.yes), DialogInterface.OnClickListener {
                    dialog, id -> signOut()
            })
            // negative button text and action
            .setNegativeButton(getString(R.string.no), DialogInterface.OnClickListener {
                    dialog, id -> dialog.cancel()
                binding.bottomNavigation.selectedItemId = R.id.home_page_menu_search
            })

        // create dialog box
        val alert = dialogBuilder.create()
        // set title for alert dialog box
        alert.setTitle("EdTube")
        // show alert dialog
        alert.show()
    }

    private fun signOut() {
        Log.i(TAG,"Function signOut is called")
        mGoogleSignInClient.signOut()
            .addOnCompleteListener(this) {
                Toast.makeText(this, getString(R.string.signOut_succes), Toast.LENGTH_LONG).show()
                finish()
            }
    }

    private inner class VideoListAdapter(private val mContext: Context, private var dataSet: List<PlaylistItem>) : RecyclerView.Adapter<SearchVideoActivity.VideoListAdapter.VideoViewHolder>(), View.OnClickListener {

        private inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var ivThumbnail = itemView.findViewById<ImageView>(R.id.ivThumbnail)
            var tvVideoTitle = itemView.findViewById<TextView>(R.id.tvVideoTitle)
            var tvAuthor = itemView.findViewById<TextView>(R.id.tvAuthor)
            var tvDuration = itemView.findViewById<TextView>(R.id.tvDuration)

            fun bind(video: PlaylistItem, position: Int) {
                //Code section to send image thumbnails to main view when user click on specific row
                if (!video.snippet.thumbnails.high.url.isNullOrEmpty()){
                    Glide.with(mContext).load(video.snippet.thumbnails.high.url).into(ivThumbnail)
                }

                tvVideoTitle.text = video.snippet.title
                tvAuthor.text = video.snippet.description
                tvDuration.text = ConvertDurationIsoToString.convert(video.duration)
            }
        }

        @Override
        override fun onClick(v: View) {
        }

        @Override
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoListAdapter.VideoViewHolder {
            val layout =  R.layout.ytvideo_result_cell_row
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
            holder.bind(video, position)

            holder.itemView.setOnClickListener {
                Log.i(TAG,"User click on specific playlist title: ${video.snippet.title} row position: $position")
                val youtubeVideoID = video.id.videoId
                val intent = Intent(this.mContext,VideoViewActivity::class.java)
                intent.putExtra("youtubeVideoID",youtubeVideoID)
                startActivity(intent)
            }
        }
    }
}