package edu.bluejack22_1.fidertime.common

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import edu.bluejack22_1.fidertime.models.*
import java.util.*

class NotificationHelper {
    companion object {
        fun createNotification(context: Context, title: String, message: String, time: Long) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(context.applicationContext, Notification::class.java)
                intent.putExtra(titleExtra, title)
                intent.putExtra(messageExtra, message)

                val pendingIntent = PendingIntent.getBroadcast(
                    context.applicationContext,
                    notificationId,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    time,
                    pendingIntent
                )
            }
        }

        fun createNotification(context: Context, title: String, message: String) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(context.applicationContext, Notification::class.java)
                intent.putExtra(titleExtra, title)
                intent.putExtra(messageExtra, message)

                val pendingIntent = PendingIntent.getBroadcast(
                    context.applicationContext,
                    notificationId,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val time = Calendar.getInstance().timeInMillis
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    time,
                    pendingIntent
                )
            }
        }

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "New chat channel"
                val description = "Fire when new chat received"
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(channelId, name, importance)
                channel.description = description
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}