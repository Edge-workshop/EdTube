package dev.kingkongcode.edtube.app.server

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import dev.kingkongcode.edtube.R
import dev.kingkongcode.edtube.model.PlaylistCategory
import dev.kingkongcode.edtube.model.PlaylistItem
import dev.kingkongcode.edtube.app.Constants
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class APIManager {
    private var accessToken = Constants.EMPTY_STRING
    private var userSelectedListRecv = arrayListOf<PlaylistItem>()

    companion object{
        var instance: APIManager = APIManager()
        private const val SHARED_PROFILE = "keystoragesaved"
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
        val ctx = context.applicationContext
        val url = Constants.GOOGLE_AUTH2
        Log.i("OAuth api accessToken",url)
        val queue = Volley.newRequestQueue(ctx)

        val bodyJSON = JSONObject()
        bodyJSON.apply {
            put("grant_type", "authorization_code")
            put("client_id", Config.CLIENT_ID)
            put("client_secret", Config.CLIENT_SECRET)
            put("redirect_uri", Constants.EMPTY_STRING)
            put("code", deviceCode)
            put("id_token", idToken)
        }

        val request = object : JsonObjectRequest(
            Method.POST, url, bodyJSON,
            Response.Listener { response ->
                if (response != null) {
                    this.accessToken = response.optString("access_token")

                    val sharedPreferences: SharedPreferences = ctx.getSharedPreferences(
                        SHARED_PROFILE, Context.MODE_PRIVATE)
                    val editor: SharedPreferences.Editor = sharedPreferences.edit()
                    editor.apply {
                        putString("refresh_token",response.optString("refresh_token"))
                        putString("id_token",response.optString("id_token"))
                        putString("access_token",response.optString("access_token"))
                        putInt("expires_in",response.optInt("expires_in"))
                        putString("token_type",response.optString("token_type"))
                        apply()
                        //commit()
                    }

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
        val ctx = context.applicationContext
        val sharedPreferences = ctx.getSharedPreferences(SHARED_PROFILE, Context.MODE_PRIVATE)
        val url = Constants.YOUTUBE_BASE_URL+"/playlists?part=snippet&part=contentDetails&maxResults=25&mine=true&access_token=${sharedPreferences.getString("access_token", "")}"

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

    fun requestSelectedPlaylistDetails (context: Context, pListID: String,  nextPageToken: String?, completion: (error: String?, selectedPList: ArrayList<PlaylistItem>?) -> Unit) {
        val ctx = context.applicationContext
        val sharedPreferences = ctx.getSharedPreferences(SHARED_PROFILE, Context.MODE_PRIVATE)
        var url = Constants.YOUTUBE_BASE_URL+"/playlistItems?part=contentDetails&part=id&part=snippet&part=status&playlistId=${pListID}&access_token=${sharedPreferences.getString("access_token", "")}"
        Log.i("Req selected playlist",url)
        val queue = Volley.newRequestQueue(ctx)

        nextPageToken?.let { url += "&pageToken=$nextPageToken" }

        val bodyJSON = JSONObject()
        val request = object : JsonObjectRequest(
            Method.GET, url, bodyJSON,
            Response.Listener { response ->
                if (response != null) {
                    val responseRecv =  PlaylistCategory(response)
                    for (video in responseRecv.items) {
                        userSelectedListRecv.add(video)
                    }

                    //Code section to check if there is another page in the result to fetch before sending back to Activity
                    if (responseRecv.nextPageToken.isNotEmpty()) {
                        requestSelectedPlaylistDetails(context, pListID, responseRecv.nextPageToken, completion)
                    } else {
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

    fun requestGetVideoDuration (context: Context, allVideoId: ArrayList<String>, completion: (error: String?, durationVideoList: ArrayList<Pair<String, String>>?) -> Unit) {
        val ctx = context.applicationContext
        val sharedPreferences = ctx.getSharedPreferences(SHARED_PROFILE, Context.MODE_PRIVATE)
        val queue = Volley.newRequestQueue(ctx)
        var strVideoId = Constants.EMPTY_STRING

        for (videoId in allVideoId) {
            if (strVideoId.isNotEmpty()) {
                strVideoId += ","
            }

            strVideoId += videoId
        }

        val url = Constants.YOUTUBE_BASE_URL+"/videos?part=contentDetails&id=$strVideoId&access_token=${sharedPreferences.getString("access_token", "")}"
        Log.i("Req video duration",url)

        val bodyJSON = JSONObject()
        val request = object : JsonObjectRequest(
            Method.GET, url, bodyJSON,
            Response.Listener { response ->
                if (response != null) {
                    val responseRecv = durationListRetriever(response)
                    completion(null, responseRecv)
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

    fun requestSearchVideo (context: Context, requestWord: String,  completion: (error: String?, searchResultList: ArrayList<PlaylistItem>?) -> Unit) {
        val ctx = context.applicationContext
        val sharedPreferences = ctx.getSharedPreferences(SHARED_PROFILE, Context.MODE_PRIVATE)
        val url = Constants.YOUTUBE_BASE_URL+"/search?part=snippet&maxResults=25&q=$requestWord&videoDuration=any&access_token=${sharedPreferences.getString("access_token", "")}"
        Log.i("Req SearchVideo",url)
        val queue = Volley.newRequestQueue(ctx)

        val bodyJSON = JSONObject()
        val request = object : JsonObjectRequest(
            Method.GET, url, bodyJSON,
            Response.Listener { response ->
                if (response != null) {
                    val responseRecv = PlaylistCategory(response)
                    val userSelectedListRecv = arrayListOf<PlaylistItem>()

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

    private fun durationListRetriever(resp: JSONObject) : ArrayList<Pair<String, String>> {
        val complDurationList = arrayListOf<Pair<String, String>>()

        resp.optJSONArray("items")?.let {
            for (i in 0 until it.length()) {
                val jsonObject = it.optJSONObject(i)
                val videoId = jsonObject.optString("id")//put into variable
                val secondJsonObj = jsonObject.optJSONObject("contentDetails") ?: JSONObject()
                val duration = secondJsonObj.optString("duration")//put into variable

                val tempPair = Pair(videoId, duration)
                complDurationList.add(tempPair)
            }
        }

        return complDurationList
    }
}