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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import dev.kingkongcode.edtube.R
import dev.kingkongcode.edtube.databinding.ActivitySearchVideoBinding
import dev.kingkongcode.edtube.dialogs.MyCustomDialog
import dev.kingkongcode.edtube.model.PlaylistItem
import dev.kingkongcode.edtube.app.BaseActivity
import dev.kingkongcode.edtube.app.ConvertDurationIsoToString
import dev.kingkongcode.edtube.viewmodel.SearchVideoViewModel
import dev.kingkongcode.edtube.viewmodel.SearchVideoViewModelFactory


class SearchVideoActivity : BaseActivity() {
    private lateinit var viewBinding: ActivitySearchVideoBinding
    private lateinit var viewModel: SearchVideoViewModel
    private lateinit var mGoogleSignInClient : GoogleSignInClient
    private lateinit var playListAdapter: VideoListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivitySearchVideoBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        Log.i(TAG,"onCreate is called")

        viewModel = ViewModelProviders.of(this, SearchVideoViewModelFactory(application)).get(SearchVideoViewModel::class.java)

        configVideoViewList()
        configSearchBarOkAndEnterButton()
        retrieveUserProfileData()
        displayUserProfileIcon()
        configAllClickListener()
        configAllLiveDataListener()
        hideKeyboard()
    }

    private fun configVideoViewList() {
        viewBinding.rvYTVideoList.layoutManager = LinearLayoutManager(this@SearchVideoActivity, LinearLayoutManager.VERTICAL, false)
        playListAdapter = VideoListAdapter(viewModel.mSearchResultList)
        viewBinding.rvYTVideoList.adapter = playListAdapter
    }

    private fun settingBottomNavigation() {
        //Set bottom navigation to first item menu
        viewBinding.bottomNavigation.selectedItemId = R.id.home_page_menu_search
        //Code section for Bottom Navigation menu item
        viewBinding.bottomNavigation.setOnNavigationItemSelectedListener {
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

    private fun configSearchBarOkAndEnterButton() {
        // Section where we specify action on soft keyboard's Ok button
        viewBinding.etSearchBar.setOnEditorActionListener { _, i, keyEvent ->
            if ((keyEvent != null && (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER)) || (i == EditorInfo.IME_ACTION_DONE)) {
                viewBinding.ibConfirmOrDelete.performClick()
                true
            }
            false
        }
    }

    private fun retrieveUserProfileData() {
        val extras: Bundle? = intent.extras
        extras?.let { data ->
            viewModel.etUser = data.getParcelable("ETUser")!!
        }
    }

    private fun displayUserProfileIcon() {
        Glide.with(this@SearchVideoActivity).load(viewModel.etUser.userPhoto).
        diskCacheStrategy(DiskCacheStrategy.NONE).
        error(R.drawable.profile_pic_na).into(viewBinding.ivProfilePic)
    }

    //Code to hide keyboard on all editText when user touch screen
    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        this.viewBinding.mainView.setOnClickListener {
            imm.hideSoftInputFromWindow(viewBinding.etSearchBar.windowToken, 0)
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
                viewBinding.bottomNavigation.selectedItemId = R.id.home_page_menu_search
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

    private fun configAllClickListener() {
        // Button Search
        viewBinding.ibConfirmOrDelete.setOnClickListener {
            if (!viewBinding.etSearchBar.text.isNullOrEmpty() && !viewModel.clearButtonShowing) confirmQuery()
            else if (!viewBinding.etSearchBar.text.isNullOrEmpty() && viewModel.clearButtonShowing) deleteQuery()
            else Toast.makeText(this,getString(R.string.write_search_word),Toast.LENGTH_SHORT).show() // Empty Query Warning
        }

        viewBinding.ivProfilePic.setOnClickListener {
            Log.i(TAG,"User click on profile icon custom dialog show")
            MyCustomDialog(viewModel.etUser).show(supportFragmentManager,"MyCustomFragment")
        }

        settingBottomNavigation()
    }

    private fun confirmQuery() {
        viewModel.run {
            clearButtonShowing = true
            loading.value = true
            setQueryListResult(viewBinding.etSearchBar.text.toString())
        }

        viewBinding.run {
            ibConfirmOrDelete.setImageResource(R.drawable.ic_morph_reverse)
            mainView.performClick()
        }
    }

    private fun deleteQuery() {
        viewBinding.run {
            etSearchBar.text.clear()
            ibConfirmOrDelete.setImageResource(R.drawable.ic_morph)
            mainView.performClick()
        }

        viewModel.run {
            clearButtonShowing = false
            mSearchResultList.clear()
            playListAdapter.update(mSearchResultList)
        }
    }

    private fun configAllLiveDataListener() {
        viewModel.loading.observe(this, { isLoading ->
            if (isLoading) {
                viewBinding.progressBar.visibility = View.VISIBLE
            } else {
                viewBinding.progressBar.visibility = View.INVISIBLE
                if (!viewModel.errorMessage.isNullOrEmpty()) {
                    Toast.makeText(this@SearchVideoActivity, viewModel.errorMessage, Toast.LENGTH_SHORT).show()
                }
                playListAdapter.update(viewModel.mSearchResultList)
            }
        })
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

                video.let { _video ->
                    tvVideoTitle.text = _video.snippet.title
                    tvAuthor.text = _video.snippet.description
                    tvDuration.text = ConvertDurationIsoToString.convert(_video.duration)
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
            dataSet.addAll(updateList)
            notifyDataSetChanged()
        }
    }

    private companion object {
        private const val TAG = "SearchVideoActivity"
    }
}