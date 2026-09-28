package com.samim.m3uradyo

import android.content.Context

internal const val PREFS_NAME = "m3u_radio_prefs"
private const val KEY_FAVORITES = "favorites"
private const val KEY_RECENTS = "recents"
private const val KEY_LAST_URL = "last_url"
private const val KEY_THEME = "theme"
private const val KEY_BROKEN_STREAMS = "broken_streams"
private const val KEY_AUTO_PLAY_LAST = "auto_play_last"

fun loadFavorites(context: Context): Set<String> =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getStringSet(KEY_FAVORITES, emptySet())
        ?.toSet()
        ?: emptySet()

fun toggleFavorite(context: Context, current: Set<String>, url: String): Set<String> {
    val updated = current.toMutableSet()
    if (!updated.add(url)) {
        updated.remove(url)
    }
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putStringSet(KEY_FAVORITES, updated)
        .apply()
    return updated
}

fun loadBrokenStreams(context: Context): Set<String> =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getStringSet(KEY_BROKEN_STREAMS, emptySet())
        ?.toSet()
        ?: emptySet()

fun markBrokenStream(context: Context, current: Set<String>, url: String): Set<String> {
    val updated = current.toMutableSet()
    updated.add(url)
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putStringSet(KEY_BROKEN_STREAMS, updated)
        .apply()
    return updated
}

fun clearBrokenStreams(context: Context): Set<String> {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putStringSet(KEY_BROKEN_STREAMS, emptySet())
        .apply()
    return emptySet()
}

fun loadRecents(context: Context): List<String> =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(KEY_RECENTS, "")
        .orEmpty()
        .split("\n")
        .filter { it.isNotBlank() }

fun addRecent(context: Context, url: String): List<String> {
    val updated = (listOf(url) + loadRecents(context).filter { it != url }).take(20)
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_RECENTS, updated.joinToString("\n"))
        .apply()
    return updated
}

fun saveLastStation(context: Context, url: String) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_LAST_URL, url)
        .apply()
}

fun loadLastStationUrl(context: Context): String? =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(KEY_LAST_URL, null)

fun loadAutoPlayLast(context: Context): Boolean =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(KEY_AUTO_PLAY_LAST, true)

fun saveAutoPlayLast(context: Context, enabled: Boolean) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(KEY_AUTO_PLAY_LAST, enabled)
        .apply()
}
fun loadThemeMode(context: Context): ThemeMode {
    val saved = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(KEY_THEME, ThemeMode.System.name)
    return runCatching { ThemeMode.valueOf(saved ?: ThemeMode.System.name) }
        .getOrDefault(ThemeMode.System)
}

fun saveThemeMode(context: Context, mode: ThemeMode) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_THEME, mode.name)
        .apply()
}
