package dev.kingkongcode.edtube.util

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.ConnectivityManager
import android.widget.Toast
import dev.kingkongcode.edtube.R

class NetworkStateConnection: BroadcastReceiver() {

    private val TAG = "NetworkStateConnection"

    private var ourInstance: NetworkStateConnection? = null
    private var isConnected = false
    private var connectActStr: String = ""
    private var connectMobStr: String = ""
    private var dialog: Dialog? = null


    fun getConnectActStr(): String? {
        return connectActStr
    }

    fun setConnectActStr(connectActStr: String) {
        this.connectActStr = connectActStr
    }

    fun getConnectMobStr(): String? {
        return connectMobStr
    }

    fun setConnectMobStr(connectMobStr: String) {
        this.connectMobStr = connectMobStr
    }

    fun isConnected(): Boolean {
        return isConnected
    }

    fun setConnected(connected: Boolean) {
        isConnected = connected
    }

    fun getInstance(): NetworkStateConnection? {
        return ourInstance
    }

    fun NetworkStateConnection(context: Context?) {
        dialog = Dialog(context!!)
        dialog!!.setContentView(R.layout.no_internet_connection_msg_error)
        dialog!!.setTitle("Connection Lost")
        dialog!!.setCancelable(false)
    }
//TODO finish network connection

//    fun getOurInstance(context: Context?): NetworkStateConnection? {
//        if (ourInstance == null) ourInstance = NetworkStateConnection(context)
//        return ourInstance
//    }

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
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //when gps is on
        } else {
            //when gps is off
            Toast.makeText(context, "Please switch on the GPS", Toast.LENGTH_LONG).show()

        }
    }


}