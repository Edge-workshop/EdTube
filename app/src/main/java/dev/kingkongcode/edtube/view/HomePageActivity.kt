package dev.kingkongcode.edtube.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import dev.kingkongcode.edtube.R
import dev.kingkongcode.edtube.databinding.ActivityHomePageBinding
import dev.kingkongcode.edtube.dialogs.MyCustomDialog
import dev.kingkongcode.edtube.model.PlaylistItem
import dev.kingkongcode.edtube.app.BaseActivity
import dev.kingkongcode.edtube.app.PaginationList
import dev.kingkongcode.edtube.viewmodel.HomePageViewModel
import dev.kingkongcode.edtube.viewmodel.HomePageViewModelFactory
import java.util.*

class HomePageActivity : BaseActivity() {
    private lateinit var viewBinding: ActivityHomePageBinding
    private lateinit var viewModel: HomePageViewModel
    private lateinit var accessToken: String
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var playlistAdapter: PlaylistAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG,"onCreate()")
        viewBinding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewModel = ViewModelProviders.of(this@HomePageActivity, HomePageViewModelFactory(application)).get(HomePageViewModel::class.java)

        viewBinding.progressBar.visibility = View.VISIBLE
        retrieveDataInfo()
        viewModel.setUserProfile()
        viewModel.setUserPlaylist()
        configAllListener()
        configAllLiveDataObserver()
        viewModel.initGoogleSignIn()
    }


    /***
     * Top menu
     * */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home_page_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
//        val menuLayout: MenuItem = menu!!.findItem(R.id.home_page_menu_profile_icon)
//        val rootView: View = menuLayout.actionView
//
//        val profileIV: ImageView = rootView.findViewById(R.id.ivProfileAvatar)
//
//        //Code to retrieve profile pic from google sign in or else default pic
//        Glide.with(this).load(userPhoto).
//            diskCacheStrategy(DiskCacheStrategy.NONE).
//            error(R.drawable.profile_pic_na).into(profileIV)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return true
    }

    // TODO find replacement for sharedPreferece so i can put variable into ViewModel
    private fun retrieveDataInfo() {
        Log.i(TAG, "retrieveDataInfo()")
        sharedPreferences = this@HomePageActivity.getSharedPreferences("keystoragesaved", Context.MODE_PRIVATE)
        val refreshToken = sharedPreferences.getString("refresh_token", "")
        val idToken = sharedPreferences.getString("id_token", "")
        accessToken = sharedPreferences.getString("access_token", "")!!
        val expiresIn = sharedPreferences.getInt("expires_in", 0)
        val tokenType = sharedPreferences.getString("token_type", "")
    }

    private fun configAllListener() {
        // Code section for Profile Picture
        Log.i(TAG, "configAllListener()")
        viewBinding.ivProfilePic.setOnClickListener {
            Log.i(TAG,"User click on profile icon custom dialog show")
            MyCustomDialog(viewModel.getUserProfile()).show(supportFragmentManager,"MyCustomFragment")
        }

        // Code section for Previous page
        viewBinding.prevPageArrow.setOnClickListener {
            Log.i(TAG, "Previous page arrow is pressed by user")
            if (viewModel.currentPage > 1) {
                viewModel.currentPage -= 1
                //Code section to display user current page and max page
                val (a, b) = PaginationList.showNbrPage(viewModel.userPlayList, viewModel.currentPage)
                viewBinding.pageNbr.text = a
                viewModel.totalPage = b
                //Code section to select right items on the list
                val filterUserList = PaginationList.filterPage(viewModel.userPlayList, viewModel.currentPage)
                playlistAdapter.update(filterUserList)
            }
        }

        // Code section for Next page
        viewBinding.nextPageArrow.setOnClickListener {
            Log.i(TAG, "Next page arrow is pressed by user")
            if (viewModel.currentPage < viewModel.totalPage) {
                viewModel.currentPage += 1
                //Code section to display user current page and max page
                val (a, b) = PaginationList.showNbrPage(viewModel.userPlayList, viewModel.currentPage)
                viewBinding.pageNbr.text = a
                viewModel.totalPage = b
                //Code section to display user current page and max page
                val filterUserList = PaginationList.filterPage(viewModel.userPlayList, viewModel.currentPage)
                playlistAdapter.update(filterUserList)
            }
        }

        // Code section for Bottom Navigation buttons
        viewBinding.bottomNav.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.home_page_menu_home -> {
                    Log.i(TAG, "Home from bottom navigation is pressed by user")
                    true
                }

                R.id.home_page_menu_search -> {
                    Log.i(TAG, "Search from bottom navigation is pressed by user")
                    val intent = Intent(this@HomePageActivity,SearchVideoActivity::class.java)
                    intent.putExtra("ETUser", viewModel.getUserProfile())
                    startActivity(intent)
                    finish()
                    true
                }

                R.id.home_page_menu_log_out -> {
                    Log.i(TAG, "Logout from bottom navigation is pressed by user")
                    showLogOutWarningDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun settingVideoGrid() {
        Log.i(TAG,"settingVideoGrid()")
        viewBinding.playlistGridView.layoutManager = GridLayoutManager(this@HomePageActivity, 2, GridLayoutManager.VERTICAL, false)
        playlistAdapter = PlaylistAdapter(
            //Code section to display user current page and max page
            PaginationList.filterPage(viewModel.userPlayList, viewModel.currentPage)
        )
        viewBinding.pageNbr.text = viewModel.paginationDisplay
        viewBinding.playlistGridView.adapter = playlistAdapter
        playlistAdapter.notifyDataSetChanged()
    }

    private fun showLogOutWarningDialog(){
        Log.i(TAG, "Log out warning dialog is created")
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.apply {
            setMessage("Do you want to close this application ?")
            setCancelable(false)
            setPositiveButton("YES") {
                    _, _ -> signOut()
            }

            setNegativeButton("NO") { dialog, _ ->
                dialog.cancel()
                viewBinding.bottomNav.selectedItemId = R.id.home_page_menu_home
            }

            create()
            setTitle("EdTube")
            show()
        }
    }


    // TODO google variable is null its not initialize TEST LAST AT THE END OF APP
    private fun signOut() {
        Log.i(TAG,"signOut()")
        viewModel.mGoogleSignInClient.signOut()
            .addOnCompleteListener(this) {
                Toast.makeText(this, getString(R.string.signOut_success), Toast.LENGTH_LONG).show()
                finish()
            }
    }

    private fun displayOnTopUserInfo() {
        Log.i(TAG, "displayOnTopUserInfo()")
        //Code to display the right String in user lang
        viewBinding.tvUsernameBigTitle.text = if (Locale.getDefault().isO3Language == "eng") {
            "${viewModel.getUserProfile().firstName}' s Playlist."
        } else "La Playlist à ${viewModel.getUserProfile().firstName}."

        //Code to retreive profile pic from google sign in or else default pic
        Glide.with(this@HomePageActivity).load(viewModel.getUserProfile().userPhoto).
        diskCacheStrategy(DiskCacheStrategy.NONE).
        error(R.drawable.profile_pic_na).into(viewBinding.ivProfilePic)
    }

    private fun configAllLiveDataObserver() {
        Log.i(TAG, "configAllLiveDataObserver()")
        viewModel.loading.observe(this, { loading ->
            if (!loading) {
                viewBinding.progressBar.visibility = View.INVISIBLE
                displayOnTopUserInfo()
                settingVideoGrid()
            }
        })
    }

    inner class PlaylistAdapter(private val dataSet: MutableList<PlaylistItem>) : RecyclerView.Adapter<HomePageActivity.PlaylistAdapter.ViewHolder>() {
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvPlaylistTitle: TextView = itemView.findViewById(R.id.tvPlaylistTitle)
            private val tvNbrOfVideo: TextView = itemView.findViewById(R.id.tvNbrOfVideo)
            private val ivThumbnail: ImageView = itemView.findViewById(R.id.ivThumbnail)

            @SuppressLint("SetTextI18n")
            fun bind(playlistItem: PlaylistItem) {
                tvPlaylistTitle.text = playlistItem.snippet.title
                val nbrOfVideoStr = getString(R.string.number_of_video)
                val videoNbr = playlistItem.detailsXItem.itemCountStr
                (nbrOfVideoStr+"\t\t"+videoNbr).also { tvNbrOfVideo.text = it }

                if (playlistItem.snippet.thumbnails?.high?.url!!.isNotEmpty()){
                    Glide.with(itemView.context).load(playlistItem.snippet.thumbnails!!.high!!.url).into(ivThumbnail)
                }
            }

        }

        override fun getItemCount() = dataSet.size
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.playlist_cell, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val playlistItem: PlaylistItem = dataSet[position]
            holder.bind(playlistItem)

            holder.itemView.setOnClickListener {
                val intent = Intent(this@HomePageActivity,SelectedPListDetailsActivity::class.java)
                intent.putExtra("selectedListID", playlistItem.listId)
                intent.putExtra("videoNbr", playlistItem.detailsXItem.itemCountStr)
                intent.putExtra("ETUser", viewModel.getUserProfile())
                startActivity(intent)
                finish()
            }
        }

        fun update(updateList: List<PlaylistItem>) {
            dataSet.clear()
            dataSet.addAll(updateList)
            notifyDataSetChanged()
        }

    }

    private companion object {
        private const val TAG = "HomePageActivity"
        private const val RC_SIGN_IN = 0
    }
}
