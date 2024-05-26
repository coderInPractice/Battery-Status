package com.example.batterystatus

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.batterystatus.broadcastReceiver.BatteryConnectionStatusReceiver

class BatteryApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED)
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED)
        registerReceiver(BatteryConnectionStatusReceiver(), intentFilter)
    }

    override fun onTerminate() {
        super.onTerminate()
        unregisterReceiver(BatteryConnectionStatusReceiver())
    }

}