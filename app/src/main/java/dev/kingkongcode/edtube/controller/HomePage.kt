package dev.kingkongcode.edtube.controller

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationMenu
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubeThumbnailLoader
import com.google.android.youtube.player.YouTubeThumbnailView
import dev.kingkongcode.edtube.R
import dev.kingkongcode.edtube.server.APIManager
import dev.kingkongcode.edtube.util.HideSystemUi
import dev.kingkongcode.edtube.util.PaginationList
import java.util.*


class HomePage : YouTubeBaseActivity() {

    private val TAG = "HomePage"
    private lateinit var accessToken: String
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var mGoogleSignInClient : GoogleSignInClient

    private lateinit var tvUsernameTitle: TextView
    private lateinit var ivProfilePic: ImageView

    private lateinit var playlistGridView: GridView
    private lateinit var playlistAdapter: PlaylistAdapter
    private var userPList = arrayListOf<dev.kingkongcode.edtube.model.PlaylistItem>()

    private lateinit var pageNbr: TextView
    private var currentPage: Int = 1
    private var maxPage: Int = 1
    private lateinit var prevPageArrow: ImageButton
    private lateinit var nextPageArrow: ImageButton

    private lateinit var bottomNav: BottomNavigationView

//    private lateinit var youtubePlayerView: YouTubePlayerView
//    private lateinit var onInitializedListener: YouTubePlayer.OnInitializedListener

//    private var youtubeVideoID = "9wfRswYoex4"
//    private val YOUTUBE_API_KEY = "AIzaSyA6okESac-Fhdlx6yqRG5QkSKswXxBgh5Y"
    private val RC_SIGN_IN = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        HideSystemUi.hideSystemUi(this)

        sharedPreferences = this.getSharedPreferences("keystoragesaved", Context.MODE_PRIVATE)
        val refreshToken = sharedPreferences.getString("refresh_token", "")
        val idToken = sharedPreferences.getString("id_token", "")
        accessToken = sharedPreferences.getString("access_token", "")!!
        val expiresIn = sharedPreferences.getInt("expires_in", 0)
        val tokenType = sharedPreferences.getString("token_type", "")

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

        val acct = GoogleSignIn.getLastSignedInAccount(this)
        if (acct != null) {
            //val personName = acct.displayName
            val personGivenName = acct.givenName
            //val personFamilyName = acct.familyName
            //val personEmail = acct.email
            //val personId = acct.id
            val personPhoto: Uri? = acct.photoUrl

            //Code to display the right String in user lang
            val completeStringTitle = if (Locale.getDefault().isO3Language == "eng"){
                "$personGivenName' s Playlist."
            } else "La Playlist à $personGivenName."

            tvUsernameTitle.text = completeStringTitle

            //Code to retreive profile pic from google sign in or else default pic
            Glide.with(this).load(personPhoto).
            diskCacheStrategy(DiskCacheStrategy.NONE).
            error(R.drawable.profile_pic_na).into(ivProfilePic)

            ivProfilePic.setOnClickListener {
                Toast.makeText(this@HomePage, "Profile icon is pressed", Toast.LENGTH_SHORT).show()
                //youtubePlayerView.initialize(YOUTUBE_API_KEY,onInitializedListener)

            }


            /**
             * YouTube
             * */

//            youtubePlayerView = findViewById(R.id.youtubePlayerScreen)
//            onInitializedListener = object : YouTubePlayer.OnInitializedListener {
//                override fun onInitializationSuccess(
//                    p0: YouTubePlayer.Provider?,
//                    youtubePlayer: YouTubePlayer?,
//                    p2: Boolean
//                ) {
//                    youtubePlayer?.loadVideo(youtubeVideoID)
//                }
//
//                override fun onInitializationFailure(
//                    p0: YouTubePlayer.Provider?,
//                    p1: YouTubeInitializationResult?
//                ) {
//                    Toast.makeText(
//                        this@HomePage,
//                        "Something went wrong when loading video from YouTube!",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }


        }
        // youtubePlayerView.initialize(YOUTUBE_API_KEY,onInitializedListener)

        reqListApi()

        prevPageArrow.setOnClickListener {
            if (currentPage > 1){
                currentPage -= 1

                val (a, b) = PaginationList.showNbrPage(this,userPList,currentPage)
                pageNbr.text = a
                maxPage = b

                val filterUserList = PaginationList.filterPage(this,userPList,currentPage)
                playlistAdapter.clear()
                for (itemList in filterUserList){
                    playlistAdapter.add(itemList)
                }
                playlistAdapter.notifyDataSetChanged()
            }
        }

        nextPageArrow.setOnClickListener {
            if (currentPage < maxPage){
                currentPage += 1

                val (a, b) = PaginationList.showNbrPage(this,userPList,currentPage)
                pageNbr.text = a
                maxPage = b

                val filterUserList = PaginationList.filterPage(this,userPList,currentPage)
                playlistAdapter.clear()
                for (itemList in filterUserList){
                    playlistAdapter.add(itemList)
                }
                playlistAdapter.notifyDataSetChanged()
            }
        }

        bottomNav.setOnNavigationItemSelectedListener {
            when(it.itemId){

                R.id.home_page_menu_home ->{
                    Toast.makeText(this,"NOT Implemented yet",Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.home_page_menu_search ->{
                    Toast.makeText(this,"NOT Implemented yet",Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.home_page_menu_log_out ->{
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
        Log.i(TAG, "onActivityResult requestCode=$requestCode and resultCode=$resultCode")

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
        mGoogleSignInClient.signOut()
            .addOnCompleteListener(this) {
                Toast.makeText(this, getString(R.string.signOut_succes), Toast.LENGTH_LONG).show()
                finish()
            }
    }

    private fun reqListApi(){
        APIManager.instance.requestUserPlaylist(this, completion = { error, userPlaylist ->
            error?.let { }

            userPlaylist?.let {
                userPList = it.items

                val (a, b) = PaginationList.showNbrPage(this,userPList,currentPage)
                pageNbr.text = a
                maxPage = b

                playlistAdapter = PlaylistAdapter(this, PaginationList.filterPage(this,userPList,currentPage))
                playlistGridView.adapter = playlistAdapter
            }
        })
    }


    inner class PlaylistAdapter(
        private var mContext: Context,
        private val dataSet: List<dev.kingkongcode.edtube.model.PlaylistItem>
    ) : ArrayAdapter<dev.kingkongcode.edtube.model.PlaylistItem?>(
        mContext,
        R.layout.playlist_cell,
        dataSet
    ), View.OnClickListener {

        private inner class ViewHolder {
            var vThumbnail: YouTubeThumbnailView? = null
            var tvPlaylistTitle: TextView? = null
            var tvNbrOfVideo: TextView? = null
        }

        @Override
        override fun onClick(v: View) {}
        @Override
        override fun getItem(position: Int): dev.kingkongcode.edtube.model.PlaylistItem? {
            return super.getItem(position)
        }
        @Override
        override fun getPosition(item: dev.kingkongcode.edtube.model.PlaylistItem?): Int {
            return super.getPosition(item)
        }

        @Override
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            val dataModel = getItem(position)

            // Check if an existing view is being reused, otherwise inflate the view
            val viewHolder: ViewHolder // view lookup cache stored in tag
            val result: View
            if (convertView == null) {
                viewHolder = ViewHolder()
                val inflater = LayoutInflater.from(context)
                convertView = inflater.inflate(R.layout.playlist_cell, parent, false)
                viewHolder.vThumbnail = convertView.findViewById(R.id.vThumbnail)
                viewHolder.tvPlaylistTitle = convertView.findViewById(R.id.tvPlaylistTitle)
                viewHolder.tvNbrOfVideo = convertView.findViewById(R.id.tvNbrOfVideo)
                result = convertView
                convertView.tag = viewHolder
            } else {
                viewHolder = convertView.tag as ViewHolder
                result = convertView
            }

            if (dataModel != null) {
                viewHolder.tvPlaylistTitle!!.text = dataModel.snippet.title
                val nbrOfVideoStr = getString(R.string.number_of_video)
                val videoNbr = dataModel.detailsXItem.itemCountStr
                viewHolder.tvNbrOfVideo!!.text = nbrOfVideoStr+videoNbr

                //TODO thumbnails not working get that fix
                //viewHolder.vThumbnail = dataModel.snippet.thumbnails.default.url

//                viewHolder.vThumbnail?.initialize(
//                    accessToken,
//                    object : YouTubeThumbnailView.OnInitializedListener {
//                        @Override
//                        override fun onInitializationSuccess(
//                            youTubeThumbnailView: YouTubeThumbnailView?,
//                            youTubeThumbnailLoader: YouTubeThumbnailLoader
//                        ) {
//                            youTubeThumbnailLoader.setVideo("9wfRswYoex4")
//                            youTubeThumbnailLoader.setOnThumbnailLoadedListener(object :
//                                YouTubeThumbnailLoader.OnThumbnailLoadedListener {
//                                @Override
//                                override fun onThumbnailLoaded(
//                                    youTubeThumbnailView: YouTubeThumbnailView?,
//                                    s: String?
//                                ) {
//                                    youTubeThumbnailLoader.release()
//                                }
//
//                                @Override
//                                override fun onThumbnailError(
//                                    youTubeThumbnailView: YouTubeThumbnailView?,
//                                    errorReason: YouTubeThumbnailLoader.ErrorReason?
//                                ) {
//                                    errorReason?.name
//                                }
//                            })
//                        }
//
//                        @Override
//                        override fun onInitializationFailure(
//                            youTubeThumbnailView: YouTubeThumbnailView?,
//                            youTubeInitializationResult: YouTubeInitializationResult?
//                        ) {
//                        }
//                    })

                notifyDataSetChanged()
            }
            return convertView!!
        }
    }



}