package com.example.batterystatus.utils

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.batterystatus.R


object NotificationUtils {

    private const val TAG = "NotificationUtils"

    @SuppressLint("MissingPermission")
    fun launchNotification(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        val CHANNEL_ID = "battery_channel_id"
        val NOTIFICATION_ID = 1
        val notificationManager =  NotificationManagerCompat.from(context)
        val soundUri = Uri.parse(
            ("android.resource://" +
                    context.packageName) +
                    "/" +
                    com.example.batterystatus.R.raw.charge_complete
        )

        Log.d(TAG, "launchNotification: soundUri $soundUri")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "battery_channel"
            val descriptionText = "battery_status_channel"
            val importance = NotificationManagerCompat.IMPORTANCE_HIGH
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setLegacyStreamType(AudioManager.STREAM_RING)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .build()

            val channel = NotificationChannelCompat.Builder(CHANNEL_ID, importance)
                .setName(name)
                .setDescription(descriptionText)
                .setSound(soundUri, audioAttributes)
                .build()
            // Register the channel with the system.
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Battery Status")
            .setContentText("Battery Charged to desired level")
            .setSmallIcon(R.drawable.ic_stat_name)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}