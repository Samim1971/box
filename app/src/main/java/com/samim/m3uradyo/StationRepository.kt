package com.samim.m3uradyo

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

internal const val RADIO_LIST_URL =
    "https://raw.githubusercontent.com/Samim1971/box/refs/heads/main/RADYOLAR.m3u"
internal const val CATEGORY_ALL = "Tum"

suspend fun loadStations(
    context: Context,
    address: String
): Result<StationLoadResult> = withContext(Dispatchers.IO) {
    runCatching {
        val remoteResult = runCatching {
            val normalizedAddress = address.trim()
            require(normalizedAddress.startsWith("http://") || normalizedAddress.startsWith("https://")) {
                "Adres http veya https ile baslamali"
            }

            val connection = URL(normalizedAddress).openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 20000
            connection.setRequestProperty("User-Agent", "M3U-Radyo-Android")

            try {
                connection.inputStream.bufferedReader().use { reader ->
                    parseM3u(reader.readText())
                }
            } finally {
                connection.disconnect()
            }
        }

        val remoteStations = remoteResult.getOrNull().orEmpty()
        if (remoteStations.isNotEmpty()) {
            return@runCatching StationLoadResult(remoteStations, "GitHub")
        }

        val bundledStations = context.assets.open("RADYOLAR.m3u").bufferedReader().use { reader ->
            parseM3u(reader.readText())
        }
        require(bundledStations.isNotEmpty()) {
            "GitHub ve APK icindeki liste okunamadi"
        }
        StationLoadResult(bundledStations, "APK icindeki liste")
    }
}

fun parseM3u(content: String): List<RadioStation> {
    val stations = mutableListOf<RadioStation>()
    var pendingName: String? = null
    var pendingLogoUrl: String? = null
    var pendingCategory: String? = null

    content.lineSequence()
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .forEach { line ->
            when {
                line.startsWith("#EXTINF", ignoreCase = true) -> {
                    pendingName = line.substringAfter(",", missingDelimiterValue = "")
                        .trim()
                        .ifBlank { null }
                    pendingLogoUrl = extractM3uAttribute(line, "tvg-logo")
                        ?.trim()
                        ?.ifBlank { null }
                    pendingCategory = extractM3uAttribute(line, "group-title")
                        ?.trim()
                        ?.ifBlank { null }
                        ?.let { normalizeCategory(it) }
                }
                line.startsWith("#") -> Unit
                line.startsWith("http://", ignoreCase = true) ||
                    line.startsWith("https://", ignoreCase = true) -> {
                    val name = pendingName ?: line.substringAfterLast("/")
                        .substringBefore("?")
                        .ifBlank { "Radyo" }
                    stations += RadioStation(
                        name = name,
                        streamUrl = line,
                        logoUrl = pendingLogoUrl,
                        category = pendingCategory ?: inferCategory(name)
                    )
                    pendingName = null
                    pendingLogoUrl = null
                    pendingCategory = null
                }
            }
        }

    return stations.distinctBy { it.streamUrl }
}

fun extractM3uAttribute(line: String, key: String): String? {
    val pattern = Regex("\\b${Regex.escape(key)}=\\\"([^\\\"]*)\\\"", RegexOption.IGNORE_CASE)
    return pattern.find(line)?.groupValues?.getOrNull(1)
}

fun buildCategoryOptions(stations: List<RadioStation>): List<String> {
    val preferred = listOf(CATEGORY_ALL, "Arabesk", "Pop", "Haber", "Turku", "Spor", "Dini", "Yerel", "Genel")
    val fromList = stations
        .map { it.category }
        .filter { it.isNotBlank() && it != CATEGORY_ALL }
        .distinct()
    return (preferred + fromList).distinct()
}

fun normalizeCategory(value: String): String {
    val normalized = simplifyText(value)
    return when {
        normalized.contains("arabesk") -> "Arabesk"
        normalized.contains("pop") -> "Pop"
        normalized.contains("haber") || normalized.contains("news") -> "Haber"
        normalized.contains("turku") || normalized.contains("halk") -> "Turku"
        normalized.contains("spor") || normalized.contains("sport") -> "Spor"
        normalized.contains("dini") || normalized.contains("ilah") || normalized.contains("kuran") -> "Dini"
        normalized.contains("yerel") || normalized.contains("local") -> "Yerel"
        else -> value.trim().ifBlank { "Genel" }.take(18)
    }
}

fun inferCategory(name: String): String {
    val normalized = simplifyText(name)
    return when {
        containsAny(normalized, "arabesk", "damar", "baba", "kral") -> "Arabesk"
        containsAny(normalized, "pop", "power", "metro", "virgin", "joy", "slow", "super", "fenomen") -> "Pop"
        containsAny(normalized, "haber", "sputnik", "ntv", "cnn", "ulke", "a haber", "trt haber") -> "Haber"
        containsAny(normalized, "turku", "seymen", "halk") -> "Turku"
        containsAny(normalized, "spor", "sport", "lig", "gol") -> "Spor"
        containsAny(normalized, "diyanet", "kuran", "ilah", "dini") -> "Dini"
        containsAny(normalized, "radyo", "fm") -> "Genel"
        else -> "Genel"
    }
}

fun containsAny(value: String, vararg needles: String): Boolean =
    needles.any { value.contains(it) }

fun simplifyText(value: String): String =
    value.lowercase(Locale.ROOT)
        .replace('\u00e7', 'c')
        .replace('\u011f', 'g')
        .replace('\u0131', 'i')
        .replace('\u00f6', 'o')
        .replace('\u015f', 's')
        .replace('\u00fc', 'u')
        .replace('\u00c7', 'c')
        .replace('\u011e', 'g')
        .replace('\u0130', 'i')
        .replace('\u00d6', 'o')
        .replace('\u015e', 's')
        .replace('\u00dc', 'u')
