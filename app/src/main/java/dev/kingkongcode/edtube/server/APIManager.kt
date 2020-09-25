package dev.kingkongcode.edtube.server

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import dev.kingkongcode.edtube.model.PlaylistCategory
import dev.kingkongcode.edtube.model.PlaylistItemActivity
import dev.kingkongcode.edtube.util.Constants
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class APIManager {

    private lateinit var ctx: Context

    private val sharedPrefFile = "keystoragesaved"

    private val unexpectedError = "Une erreur inattendue est survenue, veuillez réessayer plus tard"

    private var accessToken = ""

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

        val url = "https://www.googleapis.com/oauth2/v4/token"

        Log.i("OAuth api accessToken",url)
        val queue = Volley.newRequestQueue(ctx)

        val bodyJSON = JSONObject()
        bodyJSON.put("grant_type", "authorization_code")
        bodyJSON.put("client_id", Config.current.CLIENT_ID)
        bodyJSON.put("client_secret", Config.current.CLIENT_SECRET)
        bodyJSON.put("redirect_uri", "")
        bodyJSON.put("code", deviceCode)
        bodyJSON.put("id_token", idToken)

        val request = object : JsonObjectRequest(
            Method.POST, url, bodyJSON,
            Response.Listener { response ->
                if (response != null) {

                    val refreshToken = response.optString("refresh_token")
                    val idToken = response.optString("id_token")
                    val accessToken = response.optString("access_token")
                    val expireIn = response.optInt("expires_in")
                    val tokenType = response.optString("token_type")
                    this.accessToken = accessToken

                    val sharedPreferences: SharedPreferences = this.ctx.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
                    val editor:SharedPreferences.Editor =  sharedPreferences.edit()
                    editor.putString("refresh_token",refreshToken)
                    editor.putString("id_token",idToken)
                    editor.putString("access_token",accessToken)
                    editor.putInt("expires_in",expireIn)
                    editor.putString("token_type",tokenType)
                    editor.apply()
                    editor.commit()

                    completion(null)

                } else {
                    completion(unexpectedError)
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
                    completion(unexpectedError, null)
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


    fun requestSelectedPlaylistDetails (context: Context, pListID: String,  completion: (error: String?, selectedPList: ArrayList<PlaylistItemActivity>?) -> Unit) {
        this.ctx = context.applicationContext
        val sharedPreferences = ctx.getSharedPreferences("keystoragesaved", Context.MODE_PRIVATE)

        val url = Constants.instance.YOUTUBE_BASE_URL+"/playlistItems?part=contentDetails&part=id&part=snippet&part=status&playlistId=${pListID}&access_token=${sharedPreferences.getString("access_token", "")}"

        Log.i("Req selected playlist",url)
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
                    completion(unexpectedError, null)
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