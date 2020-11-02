package dev.kingkongcode.edtube.controller

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import dev.kingkongcode.edtube.model.ETUser
import dev.kingkongcode.edtube.dialogs.MyCustomDialog
import dev.kingkongcode.edtube.model.PlaylistItemActivity
import dev.kingkongcode.edtube.server.APIManager
import dev.kingkongcode.edtube.util.BaseActivity

private const val TAG = "SearchVideoActivity"

class SearchVideoActivity : BaseActivity() {
    private lateinit var main: ConstraintLayout
    private lateinit var progressBar: ProgressBar

    private lateinit var ivProfilePic: ImageView
    private lateinit var etSearchBar: EditText
    private lateinit var ibConfirmOrDelete: ImageButton

    private lateinit var rvYTVideoList: RecyclerView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private var mSearchResultList = arrayListOf<PlaylistItemActivity>()
    private lateinit var playlistAdapter: SearchVideoActivity.VideoListAdapter

    private lateinit var bottomNavigation: BottomNavigationView

    private lateinit var etUser: ETUser
    private lateinit var mGoogleSignInClient : GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_video)
        Log.i(TAG,"onCreate is called")

        //Main Layout view
        main = findViewById(R.id.mainView)
        //ProgressBar
        progressBar = findViewById(R.id.progressBar)
        //Profil Picture
        ivProfilePic = findViewById(R.id.ivProfileAvatar)
        //SearchBar
        etSearchBar = findViewById(R.id.etSearchBar)
        //Confirm or Delete button for search request word
        ibConfirmOrDelete = findViewById(R.id.ibConfirmOrDelete)
        //Video List
        rvYTVideoList = findViewById(R.id.rvYTVideoList)
        linearLayoutManager = LinearLayoutManager(this)
        rvYTVideoList.layoutManager = linearLayoutManager
        //Bottom Navigation
        bottomNavigation = findViewById(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.home_page_menu_search

        //Initialize RecycleView and adapter
        playlistAdapter = VideoListAdapter(this@SearchVideoActivity,this.mSearchResultList)
        rvYTVideoList.adapter = playlistAdapter

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
            error(R.drawable.profile_pic_na).into(ivProfilePic)

            ivProfilePic.setOnClickListener {
                Log.i(TAG,"User click on profil icon custom dialog show")
                MyCustomDialog(etUser,this@SearchVideoActivity).show(supportFragmentManager,"MyCustomFragment")
            }
        }

        //Code ro hide keyboard on all editText
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        main.setOnClickListener {
            imm.hideSoftInputFromWindow(etSearchBar.windowToken, 0)
        }

        //Bouton Search
        var isDeleting = false
        ibConfirmOrDelete.setOnClickListener {
//            HideSystemUi.hideSystemUi(this)

            if (!etSearchBar.text.isNullOrEmpty() && !isDeleting) {
                isDeleting = true
                progressBar.visibility = View.VISIBLE
//                ibConfirmOrDelete.setImageResource(R.drawable.clear_icon)
                ibConfirmOrDelete.setImageResource(R.drawable.ic_morph_reverse)

                APIManager.instance.requestSearchVideo(this@SearchVideoActivity, etSearchBar.text.toString(), completion = { error, searchResultList ->
                    error?.let { Toast.makeText(this@SearchVideoActivity,error,Toast.LENGTH_SHORT).show() }

                    searchResultList?.let {
                        var tempSearchFilter = arrayListOf<PlaylistItemActivity>()
                        this.mSearchResultList.clear()
                        for (video in it){
                            if (video.id.kind == "youtube#video"){
                                tempSearchFilter.add(video)
                            }
                        }

                        this.mSearchResultList.addAll(tempSearchFilter)
                        this.rvYTVideoList.adapter?.notifyDataSetChanged()
                    }
                })

                progressBar.visibility = View.INVISIBLE
                //Code section to automatically hide editText Keyboard
                main.performClick()
            }else if (!etSearchBar.text.isNullOrEmpty() && isDeleting) {
                etSearchBar.text.clear()
                isDeleting = false
//                ibConfirmOrDelete.setImageResource(R.drawable.check_icon)
                ibConfirmOrDelete.setImageResource(R.drawable.ic_morph)

                //Code section to automatically hide editText Keyboard
                main.performClick()
            } else Toast.makeText(this,getString(R.string.write_search_word),Toast.LENGTH_SHORT).show()
        }

        //Code section for Bottom Navigation menu item
        bottomNavigation.setOnNavigationItemSelectedListener {
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
                bottomNavigation.selectedItemId = R.id.home_page_menu_search
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

    private inner class VideoListAdapter(private val mContext: Context, private var dataSet: List<PlaylistItemActivity>) : RecyclerView.Adapter<SearchVideoActivity.VideoListAdapter.VideoViewHolder>(), View.OnClickListener {

        private inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var ivThumbnail = itemView.findViewById<ImageView>(R.id.ivThumbnail)
            var tvVideoTitle = itemView.findViewById<TextView>(R.id.tvVideoTitle)
            var tvAuthor = itemView.findViewById<TextView>(R.id.tvAuthor)
            var tvDuration = itemView.findViewById<TextView>(R.id.tvDuration)

            fun bind(video: PlaylistItemActivity, position: Int) {
                //Code section to send image thumbnails to main view when user click on specific row
                if (!video.snippet.thumbnails.high.url.isNullOrEmpty()){
                    Glide.with(mContext).load(video.snippet.thumbnails.high.url).into(ivThumbnail)
                }
                //TODO find solution to get author name and video duration
                tvVideoTitle.text = video.snippet.title
                tvAuthor.text = video.snippet.title
                tvDuration.text = "N/A"
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