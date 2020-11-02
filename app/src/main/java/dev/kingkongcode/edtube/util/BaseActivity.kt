package dev.kingkongcode.edtube.util

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dev.kingkongcode.edtube.util.network.ConnectionStateMonitor
import dev.kingkongcode.edtube.util.network.NetworkStateConnection

open class BaseActivity : AppCompatActivity() {
    private lateinit var nc: NetworkStateConnection //Only used if api < 29
    private lateinit var cs: ConnectionStateMonitor // Only used if api >= 29

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            cs = ConnectionStateMonitor()
            cs.connectionStateMonitor()
        } else nc = NetworkStateConnection().NetworkStateConnection(this@BaseActivity)
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cs.enable(this@BaseActivity)
        } else {
            val intentFilter1 = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            registerReceiver(nc, intentFilter1)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        //Stop BroadCastReceiver network connection for Mobile Data
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) cs.disable(this@BaseActivity) else unregisterReceiver(nc)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}