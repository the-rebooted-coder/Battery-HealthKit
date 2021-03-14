package com.aaxena.batteryhealthkit

import android.app.IntentService
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.support.v4.app.NotificationCompat
import timber.log.Timber

class AlarmService: IntentService("com.aaxena.batteryhealthkit.AlarmService") {

    private val br = PowerConnectionReceiver()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationIntent = Intent(this, MainActivity :: class.java)
        val pendingNotificationIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val notificationChannelCreator = NotificationChannelCreator()
        notificationChannelCreator.createNotificationChannel(applicationContext)
        val mBuilder = NotificationCompat.Builder(this, "273")
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle("Alert is set")
                .setContentText("It will sound when the battery reaches the set amount. " +
                        "Click here to remove the alert")
                .setAutoCancel(true)
                .setContentIntent(pendingNotificationIntent)
                .setOngoing(true)
                .build()
        startForeground(15, mBuilder)
        Timber.i("Service Started")
        val filter = IntentFilter(BatteryManager.EXTRA_STATUS).apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }
        registerReceiver(br, filter)
        return Service.START_STICKY
    }

    override fun onHandleIntent(intent: Intent?) {
//        while (true) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(br)
        } catch (e: IllegalArgumentException) {
            Timber.i("Exception in onDestroy")
        }
        Timber.i("Service Killed")
    }
}