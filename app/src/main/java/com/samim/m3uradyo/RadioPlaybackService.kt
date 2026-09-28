package com.samim.m3uradyo

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

private const val NOTIFICATION_CHANNEL_ID = "radio_playback"
private const val NOTIFICATION_ID = 41
private const val ACTION_PREVIOUS = "com.samim.m3uradyo.PREVIOUS"
private const val ACTION_TOGGLE = "com.samim.m3uradyo.TOGGLE"
private const val ACTION_STOP = "com.samim.m3uradyo.STOP"
private const val ACTION_NEXT = "com.samim.m3uradyo.NEXT"
private const val ACTION_SERVICE_UPDATE = "com.samim.m3uradyo.SERVICE_UPDATE"
private const val ACTION_SERVICE_STOP = "com.samim.m3uradyo.SERVICE_STOP"
private const val EXTRA_STATION_NAME = "station_name"
private const val EXTRA_STATION_URL = "station_url"
private const val EXTRA_STATION_LOGO = "station_logo"
private const val EXTRA_STATION_CATEGORY = "station_category"
private const val EXTRA_IS_PLAYING = "is_playing"

object RadioControlBridge {
    var onPrevious: (() -> Unit)? = null
    var onToggle: (() -> Unit)? = null
    var onStop: (() -> Unit)? = null
    var onNext: (() -> Unit)? = null
}

class RadioControlReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_PREVIOUS -> RadioControlBridge.onPrevious?.invoke()
            ACTION_TOGGLE -> RadioControlBridge.onToggle?.invoke()
            ACTION_STOP -> RadioControlBridge.onStop?.invoke()
            ACTION_NEXT -> RadioControlBridge.onNext?.invoke()
        }
    }
}

class RadioPlaybackService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_SERVICE_STOP) {
            stopForegroundCompat()
            stopSelf()
            return START_NOT_STICKY
        }

        val station = intent?.toRadioStation()
        val isPlaying = intent?.getBooleanExtra(EXTRA_IS_PLAYING, false) ?: false
        if (station == null) {
            stopForegroundCompat()
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = buildPlaybackNotification(this, station, isPlaying)
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stopForegroundCompat()
        super.onDestroy()
    }

    private fun stopForegroundCompat() {
        if (Build.VERSION.SDK_INT >= 24) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }
}
fun playStation(player: ExoPlayer, station: RadioStation): Boolean {
    if (station.streamUrl.isBlank()) {
        return false
    }

    return runCatching {
        player.stop()
        player.clearMediaItems()
        player.setMediaItem(MediaItem.fromUri(station.streamUrl.trim()))
        player.prepare()
        player.play()
    }.isSuccess
}
fun updatePlaybackService(context: Context, station: RadioStation, isPlaying: Boolean) {
    val intent = Intent(context, RadioPlaybackService::class.java).apply {
        action = ACTION_SERVICE_UPDATE
        putStationExtras(station)
        putExtra(EXTRA_IS_PLAYING, isPlaying)
    }
    if (Build.VERSION.SDK_INT >= 26) {
        ContextCompat.startForegroundService(context, intent)
    } else {
        context.startService(intent)
    }
}

fun stopPlaybackService(context: Context) {
    context.stopService(Intent(context, RadioPlaybackService::class.java))
    cancelPlaybackNotification(context)
}

fun Intent.putStationExtras(station: RadioStation) {
    putExtra(EXTRA_STATION_NAME, station.name)
    putExtra(EXTRA_STATION_URL, station.streamUrl)
    putExtra(EXTRA_STATION_LOGO, station.logoUrl)
    putExtra(EXTRA_STATION_CATEGORY, station.category)
}

fun Intent.toRadioStation(): RadioStation? {
    val name = getStringExtra(EXTRA_STATION_NAME)?.takeIf { it.isNotBlank() } ?: return null
    val streamUrl = getStringExtra(EXTRA_STATION_URL)?.takeIf { it.isNotBlank() } ?: return null
    return RadioStation(
        name = name,
        streamUrl = streamUrl,
        logoUrl = getStringExtra(EXTRA_STATION_LOGO),
        category = getStringExtra(EXTRA_STATION_CATEGORY) ?: "Genel"
    )
}

fun notificationActionIntent(context: Context, action: String, requestCode: Int): PendingIntent {
    val intent = Intent(context, RadioControlReceiver::class.java).setAction(action)
    return PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
}

fun showLockScreenNotification(
    context: Context,
    station: RadioStation,
    isPlaying: Boolean
) {
    if (Build.VERSION.SDK_INT >= 33 &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    val notification = buildPlaybackNotification(context, station, isPlaying)
    NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
}

fun buildPlaybackNotification(
    context: Context,
    station: RadioStation,
    isPlaying: Boolean
): Notification {
    ensureNotificationChannel(context)

    val activityIntent = Intent(context, MainActivity::class.java)
    val contentIntent = PendingIntent.getActivity(
        context,
        0,
        activityIntent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val playPauseIcon = if (isPlaying) {
        R.drawable.ic_notify_pause
    } else {
        R.drawable.ic_notify_play
    }
    val playPauseTitle = if (isPlaying) "Pause" else "Play"

    val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_media_play)
        .setContentTitle(station.name)
        .setContentText(if (isPlaying) "Caliniyor" else "Duraklatildi")
        .setContentIntent(contentIntent)
        .setOngoing(isPlaying)
        .setOnlyAlertOnce(true)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .addAction(
            R.drawable.ic_notify_previous,
            "Onceki",
            notificationActionIntent(context, ACTION_PREVIOUS, 1)
        )
        .addAction(
            playPauseIcon,
            playPauseTitle,
            notificationActionIntent(context, ACTION_TOGGLE, 2)
        )
        .addAction(
            R.drawable.ic_notify_stop,
            "Stop",
            notificationActionIntent(context, ACTION_STOP, 3)
        )
        .addAction(
            R.drawable.ic_notify_next,
            "Sonraki",
            notificationActionIntent(context, ACTION_NEXT, 4)
        )
        .setStyle(
            androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2)
        )
        .build()

    return notification
}

fun ensureNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= 26) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Radyo oynatma",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}

fun cancelPlaybackNotification(context: Context) {
    NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
}
