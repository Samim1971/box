package com.samim.m3uradyo

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

private const val KEY_LAST_NOTIFIED_UPDATE = "last_notified_update"
private const val UPDATE_NOTIFICATION_CHANNEL_ID = "app_updates"
private const val UPDATE_NOTIFICATION_ID = 42

fun maybeShowUpdateNotification(context: Context, updateInfo: UpdateInfo) {
    if (Build.VERSION.SDK_INT >= 33 &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    if (prefs.getString(KEY_LAST_NOTIFIED_UPDATE, null) == updateInfo.latestVersion) {
        return
    }

    ensureUpdateNotificationChannel(context)
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.downloadUrl))
    val contentIntent = PendingIntent.getActivity(
        context,
        5,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val notification = NotificationCompat.Builder(context, UPDATE_NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(android.R.drawable.stat_sys_download_done)
        .setContentTitle("$APP_DISPLAY_NAME yeni surum")
        .setContentText("${updateInfo.latestVersion} indirilebilir")
        .setContentIntent(contentIntent)
        .setAutoCancel(true)
        .setOnlyAlertOnce(true)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()

    NotificationManagerCompat.from(context).notify(UPDATE_NOTIFICATION_ID, notification)
    prefs.edit().putString(KEY_LAST_NOTIFIED_UPDATE, updateInfo.latestVersion).apply()
}

fun ensureUpdateNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= 26) {
        val channel = NotificationChannel(
            UPDATE_NOTIFICATION_CHANNEL_ID,
            "Uygulama guncellemeleri",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
