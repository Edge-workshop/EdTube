package dev.kingkongcode.edtube.app.networkconnection

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import dev.kingkongcode.edtube.R

/**
 * Only for api >= 29
 * */

const val TAG = "ConnectionStateMonitor"

open class ConnectionStateMonitor : ConnectivityManager.NetworkCallback() {
    private var networkRequest: NetworkRequest? = null
    private var dialog: Dialog? = null
    private var mActivity: Activity? = null

    fun connectionStateMonitor() {
        networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
    }

    open fun enable(context: Context) {
        Log.i(TAG,"is enable")
        mActivity = context as Activity

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(networkRequest, this)

        dialog = Dialog(context)
        dialog!!.setContentView(R.layout.no_internet_connection_msg_error)
        dialog!!.setTitle("Connection Lost")
        dialog!!.setCancelable(false)
    }

    open fun disable(context: Context) {
        Log.i(TAG,"is disable")
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(this)
    }

    override fun onAvailable(network: Network?) {
        Log.i(TAG,"is onAvailable")
        if (dialog != null && dialog!!.isShowing) {
            mActivity!!.runOnUiThread {
                dialog!!.dismiss()
            }
        }
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        Log.i(TAG,"is onLost")
        mActivity!!.runOnUiThread {
            if (dialog != null) dialog!!.show()
        }
    }

    override fun onUnavailable() {
        super.onUnavailable()
        Log.i(TAG,"is onUnavailable")
        if (dialog != null) dialog!!.show()
    }
}