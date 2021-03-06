package dev.kingkongcode.edtube.view

import android.content.Context
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import dev.kingkongcode.edtube.R
import dev.kingkongcode.edtube.databinding.ActivitySearchVideoBinding
import dev.kingkongcode.edtube.model.ETUser
import dev.kingkongcode.edtube.dialogs.MyCustomDialog
import dev.kingkongcode.edtube.model.PlaylistItem
import dev.kingkongcode.edtube.app.server.APIManager
import dev.kingkongcode.edtube.app.BaseActivity
import dev.kingkongcode.edtube.app.ConvertDurationIsoToString


class SearchVideoActivity : BaseActivity() {
    private lateinit var binding: ActivitySearchVideoBinding
    private lateinit var mGoogleSignInClient : GoogleSignInClient
    private var mSearchResultList = arrayListOf<PlaylistItem>()
    private lateinit var playListAdapter: VideoListAdapter
    private var isDeleting = false

    private companion object {
        private const val TAG = "SearchVideoActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.i(TAG,"onCreate is called")

        //Initialize RecycleView and adapter
        binding.rvYTVideoList.layoutManager = LinearLayoutManager(this@SearchVideoActivity, LinearLayoutManager.VERTICAL, false)
        playListAdapter = VideoListAdapter(this@SearchVideoActivity.mSearchResultList)
        binding.rvYTVideoList.adapter = playListAdapter
    }

    override fun onResume() {
        super.onResume()
        hideKeyboard()
        getUserDataAndSetProfileIcon()
        settingSearchBar()
        settingBottomNavigation()
    }

    private fun settingBottomNavigation() {
        //Set bottom navigation to first item menu
        binding.bottomNavigation.selectedItemId = R.id.home_page_menu_search
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

    private fun settingSearchBar() {
        //Code section where we specify action on soft keyboard's Ok button
        binding.etSearchBar.setOnEditorActionListener { _, i, keyEvent ->
            if ((keyEvent != null && (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER)) || (i == EditorInfo.IME_ACTION_DONE)) {
                binding.ibConfirmOrDelete.performClick()
                true
            }
            false
        }

        //Button Search
        binding.ibConfirmOrDelete.setOnClickListener {
            val tempSearchFilter = arrayListOf<PlaylistItem>()
            if (!binding.etSearchBar.text.isNullOrEmpty() && !isDeleting) {
                isDeleting = true
                binding.progressBar.visibility = View.VISIBLE
                binding.ibConfirmOrDelete.setImageResource(R.drawable.ic_morph_reverse)

                APIManager.instance.requestSearchVideo(this@SearchVideoActivity, binding.etSearchBar.text.toString(), completion = { error, searchResultList ->
                    error?.let { errorMsg -> Toast.makeText(this@SearchVideoActivity, errorMsg, Toast.LENGTH_SHORT).show() }

                    searchResultList?.let { xSelectedPList ->
                        val strVideoIdList = arrayListOf<String>()
                        for (video in xSelectedPList){
                            if (video.id.kind == "youtube#video"){
                                tempSearchFilter.add(video)
                                strVideoIdList.add(video.id.videoId)
                            }
                        }

                        APIManager.instance.requestGetVideoDuration(this@SearchVideoActivity, strVideoIdList, completion = { error, durationVideoList ->
                            error?.let { errorMsg -> Toast.makeText(this@SearchVideoActivity, errorMsg, Toast.LENGTH_LONG).show() }

                            durationVideoList?.let {
                                //Code section where we get list from API server and to match video id from user selected list from first API call
                                for (durationPairObj in it) {
                                    for (videoStrId in tempSearchFilter) {
                                        if (durationPairObj.first == videoStrId.id.videoId) {
                                            videoStrId.duration = durationPairObj.second
                                        }
                                    }
                                }

                                playListAdapter.update(tempSearchFilter)
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
    }

    private fun getUserDataAndSetProfileIcon() {
        val extras: Bundle? = intent.extras
        extras?.let {
            //Getting user info
            val etUser: ETUser = it.getParcelable("ETUser")!!

            //Code to retrieve profile pic from google sign in or else default pic
            Glide.with(this@SearchVideoActivity).load(etUser.userPhoto).
            diskCacheStrategy(DiskCacheStrategy.NONE).
            error(R.drawable.profile_pic_na).into(binding.ivProfilePic)

            binding.ivProfilePic.setOnClickListener {
                Log.i(TAG,"User click on profile icon custom dialog show")
                MyCustomDialog(etUser).show(supportFragmentManager,"MyCustomFragment")
            }
        }
    }

    //Code to hide keyboard on all editText when user touch screen
    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        binding.mainView.setOnClickListener {
            imm.hideSoftInputFromWindow(binding.etSearchBar.windowToken, 0)
        }
    }

    private fun showLogOutDialog() {
        // build alert dialog
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.apply {
            setMessage(getString(R.string.exit_dialog))
            setCancelable(false)
            setPositiveButton(getString(R.string.yes)) { _, _ ->
                signOut()
            }
            setNegativeButton(getString(R.string.no))  {
                    dialog, _ -> dialog.cancel()
                binding.bottomNavigation.selectedItemId = R.id.home_page_menu_search
            }
            create()
            setTitle("EdTube")
            show()
        }
    }

    private fun signOut() {
        Log.i(TAG,"Function signOut is called")
        mGoogleSignInClient.signOut()
            .addOnCompleteListener(this) {
                Toast.makeText(this, getString(R.string.signOut_success), Toast.LENGTH_LONG).show()
                finish()
            }
    }

    private inner class VideoListAdapter(private val dataSet: MutableList<PlaylistItem>) : RecyclerView.Adapter<SearchVideoActivity.VideoListAdapter.VideoViewHolder>() {

        private inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var ivThumbnail: ImageView = itemView.findViewById(R.id.ivThumbnail)
            var tvVideoTitle: TextView = itemView.findViewById(R.id.tvVideoTitle)
            var tvAuthor: TextView = itemView.findViewById(R.id.tvAuthor)
            var tvDuration: TextView = itemView.findViewById(R.id.tvDuration)

            fun bind(video: PlaylistItem, position: Int) {
                //Code section to send image thumbnails to main view when user click on specific row
                if (video.snippet.thumbnails?.high?.url!!.isNotEmpty()){
                    Glide.with(itemView.context).load(video.snippet.thumbnails?.high?.url).into(ivThumbnail)
                }

                video.let {
                    tvVideoTitle.text = it.snippet.title
                    tvAuthor.text = it.snippet.description
                    tvDuration.text = ConvertDurationIsoToString.convert(it.duration)
                }
            }
        }

        @Override
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoListAdapter.VideoViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.ytvideo_result_cell_row, parent, false)
            return VideoViewHolder(view)
        }

        @Override
        override fun getItemCount() = dataSet.size

        @Override
        override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
            val video = dataSet[position]
            holder.bind(video, position)

            holder.itemView.setOnClickListener {
                Log.i(TAG,"User click on specific playlist title: ${video.snippet.title} row position: $position")
                val intent = Intent(holder.itemView.context,VideoViewActivity::class.java)
                intent.putExtra("youtubeVideoID",video.id.videoId)
                startActivity(intent)
            }
        }

        fun update(updateList: List<PlaylistItem>) {
            dataSet.clear()
            dataSet.addAll(updateList)
            notifyDataSetChanged()
        }
    }
}