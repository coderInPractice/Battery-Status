package com.example.batterystatus

import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.WorkManager
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var mBatterySeekBar : SeekBar
    private lateinit var mBatteryStatusText : TextView
    private lateinit var mSharedPreferences : SharedPreferences

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                isGranted: Boolean ->
            if (!isGranted) {
                Toast.makeText(applicationContext, "Permission is required to launch notifications",
                    Toast.LENGTH_SHORT).show()
            }
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Check if the permission is already granted.
        if (ContextCompat.checkSelfPermission(
                this,android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "onCreate: permission is already granted")
        } else {
            // Request the permission.
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }


        mBatterySeekBar = findViewById(R.id.battery_seekBar)
        mBatteryStatusText = findViewById(R.id.seekBar_value)

        findViewById<TextView>(R.id.worker_status).text = WorkManager.getInstance(this)
            .getWorkInfosByTag("batteryStatus").isCancelled.toString()

        mSharedPreferences = getSharedPreferences("desiredBatteryValue", MODE_PRIVATE)
        val seekBarValue = mSharedPreferences.getInt("seekBarValue", 0)
        Log.d(TAG, "onCreate: seekBarValue $seekBarValue")
        mBatterySeekBar.progress = seekBarValue
        mBatteryStatusText.text = seekBarValue.toString()

        val batteryStatus: Intent? = registerReceiver(null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL

        if (isCharging) {
            mBatterySeekBar.isEnabled = false
        }

        mBatterySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                mBatteryStatusText.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                //Do nothing
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val currentBatteryLevel = level * 100 / scale
                if (seekBar?.progress!! > currentBatteryLevel) {
                    getSharedPreferences("desiredBatteryValue", MODE_PRIVATE).edit()
                        .putInt("seekBarValue", seekBar.progress)
                        .apply()
                    Snackbar.make(findViewById(android.R.id.content),
                        "Desired Battery Level Set ${seekBar.progress}",
                        Snackbar.LENGTH_SHORT).show()
                } else
                    Snackbar.make(findViewById(android.R.id.content),
                        "Seek Value is less than current battery level",
                        Snackbar.LENGTH_SHORT).show()
            }

        })
    }

    override fun onResume() {
        super.onResume()
        val seekBarValue = mSharedPreferences.getInt("seekBarValue", 0)
        Log.d(TAG, "onResume: seekBarValue $seekBarValue")
        mBatterySeekBar.progress = seekBarValue
        mBatteryStatusText.text = seekBarValue.toString()
    }
}