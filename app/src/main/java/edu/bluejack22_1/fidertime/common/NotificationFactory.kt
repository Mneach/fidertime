package edu.bluejack22_1.fidertime.common

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import edu.bluejack22_1.fidertime.R

class NotificationFactory {

    constructor(context: Context, title: String, text: String) {
        var builder = NotificationCompat.Builder(context, "")
            .setSmallIcon(R.drawable.chat_colored)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(1, builder.build())
        }
    }

}