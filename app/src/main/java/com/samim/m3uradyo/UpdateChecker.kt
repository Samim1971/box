package com.samim.m3uradyo

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

internal const val APP_DISPLAY_NAME = "Kanalixor Radio"
internal const val DOWNLOAD_URL = "https://github.com/Samim1971/box/releases"
private const val UPDATE_RELEASES_API_URL =
    "https://api.github.com/repos/Samim1971/box/releases?per_page=50"
private const val RADIO_APK_PREFIX = "Kanalixor-Radio-"
private const val RADIO_APK_SUFFIX = ".apk"
private const val TRUSTED_DOWNLOAD_PREFIX =
    "https://github.com/Samim1971/box/releases/download/"

data class UpdateInfo(
    val latestVersion: String,
    val releaseName: String,
    val releaseUrl: String,
    val downloadUrl: String,
    val isUpdateAvailable: Boolean
)

internal data class RadioReleaseCandidate(
    val version: String,
    val releaseName: String,
    val releaseUrl: String,
    val downloadUrl: String
)

suspend fun checkLatestRelease(context: Context): Result<UpdateInfo> = withContext(Dispatchers.IO) {
    runCatching {
        val connection = URL(UPDATE_RELEASES_API_URL).openConnection() as HttpURLConnection
        connection.connectTimeout = 10000
        connection.readTimeout = 15000
        connection.setRequestProperty("Accept", "application/vnd.github+json")
        connection.setRequestProperty("User-Agent", "Kanalixor-Radio/${currentAppVersion(context)}")

        val body = try {
            val statusCode = connection.responseCode
            if (statusCode !in 200..299) {
                throw IOException("GitHub surum sorgusu HTTP $statusCode ile sonuclandi")
            }
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }

        val latest = selectLatestRadioRelease(parseRadioReleaseCandidates(body))
            ?: throw IOException("Kanalixor Radio APK surumu bulunamadi")
        val currentVersion = currentAppVersion(context)
        UpdateInfo(
            latestVersion = latest.version,
            releaseName = latest.releaseName,
            releaseUrl = latest.releaseUrl,
            downloadUrl = latest.downloadUrl,
            isUpdateAvailable = compareVersions(latest.version, currentVersion) > 0
        )
    }
}

internal fun parseRadioReleaseCandidates(body: String): List<RadioReleaseCandidate> {
    val releases = JSONArray(body)
    val candidates = mutableListOf<RadioReleaseCandidate>()
    for (releaseIndex in 0 until releases.length()) {
        val release = releases.optJSONObject(releaseIndex) ?: continue
        if (release.optBoolean("draft") || release.optBoolean("prerelease")) continue

        val releaseUrl = release.optString("html_url").ifBlank { DOWNLOAD_URL }
        val assets = release.optJSONArray("assets") ?: continue
        for (assetIndex in 0 until assets.length()) {
            val asset = assets.optJSONObject(assetIndex) ?: continue
            val version = radioVersionFromApkName(asset.optString("name")) ?: continue
            val downloadUrl = asset.optString("browser_download_url")
            if (!downloadUrl.startsWith(TRUSTED_DOWNLOAD_PREFIX)) continue
            candidates += RadioReleaseCandidate(
                version = version,
                releaseName = release.optString("name").ifBlank { "$APP_DISPLAY_NAME $version" },
                releaseUrl = releaseUrl,
                downloadUrl = downloadUrl
            )
        }
    }
    return candidates
}

internal fun radioVersionFromApkName(name: String): String? {
    if (!name.startsWith(RADIO_APK_PREFIX, ignoreCase = true) ||
        !name.endsWith(RADIO_APK_SUFFIX, ignoreCase = true)
    ) {
        return null
    }
    val version = name.substring(
        RADIO_APK_PREFIX.length,
        name.length - RADIO_APK_SUFFIX.length
    )
    return version.takeIf { it.matches(Regex("\\d+(?:\\.\\d+)+")) }
}

internal fun selectLatestRadioRelease(
    candidates: List<RadioReleaseCandidate>
): RadioReleaseCandidate? = candidates.maxWithOrNull { left, right ->
    compareVersions(left.version, right.version)
}

internal fun currentAppVersion(context: Context): String {
    val packageInfo = if (Build.VERSION.SDK_INT >= 33) {
        context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.PackageInfoFlags.of(0)
        )
    } else {
        @Suppress("DEPRECATION")
        context.packageManager.getPackageInfo(context.packageName, 0)
    }
    return packageInfo.versionName?.let { normalizeVersionName(it) } ?: "0.0.0"
}

internal fun normalizeVersionName(value: String): String =
    value.trim().removePrefix("v").removePrefix("V").ifBlank { "0.0.0" }

internal fun compareVersions(left: String, right: String): Int {
    val leftParts = versionParts(left)
    val rightParts = versionParts(right)
    val maxSize = maxOf(leftParts.size, rightParts.size)
    for (index in 0 until maxSize) {
        val leftPart = leftParts.getOrElse(index) { 0 }
        val rightPart = rightParts.getOrElse(index) { 0 }
        if (leftPart != rightPart) {
            return leftPart.compareTo(rightPart)
        }
    }
    return 0
}

private fun versionParts(value: String): List<Int> =
    normalizeVersionName(value)
        .split(Regex("[^0-9]+"))
        .filter { it.isNotBlank() }
        .map { it.toIntOrNull() ?: 0 }
        .ifEmpty { listOf(0) }
