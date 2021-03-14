package com.aaxena.batteryhealthkit

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import timber.log.Timber

class NotificationChannelCreator {
    fun createNotificationChannel(context: Context) {
        Timber.i("Created Notification Channel")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            run {
                val name = context.getString(R.string.battery_full_notification_name)
                val descriptionText = context.getString(R.string.battery_full_notification_description)
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val mChannel = NotificationChannel("273", name, importance)
                mChannel.description = descriptionText
                val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(mChannel)
            }
            run {
                val name = context.getString(R.string.alarm_set_notification_name)
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val mChannel = NotificationChannel("274", name, importance)
                val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(mChannel)
            }
        }
    }
}