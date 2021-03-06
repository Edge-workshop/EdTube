package dev.kingkongcode.edtube.app.networkconnection

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import dev.kingkongcode.edtube.R

open class NetworkStateConnection : BroadcastReceiver() {
    private var dialog: Dialog? = null

    fun NetworkStateConnection(context: Context?) : NetworkStateConnection {
        dialog = Dialog(context!!)
        dialog!!.setContentView(R.layout.no_internet_connection_msg_error)
        dialog!!.setTitle("Connection Lost")
        dialog!!.setCancelable(false)
        return this
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION == intent.action) {
            val noConnectivity = intent.getBooleanExtra(
                ConnectivityManager.EXTRA_NO_CONNECTIVITY, false
            )
            if (noConnectivity) {
                //Disconnected
                if (dialog != null) dialog!!.show()
            } else {
                //Connected
                if (dialog != null && dialog!!.isShowing) dialog!!.dismiss()
            }
        }
    }
}