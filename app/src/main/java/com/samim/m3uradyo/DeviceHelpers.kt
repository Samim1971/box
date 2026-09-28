package com.samim.m3uradyo

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt


fun mediaAudioManager(context: Context): AudioManager =
    context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

fun mediaVolumeRange(context: Context): IntRange {
    val audioManager = mediaAudioManager(context)
    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    val minVolume = if (Build.VERSION.SDK_INT >= 28) {
        audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)
    } else {
        0
    }
    return minVolume..maxVolume
}

fun getMediaVolumeRatio(context: Context): Float {
    val audioManager = mediaAudioManager(context)
    val range = mediaVolumeRange(context)
    val span = (range.last - range.first).coerceAtLeast(1)
    val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).coerceIn(range.first, range.last)
    return ((current - range.first).toFloat() / span.toFloat()).coerceIn(0f, 1f)
}

fun setMediaVolumeRatio(context: Context, value: Float): Float {
    val audioManager = mediaAudioManager(context)
    val range = mediaVolumeRange(context)
    val span = (range.last - range.first).coerceAtLeast(1)
    val targetVolume = (range.first + (value.coerceIn(0f, 1f) * span).roundToInt())
        .coerceIn(range.first, range.last)
    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0)
    return getMediaVolumeRatio(context)
}
fun openUrl(context: Context, url: String) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}
fun getBluetoothStatus(context: Context): String {
    val adapter = if (Build.VERSION.SDK_INT >= 23) {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        manager?.adapter
    } else {
        @Suppress("DEPRECATION")
        BluetoothAdapter.getDefaultAdapter()
    }

    if (adapter == null) {
        return "Bluetooth: Bu cihazda desteklenmiyor"
    }

    if (Build.VERSION.SDK_INT >= 31 &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) !=
        PackageManager.PERMISSION_GRANTED
    ) {
        return "Bluetooth: Izin gerekli"
    }

    return if (adapter.isEnabled) {
        "Bluetooth: Acik"
    } else {
        "Bluetooth: Kapali"
    }
}

fun openBluetoothSettings(context: Context) {
    val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
    context.startActivity(intent)
}

fun shareStation(context: Context, station: RadioStation) {
    val shareText = buildString {
        appendLine(station.name)
        appendLine(station.streamUrl)
        append(APP_DISPLAY_NAME)
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, station.name)
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, "Radyo paylas"))
}
