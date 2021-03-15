package dev.kingkongcode.edtube.app.networkconnection

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log
import dev.kingkongcode.edtube.R

open class NetworkStateConnection(context: Context) : BroadcastReceiver() {
    private var dialog: Dialog? = null

    init {
        this.dialog = Dialog(context)
        this.dialog!!.apply {
            setContentView(R.layout.no_internet_connection_msg_error)
            setTitle("Connection Lost")
            setCancelable(false)
        }
    }

    // TODO deprecated part to be change
    override fun onReceive(context: Context, intent: Intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION == intent.action) {
            val noConnectivity = intent.getBooleanExtra(
                ConnectivityManager.EXTRA_NO_CONNECTIVITY, false
            )
            if (noConnectivity) {
                Log.i(TAG, "Disconnected")
                //Disconnected
                if (this.dialog != null) this.dialog!!.show()
            } else {
                Log.i(TAG, "Connected")
                //Connected
                if (this.dialog != null && this.dialog!!.isShowing) this.dialog!!.dismiss()
            }
        } else Log.i(TAG, "Not connected at all")
    }

    companion object {
        private const val TAG = "NetworkStateConnection"
    }
}