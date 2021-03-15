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

open class ConnectionStateMonitor : ConnectivityManager.NetworkCallback() {
    private var networkRequest: NetworkRequest? = null
    private var dialog: Dialog? = null
    private var mActivity: Activity? = null

    fun connectionStateMonitor() {
        Log.i(TAG, "connectionStateMonitor()")
        this.networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
    }

    open fun enable(context: Context) {
        Log.i(TAG,"is enable()")
        this.mActivity = context as Activity

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(this.networkRequest!!, this)

        this.dialog = Dialog(context)
        this.dialog!!.apply {
            setContentView(R.layout.no_internet_connection_msg_error)
            setTitle("Connection Lost")
            setCancelable(false)
        }
    }

    open fun disable(context: Context) {
        Log.i(TAG,"is disable()")
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(this)
    }

    override fun onAvailable(network: Network?) {
        Log.i(TAG,"is onAvailable()")
        if (this.dialog != null && dialog!!.isShowing) {
            this.mActivity!!.runOnUiThread {
                this.dialog!!.dismiss()
            }
        }
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        Log.i(TAG,"is onLost()")
        this.mActivity!!.runOnUiThread {
            if (this.dialog != null) this.dialog!!.show()
        }
    }

    override fun onUnavailable() {
        super.onUnavailable()
        Log.i(TAG,"is onUnavailable()")
        if (this.dialog != null) this.dialog!!.show()
    }

    companion object {
        private const val TAG = "ConnectionStateMonitor"
    }
}