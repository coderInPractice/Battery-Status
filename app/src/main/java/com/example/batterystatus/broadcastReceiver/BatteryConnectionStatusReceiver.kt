package com.example.batterystatus.broadcastReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.example.batterystatus.worker.BatteryStatusWorker
import java.util.concurrent.TimeUnit

class BatteryConnectionStatusReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("BatteryConnectionStatusReceiver", "onReceive: action received ${intent?.action}")
        if (intent?.action.equals(Intent.ACTION_POWER_CONNECTED)) {
            initiateBatteryStatusCheck(context!!)
        }

        else if (intent?.action.equals(Intent.ACTION_POWER_DISCONNECTED))
            WorkManager.getInstance(context!!).cancelUniqueWork("batteryWorker")

    }

    private fun initiateBatteryStatusCheck(context: Context?) {
        val batteryStatus: Intent? = context?.registerReceiver(null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        val chargePlug: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val usbCharge: Boolean = chargePlug == BatteryManager.BATTERY_PLUGGED_USB

        Log.d("BatteryConnectionStatusReceiver", "initiateBatteryStatusCheck: usbCharge $usbCharge")

        if (!usbCharge) {
            val workRequest = PeriodicWorkRequest.Builder(BatteryStatusWorker::class.java, 15, TimeUnit.MINUTES)
                .addTag("batteryStatus")
                .build()
            WorkManager.getInstance(context!!).enqueueUniquePeriodicWork("batteryWorker",
                ExistingPeriodicWorkPolicy.UPDATE, workRequest)
        }

    }



}