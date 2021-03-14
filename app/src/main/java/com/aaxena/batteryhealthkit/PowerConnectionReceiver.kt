package com.aaxena.batteryhealthkit

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.BatteryManager
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat

class PowerConnectionReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmServiceIntent = Intent(context, AlarmService :: class.java)
        var alert: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        if (alert == null) {
            alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALL)
        }
        val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPercentInt = ((level / scale.toFloat()) * 100.0 + 0.5).toInt()
        //TODO set the  shared preferences thing here
        val sharedPref = context.getSharedPreferences(context.getString(R.string.sharedPreferencesFile), Context.MODE_PRIVATE)
        val buttonSetting = sharedPref.getBoolean(context.getString(R.string.buttonSetting), false)
        val batteryValue = sharedPref.getInt(context.getString(R.string.batteryValue), 100)
        if (buttonSetting && batteryPercentInt >= batteryValue) {
            val notificationChannel = NotificationChannelCreator()
            notificationChannel.createNotificationChannel(context)
            val notification = NotificationCompat.Builder(context, "273")
                    .setContentTitle("Battery charged till $batteryValue")
                    .setContentText("Swipe to dismiss")
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setAutoCancel(true)
                    .setSound(alert)
                    .build()
            with(NotificationManagerCompat.from(context)) {
                notify(222, notification)
            }
            with (sharedPref.edit()) {
                putBoolean(context.getString(R.string.buttonSetting), false)
                apply()
            }
            notification.flags = notification.flags or Notification.FLAG_INSISTENT or Notification.FLAG_AUTO_CANCEL
            context.stopService(alarmServiceIntent)
        }
    }
}