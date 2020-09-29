package dev.kingkongcode.edtube.controller

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import dev.kingkongcode.edtube.R
import dev.kingkongcode.edtube.model.ETUser
import dev.kingkongcode.edtube.model.MyCustomDialog
import dev.kingkongcode.edtube.server.APIManager
import dev.kingkongcode.edtube.util.HideSystemUi
import dev.kingkongcode.edtube.util.PaginationList
import java.util.*

class HomePage : AppCompatActivity() {

    private val TAG = "HomePage"
    private lateinit var accessToken: String
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mGoogleSignInClient : GoogleSignInClient
    private lateinit var progressBar: ProgressBar

    private lateinit var tvUsernameTitle: TextView
    private lateinit var ivProfilePic: ImageView

    private lateinit var playlistGridView: GridView
    private lateinit var playlistAdapter: PlaylistAdapter
    private var userPList = arrayListOf<dev.kingkongcode.edtube.model.PlaylistItemActivity>()

    private lateinit var pageNbr: TextView
    private var currentPage: Int = 1
    private var maxPage: Int = 1
    private lateinit var prevPageArrow: ImageButton
    private lateinit var nextPageArrow: ImageButton

    private lateinit var bottomNav: BottomNavigationView

    private lateinit var etUser: ETUser
    private val RC_SIGN_IN = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)
        Log.i(TAG,"onCreate is called")

        HideSystemUi.hideSystemUi(this)

        sharedPreferences = this.getSharedPreferences("keystoragesaved", Context.MODE_PRIVATE)
        val refreshToken = sharedPreferences.getString("refresh_token", "")
        val idToken = sharedPreferences.getString("id_token", "")
        accessToken = sharedPreferences.getString("access_token", "")!!
        val expiresIn = sharedPreferences.getInt("expires_in", 0)
        val tokenType = sharedPreferences.getString("token_type", "")

        //ProgressBar
        progressBar = findViewById(R.id.progressBar)
        //Main Title
        tvUsernameTitle = findViewById(R.id.tvUsernameBigTitle)
        //Profile Picture
        ivProfilePic = findViewById(R.id.ivProfileAvatar)
        //PlaylistGridview
        playlistGridView = findViewById(R.id.gvAllPlaylist)
        //Page View nbr
        pageNbr = findViewById(R.id.tvPagination)
        //Back page arrow
        prevPageArrow = findViewById(R.id.backBtn)
        //Next page arrow
        nextPageArrow = findViewById(R.id.fowardBtn)
        //Bottom Navigation
        bottomNav = findViewById(R.id.bottomNavigation)

        initiate()
    }

    private fun initiate() {
        Log.i(TAG,"Function initiate is called")
        progressBar.visibility = View.VISIBLE

        val acct = GoogleSignIn.getLastSignedInAccount(this)
        if (acct != null) {
            //Creating user
            etUser = ETUser(acct.givenName,acct.familyName,acct.email,acct.photoUrl)

            //Code to display the right String in user lang
            val completeStringTitle = if (Locale.getDefault().isO3Language == "eng"){
                "${etUser.firstName}' s Playlist."
            } else "La Playlist à ${etUser.firstName}."
            tvUsernameTitle.text = completeStringTitle

            //Code to retreive profile pic from google sign in or else default pic
            Glide.with(this).load(etUser.userPhoto).
            diskCacheStrategy(DiskCacheStrategy.NONE).
            error(R.drawable.profile_pic_na).into(ivProfilePic)

            ivProfilePic.setOnClickListener {
                Log.i(TAG,"User click on profil icon custom dialog show")
                MyCustomDialog(etUser,this@HomePage).show(supportFragmentManager,"MyCustomFragment")
            }
        }

        reqListApi()

        //Code section for Previous page with pagination function
        prevPageArrow.setOnClickListener {
            if (currentPage > 1){
                currentPage -= 1

                val (a, b) = PaginationList.showNbrPage(this, userPList, currentPage)
                pageNbr.text = a
                maxPage = b

                val filterUserList = PaginationList.filterPage(this, userPList, currentPage)
                playlistAdapter.clear()
                for (itemList in filterUserList){
                    playlistAdapter.add(itemList)
                }
                playlistAdapter.notifyDataSetChanged()
            }
        }

        //Code section for Next page with pagination function
        nextPageArrow.setOnClickListener {
            if (currentPage < maxPage){
                currentPage += 1

                val (a, b) = PaginationList.showNbrPage(this, userPList, currentPage)
                pageNbr.text = a
                maxPage = b

                val filterUserList = PaginationList.filterPage(this, userPList, currentPage)
                playlistAdapter.clear()
                for (itemList in filterUserList){
                    playlistAdapter.add(itemList)
                }
                playlistAdapter.notifyDataSetChanged()
            }
        }

        //Code section for Bottom Navigation menu item
        bottomNav.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.home_page_menu_home -> {
                    true
                }
                R.id.home_page_menu_search -> {
                    val intent = Intent(this@HomePage,SearchVideoActivity::class.java)
                    intent.putExtra("ETUser",etUser)
                    startActivity(intent)
                    finish()
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
//        //Code to retreive profile pic from google sign in or else default pic
//        Glide.with(this).load(userPhoto).
//            diskCacheStrategy(DiskCacheStrategy.NONE).
//            error(R.drawable.profile_pic_na).into(profileIV)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean{
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

    private fun signOut() {
        Log.i(TAG,"Function signOut is called")
        mGoogleSignInClient.signOut()
            .addOnCompleteListener(this) {
                Toast.makeText(this, getString(R.string.signOut_succes), Toast.LENGTH_LONG).show()
                finish()
            }
    }

    private fun reqListApi(){
        Log.i(TAG,"Request user list to api manager")
        APIManager.instance.requestUserPlaylist(this, completion = { error, userPlaylist ->
            Log.i(TAG,"APIManager requestUserPlaylist response receive in activity")

            progressBar?.let { it.visibility = View.INVISIBLE }

            error?.let {Toast.makeText(this@HomePage,error,Toast.LENGTH_SHORT).show() }

            //Code section when we populate GridView adapter
            userPlaylist?.let {
                userPList = it.items

                val (a, b) = PaginationList.showNbrPage(this, userPList, currentPage)
                pageNbr.text = a
                maxPage = b

                playlistAdapter = PlaylistAdapter(
                    this, PaginationList.filterPage(
                        this,
                        userPList,
                        currentPage
                    )
                )
                playlistGridView.adapter = playlistAdapter
            }
        })
    }


    inner class PlaylistAdapter(
        private val mContext: Context, dataSet: List<dev.kingkongcode.edtube.model.PlaylistItemActivity>
    ) : ArrayAdapter<dev.kingkongcode.edtube.model.PlaylistItemActivity?>(
        mContext,
        R.layout.playlist_cell,
        dataSet
    ), View.OnClickListener {

        private inner class ViewHolder {
            lateinit var ivThumbnail: ImageView
            lateinit var tvPlaylistTitle: TextView
            lateinit var tvNbrOfVideo: TextView
        }

        @Override
        override fun onClick(v: View) {}
        @Override
        override fun getItem(position: Int): dev.kingkongcode.edtube.model.PlaylistItemActivity? {
            return super.getItem(position)
        }
        @Override
        override fun getPosition(itemActivity: dev.kingkongcode.edtube.model.PlaylistItemActivity?): Int {
            return super.getPosition(itemActivity)
        }

        @Override
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            val dataModel = getItem(position)

            // Check if an existing view is being reused, otherwise inflate the view
            val viewHolder: ViewHolder // view lookup cache stored in tag
            if (convertView == null) {
                viewHolder = ViewHolder()
                val inflater = LayoutInflater.from(context)
                convertView = inflater.inflate(R.layout.playlist_cell, parent, false)
                viewHolder.ivThumbnail = convertView.findViewById(R.id.ivThumbnail)
                viewHolder.tvPlaylistTitle = convertView.findViewById(R.id.tvPlaylistTitle)
                viewHolder.tvNbrOfVideo = convertView.findViewById(R.id.tvNbrOfVideo)

                convertView.tag = viewHolder
            } else {
                viewHolder = convertView.tag as ViewHolder
            }

            if (dataModel != null) {
                viewHolder.tvPlaylistTitle.text = dataModel.snippet.title
                val nbrOfVideoStr = getString(R.string.number_of_video)
                val videoNbr = dataModel.detailsXItem.itemCountStr
                viewHolder.tvNbrOfVideo.text = nbrOfVideoStr+"\t\t"+videoNbr

                if (dataModel.snippet.thumbnails.high.url.isNotEmpty()){
                    Glide.with(mContext).load(dataModel.snippet.thumbnails.high.url).into(viewHolder.ivThumbnail)
                }

                convertView?.setOnClickListener {
                    val intent = Intent(this@HomePage,SelectedPListDetails::class.java)
                    intent.putExtra("selectedListID",dataModel.listId)
                    intent.putExtra("videoNbr",videoNbr)
                    intent.putExtra("ETUser",etUser)
                    startActivity(intent)
                    finish()
                }

                notifyDataSetChanged()
            }
            return convertView!!
        }
    }



}