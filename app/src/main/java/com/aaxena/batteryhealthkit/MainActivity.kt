package com.aaxena.batteryhealthkit

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PorterDuff
import android.os.BatteryManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import me.itangqi.waveloadingview.WaveLoadingView
import timber.log.Timber


class MainActivity : AppCompatActivity() {

    lateinit var waveLoadingView: WaveLoadingView
    lateinit var batteryPercentageTextView: TextView
    lateinit var batterySeekBar: SeekBar
    lateinit var seekBarValueTextView: TextView
    lateinit var alarmButton: Button
    lateinit var infoTextView: TextView

    private val batteryStatusBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val status: Int = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING
                    || status == BatteryManager.BATTERY_STATUS_FULL

            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryPercentInt = ((level / scale.toFloat()) * 100.0 + 0.5).toInt()
            val batteryPercentString = "Battery Level: $batteryPercentInt%"
            batteryPercentageTextView.text = batteryPercentString
            waveLoadingView.progressValue = (batteryPercentInt)

            //TODO find a way to shift this code in onStart with rxKotlin

            if (isCharging) {
                waveLoadingView.setAnimDuration(1000)
            }else {
                waveLoadingView.setAnimDuration(2000)
            }
            if (batteryPercentInt in 0..41) {
                alarmButton.apply {
                    setTextColor(ContextCompat.getColor(context, R.color.white))
                }
            } else {
                alarmButton.apply {
                    setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                    setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                }
            }
            if (batteryPercentInt in 0..51) {
                batterySeekBar.apply {
                    progressDrawable.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark), PorterDuff.Mode.MULTIPLY)
                    thumb.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP)
                }
                seekBarValueTextView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
            } else {
                batterySeekBar.apply {
                    progressDrawable.setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.MULTIPLY)
                    thumb.setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_ATOP)
                }
                seekBarValueTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            if (batteryPercentInt in 0..59) {
                infoTextView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
            } else {
                infoTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            if (batteryPercentInt in 0..64) {
                batteryPercentageTextView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
            } else {
                batteryPercentageTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            if (batteryPercentInt in 0..15) {
                waveLoadingView.waveColor = ContextCompat.getColor(context, R.color.maroon)
            } else {
                waveLoadingView.waveColor = ContextCompat.getColor(context, android.R.color.holo_blue_dark)
            }
            Timber.i("Receiver Called")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val context = this
        waveLoadingView = findViewById(R.id.waveLoadingView)
        batteryPercentageTextView = findViewById(R.id.batteryPercentageTextView)
        batterySeekBar = findViewById(R.id.batterySeekBar)
        seekBarValueTextView = findViewById(R.id.seekBarValueTextView)
        alarmButton = findViewById(R.id.alarmButton)
        infoTextView = findViewById(R.id.infoTextView)
        var seekBarValue = 100

        //TODO create ringtone selector
        batterySeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progressValue: Int, p2: Boolean) {
                val displayString = "$progressValue%"
                seekBarValue = progressValue
                seekBarValueTextView.text = displayString
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })
        alarmButton.setOnClickListener {
            val sharedPref = this.getSharedPreferences(getString(R.string.sharedPreferencesFile), Context.MODE_PRIVATE)
            val buttonSetting = sharedPref.getBoolean(getString(R.string.buttonSetting), false)

            if(buttonSetting) {
                with(sharedPref.edit()) {
                    putBoolean(getString(R.string.buttonSetting), false)
                    apply()
                }
                alarmButton.setText(R.string.setAlert)
                Toast.makeText(context, "Alert Removed", Toast.LENGTH_SHORT).show()
                val stopBroadcastServiceIntent = Intent(this, AlarmService::class.java)
                batterySeekBar.isEnabled = true
                stopService(stopBroadcastServiceIntent)
            } else {
                with(sharedPref.edit()) {
                    putBoolean(getString(R.string.buttonSetting), true)
                    putInt(getString(R.string.batteryValue), seekBarValue)
                    apply()
                }
                alarmButton.setText(R.string.removeAlert)
                batterySeekBar.isEnabled = false
                Toast.makeText(context, "Alert Set", Toast.LENGTH_SHORT).show()
                val startAlarmService = Intent(this, AlarmService::class.java)
                if  (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    startForegroundService(startAlarmService)
                else
                    startService(startAlarmService)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val sharedPref = this.getSharedPreferences(getString(R.string.sharedPreferencesFile), Context.MODE_PRIVATE)
        val buttonSetting = sharedPref.getBoolean(getString(R.string.buttonSetting), false)
        val batteryValue = sharedPref.getInt(getString(R.string.batteryValue), 0)
        if(buttonSetting) {
            alarmButton.setText(R.string.removeAlert)
            val displayString = "$batteryValue%"
            seekBarValueTextView.text = displayString
            batterySeekBar.isEnabled = false
            batterySeekBar.progress = batteryValue
        } else {
            alarmButton.setText(R.string.setAlert)
            val displayString = "${batterySeekBar.progress}%"
            seekBarValueTextView.text = displayString
            batterySeekBar.isEnabled = true
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        this.registerReceiver(batteryStatusBroadcastReceiver, filter)
        Timber.i("OnStart Works")
    }

    override fun onStop() {
        super.onStop()
        this.unregisterReceiver(batteryStatusBroadcastReceiver)
    }
}
