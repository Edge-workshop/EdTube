package dev.kingkongcode.edtube.server

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import dev.kingkongcode.edtube.R
import dev.kingkongcode.edtube.model.PlaylistCategory
import dev.kingkongcode.edtube.model.PlaylistItemActivity
import dev.kingkongcode.edtube.util.Constants
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class APIManager {

    private lateinit var ctx: Context
    private val sharedPrefFile = "keystoragesaved"
    private var accessToken = Constants.instance.EMPTY_STRING
    private var userSelectedListRecv = arrayListOf<PlaylistItemActivity>()

    companion object{
        var instance: APIManager = APIManager()
    }

//    private fun getBaseURL(): String {
//        return Config.current.OAUTH2_URL
//    }

    private fun getRegularHeaders(): Map<String, String> {
        val headers = HashMap<String, String>()
//        headers["language"] = Locale.getDefault().displayLanguage
//        headers["version"] = BuildConfig.VERSION_NAME
        headers["Authorization"] = "Bearer $accessToken"
        headers["Accept"] = "application/json"

        return headers
    }


    fun requestAccessToken(context: Context, idToken: String?, deviceCode: String?, completion: (error: String?) -> Unit) {
        this.ctx = context.applicationContext
        val url = Constants.instance.GOOGLE_AUTH2
        Log.i("OAuth api accessToken",url)

        val queue = Volley.newRequestQueue(ctx)

        val bodyJSON = JSONObject()
        bodyJSON.put("grant_type", "authorization_code")
        bodyJSON.put("client_id", Config.current.CLIENT_ID)
        bodyJSON.put("client_secret", Config.current.CLIENT_SECRET)
        bodyJSON.put("redirect_uri", Constants.instance.EMPTY_STRING)
        bodyJSON.put("code", deviceCode)
        bodyJSON.put("id_token", idToken)

        val request = object : JsonObjectRequest(
            Method.POST, url, bodyJSON,
            Response.Listener { response ->
                if (response != null) {

                    this.accessToken = response.optString("access_token")

                    val sharedPreferences: SharedPreferences = this.ctx.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
                    val editor:SharedPreferences.Editor =  sharedPreferences.edit()
                    editor.putString("refresh_token",response.optString("refresh_token"))
                    editor.putString("id_token",response.optString("id_token"))
                    editor.putString("access_token",response.optString("access_token"))
                    editor.putInt("expires_in",response.optInt("expires_in"))
                    editor.putString("token_type",response.optString("token_type"))
                    editor.apply()
                    editor.commit()

                    completion(null)
                } else {
                    completion(R.string.general_unexpected_error.toString())
                }
            },
            Response.ErrorListener { error ->
                completion(error.toString())
            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers.putAll(getRegularHeaders())
                return headers
            }
        }
        queue.add(request)
    }


    fun requestUserPlaylist(context: Context, completion: (error: String?, userPlaylist: PlaylistCategory?) -> Unit) {
        this.ctx = context.applicationContext
        val sharedPreferences = ctx.getSharedPreferences("keystoragesaved", Context.MODE_PRIVATE)

        val url = Constants.instance.YOUTUBE_BASE_URL+"/playlists?part=snippet%2CcontentDetails&maxResults=25&mine=true&access_token=${sharedPreferences.getString("access_token", "")}"

        Log.i("Req to obtain playlist",url)
        val queue = Volley.newRequestQueue(ctx)

        val bodyJSON = JSONObject()
        val request = object : JsonObjectRequest(
            Method.GET, url, bodyJSON,
            Response.Listener { response ->
                if (response != null) {
                    val userPlaylistRecv = PlaylistCategory(response)

                    completion(null, userPlaylistRecv)
                } else {
                    completion(R.string.general_unexpected_error.toString(), null)
                }
            },
            Response.ErrorListener { error ->
                completion(error.toString(), null)
            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers.putAll(getRegularHeaders())
                return headers
            }
        }
        queue.add(request)
    }


    fun requestSelectedPlaylistDetails (context: Context, pListID: String,  nextPageToken: String?, completion: (error: String?, selectedPList: ArrayList<PlaylistItemActivity>?) -> Unit) {
        this.ctx = context.applicationContext
        val sharedPreferences = ctx.getSharedPreferences("keystoragesaved", Context.MODE_PRIVATE)

        var url = Constants.instance.YOUTUBE_BASE_URL+"/playlistItems?part=contentDetails&part=id&part=snippet&part=status&playlistId=${pListID}&access_token=${sharedPreferences.getString("access_token", "")}"
        nextPageToken?.let { url += "&pageToken=$nextPageToken" }

        Log.i("Req selected playlist",url)
        val queue = Volley.newRequestQueue(ctx)

        val bodyJSON = JSONObject()

        val request = object : JsonObjectRequest(
            Method.GET, url, bodyJSON,
            Response.Listener { response ->
                if (response != null) {
                    val responseRecv =  PlaylistCategory(response)
                    for (video in responseRecv.items){
                        userSelectedListRecv.add(video)
                    }

                    //Code section to check if there is another page in the result to fetch before sending back to Activity
                    if (!responseRecv.nextPageToken.isNullOrEmpty()){
                        requestSelectedPlaylistDetails(context, pListID, responseRecv.nextPageToken, completion)
                    }else {
                        completion(null, userSelectedListRecv)
                        userSelectedListRecv.clear()
                    }
                } else {
                    completion(R.string.general_unexpected_error.toString(), null)
                }
            },
            Response.ErrorListener { error ->
                completion(error.toString(), null)
            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers.putAll(getRegularHeaders())
                return headers
            }
        }
        queue.add(request)
    }


    fun requestSearchVideo (context: Context, requestWord: String,  completion: (error: String?, searchResultList: ArrayList<PlaylistItemActivity>?) -> Unit) {
        this.ctx = context.applicationContext
        val sharedPreferences = ctx.getSharedPreferences("keystoragesaved", Context.MODE_PRIVATE)
        val url = Constants.instance.YOUTUBE_BASE_URL+"/search?part=snippet&maxResults=25&q=$requestWord&videoDuration=any&access_token=${sharedPreferences.getString("access_token", "")}"

        Log.i("Req SearchVideo",url)
        val queue = Volley.newRequestQueue(ctx)

        val bodyJSON = JSONObject()
        val request = object : JsonObjectRequest(
            Method.GET, url, bodyJSON,
            Response.Listener { response ->
                if (response != null) {
                    val responseRecv =  PlaylistCategory(response)
                    var userSelectedListRecv = arrayListOf<PlaylistItemActivity>()

                    for (video in responseRecv.items){
                        userSelectedListRecv.add(video)
                    }

                    completion(null, userSelectedListRecv)
                } else {
                    completion(R.string.general_unexpected_error.toString(), null)
                }

            },
            Response.ErrorListener { error ->
                completion(error.toString(), null)
            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers.putAll(getRegularHeaders())
                return headers
            }
        }
        queue.add(request)
    }

}