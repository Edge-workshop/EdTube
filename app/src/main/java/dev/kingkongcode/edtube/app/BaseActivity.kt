package dev.kingkongcode.edtube.app

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import dev.kingkongcode.edtube.app.networkconnection.ConnectionStateMonitor
import dev.kingkongcode.edtube.app.networkconnection.NetworkStateConnection

open class BaseActivity : AppCompatActivity() {
    // TODO refactor class
    private lateinit var nc: NetworkStateConnection //Only used if api < 29
    private lateinit var cs: ConnectionStateMonitor // Only used if api >= 29

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate()")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            this.cs = ConnectionStateMonitor()
            this.cs.connectionStateMonitor()
        } else this.nc = NetworkStateConnection(this@BaseActivity)
    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "onStart()")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.cs.enable(this@BaseActivity)
        } else {
            val intentFilter1 = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            registerReceiver(this.nc, intentFilter1)
        }
    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG, "onStop()")
        //Stop BroadCastReceiver network connection for Mobile Data
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) this.cs.disable(this@BaseActivity) else unregisterReceiver(this.nc)
    }

    companion object {
        private const val TAG = "BaseActivity"
    }

}