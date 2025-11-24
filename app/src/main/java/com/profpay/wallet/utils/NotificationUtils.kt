package com.profpay.wallet.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.profpay.wallet.MainActivity
import me.pushy.sdk.Pushy
import kotlin.random.Random

object NotificationUtils {
    fun showNotification(
        context: Context,
        title: String,
        text: String
    ) {
        val channelId = "PUSHY_SERVICE_CHANNEL"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelName = "Pushy Channel Service"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val notificationChannel = NotificationChannel(channelId, channelName, importance)

        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.enableVibration(true)
        notificationChannel.vibrationPattern = longArrayOf(0, 400, 250, 400)
        notificationManager.createNotificationChannel(notificationChannel)

        val builder =
            NotificationCompat.Builder(context, channelId)
                .setAutoCancel(true)
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentTitle(title)
                .setContentText(text)
                .setLights(Color.RED, 1000, 1000)
                .setVibrate(longArrayOf(0, 400, 250, 400))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(
                    PendingIntent.getActivity(
                        context,
                        0,
                        Intent(context, MainActivity::class.java),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                    )
                )

        Pushy.setNotificationChannel(builder, context)
        notificationManager.notify(Random.nextInt(100_000), builder.build())
    }
}
