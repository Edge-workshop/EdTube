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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import dev.kingkongcode.edtube.R
import dev.kingkongcode.edtube.databinding.ActivityHomePageBinding
import dev.kingkongcode.edtube.model.ETUser
import dev.kingkongcode.edtube.dialogs.MyCustomDialog
import dev.kingkongcode.edtube.model.PlaylistItem
import dev.kingkongcode.edtube.app.server.APIManager
import dev.kingkongcode.edtube.app.BaseActivity
import dev.kingkongcode.edtube.app.PaginationList
import java.util.*

class HomePageActivity : BaseActivity() {
    private lateinit var binding: ActivityHomePageBinding
    private lateinit var accessToken: String
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var playlistAdapter: PlaylistAdapter
    private var userPList = arrayListOf<PlaylistItem>()

    private var currentPage: Int = 1
    private var maxPage: Int = 1

    private lateinit var etUser: ETUser

    private companion object {
        private const val TAG = "HomePage"
        private const val RC_SIGN_IN = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.i(TAG,"onCreate is called")

        retrieveDataInfo()
        binding.progressBar.visibility = View.VISIBLE
        setUserProfile()
        settingVideoGrid()
        reqListApi()
        setPreviousAndNextPageButton()
        setBottomNavigation()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i(TAG, "onActivityResult requestCode= $requestCode and resultCode= $resultCode")
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val idToken = account?.idToken
            val deviceCode = account?.serverAuthCode

            // updateUI(account)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
        }
    }

    private fun retrieveDataInfo() {
        sharedPreferences = this@HomePageActivity.getSharedPreferences("keystoragesaved", Context.MODE_PRIVATE)
        val refreshToken = sharedPreferences.getString("refresh_token", "")
        val idToken = sharedPreferences.getString("id_token", "")
        accessToken = sharedPreferences.getString("access_token", "")!!
        val expiresIn = sharedPreferences.getInt("expires_in", 0)
        val tokenType = sharedPreferences.getString("token_type", "")
    }

    private fun settingVideoGrid() {
        binding.playlistGridView.layoutManager = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)
        playlistAdapter = PlaylistAdapter(
            //Code section to display user current page and max page
            PaginationList.filterPage(userPList, currentPage)
        )
        binding.playlistGridView.adapter = playlistAdapter
    }

    private fun showLogOutDialog(){
        // build alert dialog
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.apply {
            setMessage("Do you want to close this application ?")
            setCancelable(false)
            setPositiveButton("YES") {
                    _, _ -> signOut()
            }

            setNegativeButton("NO") { dialog, _ ->
                dialog.cancel()
                binding.bottomNav.selectedItemId = R.id.home_page_menu_home
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

    private fun reqListApi() {
        Log.i(TAG,"Request user list to api manager")
        APIManager.instance.requestUserPlaylist(this, completion = { error, userPlaylist ->
            Log.i(TAG,"APIManager requestUserPlaylist response receive in activity")
            binding.progressBar.apply { visibility = View.INVISIBLE }

            error?.let { Toast.makeText(this@HomePageActivity,error,Toast.LENGTH_SHORT).show() }

            //Code section when we populate GridView adapter
            userPlaylist?.let {
                userPList = it.items
                //Code section to display user current page and max page
                val (a, b) = PaginationList.showNbrPage(userPList, currentPage)
                binding.pageNbr.text = a
                maxPage = b

                playlistAdapter.update(PaginationList.filterPage(userPList, currentPage))
            }
        })
    }

    private fun setPreviousAndNextPageButton() {
        //Code section for Previous page with pagination function
        binding.prevPageArrow.setOnClickListener {
            if (currentPage > 1) {
                currentPage -= 1
                //Code section to display user current page and max page
                val (a, b) = PaginationList.showNbrPage(userPList, currentPage)
                binding.pageNbr.text = a
                maxPage = b
                //Code section to select right items on the list
                val filterUserList = PaginationList.filterPage(userPList, currentPage)
                playlistAdapter.update(filterUserList)
            }
        }

        //Code section for Next page with pagination function
        binding.nextPageArrow.setOnClickListener {
            if (currentPage < maxPage) {
                currentPage += 1
                //Code section to display user current page and max page
                val (a, b) = PaginationList.showNbrPage(userPList, currentPage)
                binding.pageNbr.text = a
                maxPage = b
                //Code section to display user current page and max page
                val filterUserList = PaginationList.filterPage(userPList, currentPage)
                playlistAdapter.update(filterUserList)
            }
        }
    }

    private fun setUserProfile() {
        val acct = GoogleSignIn.getLastSignedInAccount(this)
        acct?.let {
            //Creating user
            etUser = ETUser(it.givenName,it.familyName,it.email,it.photoUrl)
            //Code to display the right String in user lang
            binding.tvUsernameBigTitle.text = if (Locale.getDefault().isO3Language == "eng") {
                "${etUser.firstName}' s Playlist."
            } else "La Playlist à ${etUser.firstName}."

            //Code to retreive profile pic from google sign in or else default pic
            Glide.with(this@HomePageActivity).load(etUser.userPhoto).
            diskCacheStrategy(DiskCacheStrategy.NONE).
            error(R.drawable.profile_pic_na).into(binding.ivProfilePic)

            binding.ivProfilePic.setOnClickListener {
                Log.i(TAG,"User click on profile icon custom dialog show")
                MyCustomDialog(etUser).show(supportFragmentManager,"MyCustomFragment")
            }
        }
    }

    private fun setBottomNavigation() {
        //Code section for Bottom Navigation menu item
        binding.bottomNav.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.home_page_menu_home -> {
                    true
                }

                R.id.home_page_menu_search -> {
                    val intent = Intent(this@HomePageActivity,SearchVideoActivity::class.java)
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
                intent.putExtra("ETUser", etUser)
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
}
