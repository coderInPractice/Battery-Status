package com.example.batterystatus.worker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.batterystatus.R
import com.example.batterystatus.utils.NotificationUtils


class BatteryStatusWorker (context:Context, workerParameters: WorkerParameters)
    : Worker(context, workerParameters) {

    private val mContext = context
    private val mWorkParameters = workerParameters


    companion object {
        private const val TAG = "BatteryStatusWorker"
    }

    override fun doWork(): Result {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            mContext.registerReceiver(null, ifilter)
        }

        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val batteryPct: Int? = batteryStatus?.let { intent ->
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            level * 100 / scale
        }

        Log.d(TAG, "doWork: batteryStatus $status")
        Log.d(TAG, "doWork: batteryPct $batteryPct")

        val desiredBattery = mContext.getSharedPreferences("desiredBatteryValue",
            AppCompatActivity.MODE_PRIVATE
        ).getInt("seekBarValue", 0)

        Log.d(TAG, "doWork: seekBar value $desiredBattery")

        if (batteryPct!! >= desiredBattery) {
            Log.d(TAG, "doWork: battery charged to desired level")
            NotificationUtils.launchNotification(mContext)
        }

        return Result.success()
    }
}