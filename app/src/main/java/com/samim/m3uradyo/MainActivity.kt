package com.samim.m3uradyo

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.ui.PlayerNotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.math.roundToInt

private const val RADIO_LIST_URL =
    "https://raw.githubusercontent.com/Samim1971/box/refs/heads/main/RADYOLAR.m3u"
private const val PREFS_NAME = "m3u_radio_prefs"
private const val KEY_FAVORITES = "favorites"
private const val KEY_RECENTS = "recents"
private const val KEY_LAST_URL = "last_url"
private const val KEY_THEME = "theme"
private const val KEY_BROKEN_STREAMS = "broken_streams"
private const val KEY_AUTO_PLAY_LAST = "auto_play_last"
private const val KEY_LAST_NOTIFIED_UPDATE = "last_notified_update"
private const val NOTIFICATION_CHANNEL_ID = "radio_playback"
private const val UPDATE_NOTIFICATION_CHANNEL_ID = "app_updates"
private const val NOTIFICATION_ID = 41
private const val UPDATE_NOTIFICATION_ID = 42
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
private const val CATEGORY_ALL = "Tum"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RadioApp()
        }
    }
}

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

data class RadioStation(
    val name: String,
    val streamUrl: String,
    val logoUrl: String? = null,
    val category: String = "Genel"
)

data class StationLoadResult(
    val stations: List<RadioStation>,
    val source: String
)

enum class StationFilter {
    All,
    Favorites,
    Recents
}

enum class ThemeMode {
    System,
    Light,
    Dark
}

enum class PlaybackUiState(val label: String) {
    Loading("Liste yukleniyor"),
    Ready("Hazir"),
    Opening("Aciliyor"),
    Playing("Caliyor"),
    Paused("Duraklatildi"),
    Stopped("Durduruldu"),
    Retrying("Yeniden deneniyor"),
    Error("Baglanti hatasi")
}

@Composable
fun RadioApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            setWakeMode(C.WAKE_MODE_NETWORK)
        }
    }
    val mediaSession = remember {
        MediaSession.Builder(context, player).build()
    }

    var stations by remember { mutableStateOf(emptyList<RadioStation>()) }
    var selectedStation by remember { mutableStateOf<RadioStation?>(null) }
    var status by remember { mutableStateOf("Radyo listesi yukleniyor...") }
    var playbackState by remember { mutableStateOf(PlaybackUiState.Loading) }
    var isLoading by remember { mutableStateOf(false) }
    var isSwitchingStation by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var retryCount by remember { mutableStateOf(0) }
    var searchText by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(StationFilter.All) }
    var categoryFilter by remember { mutableStateOf(CATEGORY_ALL) }
    var isMenuOpen by remember { mutableStateOf(false) }
    var isAboutOpen by remember { mutableStateOf(false) }
    var themeMode by remember { mutableStateOf(loadThemeMode(context)) }
    var bluetoothStatus by remember { mutableStateOf(getBluetoothStatus(context)) }
    var favorites by remember { mutableStateOf(loadFavorites(context)) }
    var recents by remember { mutableStateOf(loadRecents(context)) }
    var brokenStreams by remember { mutableStateOf(loadBrokenStreams(context)) }
    var autoPlayLastEnabled by remember { mutableStateOf(loadAutoPlayLast(context)) }
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var updateStatus by remember { mutableStateOf("Guncelleme kontrol edilmedi") }
    var isCheckingUpdates by remember { mutableStateOf(false) }
    var volume by remember { mutableStateOf(getMediaVolumeRatio(context)) }
    var previousAudibleVolume by remember {
        mutableStateOf(if (volume > 0f) volume else 0.5f)
    }
    var isMuted by remember { mutableStateOf(volume <= 0f) }
    val systemDark = isSystemInDarkTheme()
    val useDarkTheme = when (themeMode) {
        ThemeMode.System -> systemDark
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}
    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        bluetoothStatus = getBluetoothStatus(context)
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT >= 31 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            bluetoothStatus = getBluetoothStatus(context)
        }
    }

    fun savePlaybackState(station: RadioStation) {
        saveLastStation(context, station.streamUrl)
        recents = addRecent(context, station.streamUrl)
    }

    fun stopPlayback() {
        player.stop()
        player.clearMediaItems()
        isSwitchingStation = false
        isPlaying = false
        playbackState = PlaybackUiState.Stopped
        status = "Durduruldu."
        stopPlaybackService(context)
    }

    fun startStation(station: RadioStation) {
        selectedStation = station
        isSwitchingStation = true
        retryCount = 0
        playbackState = PlaybackUiState.Opening
        status = "Aciliyor: ${station.name}"
        savePlaybackState(station)
        val started = playStation(player, station)
        isPlaying = started
        if (!started) {
            isSwitchingStation = false
            playbackState = PlaybackUiState.Error
            status = "Radyo acilamadi: ${station.name}"
        } else {
            status = "Baglaniyor: ${station.name}"
            updatePlaybackService(context, station, true)
        }
    }

    fun playRelativeStation(offset: Int) {
        val playableStations = stations
            .filter { !brokenStreams.contains(it.streamUrl) }
            .filter { categoryFilter == CATEGORY_ALL || it.category == categoryFilter }
            .ifEmpty { stations.filter { !brokenStreams.contains(it.streamUrl) } }
            .ifEmpty { stations }
        if (playableStations.isEmpty()) return
        val currentIndex = selectedStation?.let { current ->
            playableStations.indexOfFirst { it.streamUrl == current.streamUrl }
        } ?: -1
        val nextIndex = if (currentIndex >= 0) {
            Math.floorMod(currentIndex + offset, playableStations.size)
        } else if (offset >= 0) {
            0
        } else {
            playableStations.lastIndex
        }
        startStation(playableStations[nextIndex])
    }

    fun togglePlayback() {
        val station = selectedStation
            ?: stations.firstOrNull { !brokenStreams.contains(it.streamUrl) }
            ?: stations.firstOrNull()
            ?: return
        if (isPlaying) {
            player.pause()
            isPlaying = false
            playbackState = PlaybackUiState.Paused
            status = "Duraklatildi: ${station.name}"
            updatePlaybackService(context, station, false)
        } else {
            startStation(station)
        }
    }

    fun refreshStations(autoPlayLast: Boolean) {
        scope.launch {
            isLoading = true
            playbackState = PlaybackUiState.Loading
            status = "Radyo listesi indiriliyor..."
            val result = loadStations(context, RADIO_LIST_URL)
            stations = result.getOrNull()?.stations ?: emptyList()
            status = result.fold(
                onSuccess = { "Liste hazir. Kaynak: ${it.source}" },
                onFailure = { "Liste okunamadi: ${it.message ?: "hata"}" }
            )
            playbackState = if (result.isSuccess) PlaybackUiState.Ready else PlaybackUiState.Error
            isLoading = false

            if (autoPlayLast && stations.isNotEmpty()) {
                val lastUrl = loadLastStationUrl(context)
                val lastStation = stations.firstOrNull {
                    it.streamUrl == lastUrl && !brokenStreams.contains(it.streamUrl)
                }
                if (lastStation != null) {
                    startStation(lastStation)
                }
            }
        }
    }

    fun checkForUpdates(showUpToDate: Boolean) {
        scope.launch {
            isCheckingUpdates = true
            updateStatus = "Guncelleme kontrol ediliyor..."
            val result = checkLatestRelease(context)
            result.onSuccess { info ->
                updateInfo = info
                updateStatus = if (info.isUpdateAvailable) {
                    "Yeni surum var: ${info.latestVersion}"
                } else {
                    "Guncel surum kullaniliyor"
                }
                if (info.isUpdateAvailable) {
                    maybeShowUpdateNotification(context, info)
                } else if (showUpToDate) {
                    status = "Uygulama guncel."
                }
            }.onFailure { error ->
                updateStatus = "Guncelleme kontrol edilemedi: ${error.message ?: "hata"}"
                if (showUpToDate) {
                    status = updateStatus
                }
            }
            isCheckingUpdates = false
        }
    }

    LaunchedEffect(Unit) {
        refreshStations(autoPlayLast = autoPlayLastEnabled)
        checkForUpdates(showUpToDate = false)
    }

    DisposableEffect(player, selectedStation, retryCount) {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                val station = selectedStation
                if (station != null && retryCount < 3) {
                    val nextRetry = retryCount + 1
                    retryCount = nextRetry
                    isSwitchingStation = true
                    isPlaying = false
                    playbackState = PlaybackUiState.Retrying
                    status = "Baglanti koptu, yeniden deneniyor ($nextRetry/3): ${station.name}"
                    scope.launch {
                        delay(3000)
                        playbackState = PlaybackUiState.Opening
                        val restarted = playStation(player, station)
                        isPlaying = restarted
                        if (!restarted) {
                            isSwitchingStation = false
                            playbackState = PlaybackUiState.Error
                        }
                    }
                } else {
                    isSwitchingStation = false
                    isPlaying = false
                    playbackState = PlaybackUiState.Error
                    status = "Radyo acilamadi: ${station?.name ?: "secili radyo"}"
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_BUFFERING -> {
                        playbackState = PlaybackUiState.Opening
                        selectedStation?.let { status = "Aciliyor: ${it.name}" }
                    }
                    Player.STATE_READY -> {
                        isSwitchingStation = false
                        retryCount = 0
                        playbackState = if (player.isPlaying) {
                            PlaybackUiState.Playing
                        } else {
                            PlaybackUiState.Paused
                        }
                        selectedStation?.let {
                            status = if (player.isPlaying) {
                                "Caliniyor: ${it.name}"
                            } else {
                                "Duraklatildi: ${it.name}"
                            }
                            updatePlaybackService(context, it, player.isPlaying)
                        }
                    }
                    Player.STATE_ENDED -> {
                        isPlaying = false
                        playbackState = PlaybackUiState.Stopped
                    }
                }
            }

            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                isPlaying = isPlayingNow
                selectedStation?.let {
                    if (isPlayingNow) {
                        playbackState = PlaybackUiState.Playing
                        status = "Caliniyor: ${it.name}"
                    } else if (!isSwitchingStation &&
                        playbackState != PlaybackUiState.Error &&
                        playbackState != PlaybackUiState.Stopped
                    ) {
                        playbackState = PlaybackUiState.Paused
                        status = "Duraklatildi: ${it.name}"
                    }
                    if (isPlayingNow || playbackState != PlaybackUiState.Stopped) {
                        updatePlaybackService(context, it, isPlayingNow)
                    }
                }
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            player.release()
            mediaSession.release()
            stopPlaybackService(context)
        }
    }

    DisposableEffect(stations, selectedStation, isPlaying, brokenStreams, categoryFilter) {
        RadioControlBridge.onPrevious = { playRelativeStation(-1) }
        RadioControlBridge.onToggle = { togglePlayback() }
        RadioControlBridge.onStop = { stopPlayback() }
        RadioControlBridge.onNext = { playRelativeStation(1) }
        onDispose {
            RadioControlBridge.onPrevious = null
            RadioControlBridge.onToggle = null
            RadioControlBridge.onStop = null
            RadioControlBridge.onNext = null
        }
    }

    val categoryOptions = remember(stations) {
        buildCategoryOptions(stations)
    }

    val visibleStations = remember(
        stations,
        favorites,
        recents,
        filter,
        categoryFilter,
        searchText,
        brokenStreams
    ) {
        val availableStations = stations.filter { !brokenStreams.contains(it.streamUrl) }
        val base = when (filter) {
            StationFilter.All -> availableStations.sortedByDescending { favorites.contains(it.streamUrl) }
            StationFilter.Favorites -> availableStations.filter { favorites.contains(it.streamUrl) }
            StationFilter.Recents -> recents.mapNotNull { url ->
                availableStations.firstOrNull { it.streamUrl == url }
            }
        }.filter { categoryFilter == CATEGORY_ALL || it.category == categoryFilter }
        val searched = base.let {
            val query = searchText.trim()
            if (query.isBlank()) {
                it
            } else {
                it.filter { station -> station.name.contains(query, ignoreCase = true) }
            }
        }
        searched
    }

    MaterialTheme(
        colorScheme = if (useDarkTheme) {
            darkColorScheme()
        } else {
            lightColorScheme(
                primary = Color(0xFF6F4BB3),
                primaryContainer = Color(0xFFE8DDF1),
                secondaryContainer = Color(0xFFE9DDF0),
                surface = Color(0xFFFFF8FF),
                surfaceVariant = Color(0xFFECE1EC)
            )
        }
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val nextMenuState = !isMenuOpen
                            isMenuOpen = nextMenuState
                            if (!nextMenuState) {
                                isAboutOpen = false
                            }
                        },
                        shape = RoundedCornerShape(26.dp),
                        modifier = Modifier.size(width = 58.dp, height = 42.dp),
                        contentPadding = ButtonDefaults.ContentPadding
                    ) {
                        MenuLines()
                    }
                    Text(
                        text = "Radyolar",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .widthIn(min = 104.dp)
                    )
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        label = { Text("Radyo ara", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp)
                    )
                    Button(
                        onClick = { searchText = "" },
                        enabled = searchText.isNotBlank(),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE7E0E8),
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .size(44.dp)
                    ) {
                        Text("X", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
                StatusBadge(
                    playbackState = playbackState,
                    status = status,
                    modifier = Modifier.padding(top = 14.dp)
                )
                updateInfo?.takeIf { it.isUpdateAvailable }?.let { info ->
                    UpdateBanner(
                        updateInfo = info,
                        onOpen = { openUrl(context, info.downloadUrl) },
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                if (isMenuOpen) {
                    MenuScreen(
                        modifier = Modifier.weight(1f),
                        filter = filter,
                        categoryFilter = categoryFilter,
                        categoryOptions = categoryOptions,
                        favoritesCount = favorites.size,
                        recentsCount = recents.size,
                        brokenCount = brokenStreams.size,
                        bluetoothStatus = bluetoothStatus,
                        autoPlayLastEnabled = autoPlayLastEnabled,
                        updateStatus = updateStatus,
                        isCheckingUpdates = isCheckingUpdates,
                        isAboutOpen = isAboutOpen,
                        onShowAll = {
                            filter = StationFilter.All
                            isMenuOpen = false
                            isAboutOpen = false
                        },
                        onShowFavorites = {
                            filter = StationFilter.Favorites
                            isMenuOpen = false
                            isAboutOpen = false
                        },
                        onShowRecents = {
                            filter = StationFilter.Recents
                            isMenuOpen = false
                            isAboutOpen = false
                        },
                        onRefresh = {
                            isMenuOpen = false
                            isAboutOpen = false
                            refreshStations(autoPlayLast = false)
                        },
                        onAbout = {
                            isAboutOpen = !isAboutOpen
                        },
                        onCategoryChange = { selectedCategory ->
                            categoryFilter = selectedCategory
                        },
                        onToggleAutoPlayLast = {
                            val updated = !autoPlayLastEnabled
                            autoPlayLastEnabled = updated
                            saveAutoPlayLast(context, updated)
                        },
                        onClearBroken = {
                            brokenStreams = clearBrokenStreams(context)
                        },
                        onCheckUpdates = {
                            checkForUpdates(showUpToDate = true)
                        },
                        onOpenReleases = {
                            val updateUrl = updateInfo
                                ?.takeIf { it.isUpdateAvailable }
                                ?.downloadUrl
                            openUrl(context, updateUrl ?: DOWNLOAD_URL)
                        },
                        themeMode = themeMode,
                        onThemeModeChange = { selectedMode ->
                            themeMode = selectedMode
                            saveThemeMode(context, selectedMode)
                        },
                        onOpenBluetooth = {
                            openBluetoothSettings(context)
                            bluetoothStatus = getBluetoothStatus(context)
                        },
                        onRefreshBluetooth = {
                            bluetoothStatus = getBluetoothStatus(context)
                        }
                    )
                } else {
                    if (isLoading || isSwitchingStation) {
                        CircularProgressIndicator(modifier = Modifier.padding(top = 10.dp))
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    CategoryBar(
                        categories = categoryOptions,
                        selectedCategory = categoryFilter,
                        onCategoryChange = { categoryFilter = it }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StationList(
                        stations = visibleStations,
                        selectedStation = selectedStation,
                        favorites = favorites,
                        enabled = !isSwitchingStation,
                        modifier = Modifier.weight(1f),
                        onSelect = { station -> startStation(station) },
                        onToggleFavorite = { station ->
                            favorites = toggleFavorite(context, favorites, station.streamUrl)
                        },
                        onMarkBroken = { station ->
                            brokenStreams = markBrokenStream(context, brokenStreams, station.streamUrl)
                            if (selectedStation?.streamUrl == station.streamUrl) {
                                stopPlayback()
                            }
                        },
                        onShare = { station ->
                            shareStation(context, station)
                        }
                    )
                    VolumeControl(
                        volume = volume,
                        isMuted = isMuted,
                        onVolumeChange = { requestedVolume ->
                            val appliedVolume = setMediaVolumeRatio(context, requestedVolume)
                            volume = appliedVolume
                            isMuted = appliedVolume <= 0f
                            if (appliedVolume > 0f) {
                                previousAudibleVolume = appliedVolume
                            }
                        },
                        onMuteToggle = {
                            val targetVolume = if (isMuted) {
                                previousAudibleVolume.coerceAtLeast(0.05f)
                            } else {
                                if (volume > 0f) {
                                    previousAudibleVolume = volume
                                }
                                0f
                            }
                            val appliedVolume = setMediaVolumeRatio(context, targetVolume)
                            volume = appliedVolume
                            isMuted = appliedVolume <= 0f
                        }
                    )
                    BottomControls(
                        isPlaying = isPlaying,
                        onPrevious = { playRelativeStation(-1) },
                        onPlay = {
                            if (!isPlaying) togglePlayback()
                        },
                        onPause = {
                            if (isPlaying) togglePlayback()
                        },
                        onStop = { stopPlayback() },
                        onNext = { playRelativeStation(1) }
                    )
                }
            }
        }
    }
}

@Composable
fun MenuScreen(
    modifier: Modifier = Modifier,
    filter: StationFilter,
    categoryFilter: String,
    categoryOptions: List<String>,
    favoritesCount: Int,
    recentsCount: Int,
    brokenCount: Int,
    bluetoothStatus: String,
    autoPlayLastEnabled: Boolean,
    updateStatus: String,
    isCheckingUpdates: Boolean,
    isAboutOpen: Boolean,
    onShowAll: () -> Unit,
    onShowFavorites: () -> Unit,
    onShowRecents: () -> Unit,
    onRefresh: () -> Unit,
    onAbout: () -> Unit,
    onCategoryChange: (String) -> Unit,
    onToggleAutoPlayLast: () -> Unit,
    onClearBroken: () -> Unit,
    onCheckUpdates: () -> Unit,
    onOpenReleases: () -> Unit,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onOpenBluetooth: () -> Unit,
    onRefreshBluetooth: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
            .padding(top = 12.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Menu",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Kaydirilabilir",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CompactMenuButton(
                    text = if (filter == StationFilter.All) "[Tum]" else "Tum",
                    modifier = Modifier.weight(1f),
                    onClick = onShowAll
                )
                CompactMenuButton(
                    text = "Favori $favoritesCount",
                    modifier = Modifier.weight(1f),
                    onClick = onShowFavorites
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CompactMenuButton(
                    text = "Son $recentsCount",
                    modifier = Modifier.weight(1f),
                    onClick = onShowRecents
                )
                CompactMenuButton(
                    text = "Yenile",
                    modifier = Modifier.weight(1f),
                    onClick = onRefresh
                )
            }
            Text(
                text = "Kategori",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
            categoryOptions.chunked(3).forEach { rowCategories ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowCategories.forEach { category ->
                        CompactMenuButton(
                            text = if (category == categoryFilter) "[$category]" else category,
                            modifier = Modifier.weight(1f),
                            onClick = { onCategoryChange(category) }
                        )
                    }
                }
            }
            Text(
                text = "Oynatma",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CompactMenuButton(
                    text = if (autoPlayLastEnabled) "Son radyo: Acik" else "Son radyo: Kapali",
                    modifier = Modifier.weight(1f),
                    onClick = onToggleAutoPlayLast
                )
                CompactMenuButton(
                    text = "Bozuklari temizle $brokenCount",
                    modifier = Modifier.weight(1f),
                    onClick = onClearBroken
                )
            }
            Text(
                text = "Guncelleme",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                updateStatus,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CompactMenuButton(
                    text = if (isCheckingUpdates) "Kontrol..." else "Kontrol et",
                    modifier = Modifier.weight(1f),
                    onClick = onCheckUpdates
                )
                CompactMenuButton(
                    text = "Indirme sayfasi",
                    modifier = Modifier.weight(1f),
                    onClick = onOpenReleases
                )
            }
            CompactMenuButton(
                text = if (isAboutOpen) "Hakkinda kapat" else "Hakkinda",
                modifier = Modifier.fillMaxWidth(),
                onClick = onAbout
            )
            if (isAboutOpen) {
                AboutPanel()
            }
            Text(
                text = "Tema",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CompactMenuButton(
                    text = if (themeMode == ThemeMode.System) "[Sistem]" else "Sistem",
                    modifier = Modifier.weight(1f),
                    onClick = { onThemeModeChange(ThemeMode.System) }
                )
                CompactMenuButton(
                    text = if (themeMode == ThemeMode.Light) "[Light]" else "Light",
                    modifier = Modifier.weight(1f),
                    onClick = { onThemeModeChange(ThemeMode.Light) }
                )
                CompactMenuButton(
                    text = if (themeMode == ThemeMode.Dark) "[Dark]" else "Dark",
                    modifier = Modifier.weight(1f),
                    onClick = { onThemeModeChange(ThemeMode.Dark) }
                )
            }
            Text(
                text = "Bluetooth",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(bluetoothStatus, color = MaterialTheme.colorScheme.onSurface)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CompactMenuButton(
                    text = "Ayarlar",
                    modifier = Modifier.weight(1f),
                    onClick = onOpenBluetooth
                )
                CompactMenuButton(
                    text = "Durum",
                    modifier = Modifier.weight(1f),
                    onClick = onRefreshBluetooth
                )
            }
        }
    }
}

@Composable
fun CompactMenuButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(46.dp),
        shape = RoundedCornerShape(16.dp),
        contentPadding = ButtonDefaults.ContentPadding
    ) {
        Text(
            text,
            color = MaterialTheme.colorScheme.onPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun MenuLines() {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.onPrimary)
            )
        }
    }
}

@Composable
fun AboutPanel() {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Hakkinda",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(APP_DISPLAY_NAME, color = MaterialTheme.colorScheme.onSecondaryContainer)
            Text("Samim Ozcosar tarafindan hazirlanmistir.", color = MaterialTheme.colorScheme.onSecondaryContainer)
            Text("Surum: ${currentAppVersion(context)}", color = MaterialTheme.colorScheme.onSecondaryContainer)
            Text("Liste: RADYOLAR.m3u", color = MaterialTheme.colorScheme.onSecondaryContainer)
            Text("Liste adresi: $RADIO_LIST_URL", color = MaterialTheme.colorScheme.onSecondaryContainer)
            Button(
                onClick = {
                    openUrl(context, DOWNLOAD_URL)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("GitHub indirme sayfasi")
            }
        }
    }
}

@Composable
fun StatusBadge(
    playbackState: PlaybackUiState,
    status: String,
    modifier: Modifier = Modifier
) {
    val color = when (playbackState) {
        PlaybackUiState.Playing -> Color(0xFF2D6A4F)
        PlaybackUiState.Opening, PlaybackUiState.Loading -> Color(0xFF6F4BB3)
        PlaybackUiState.Retrying -> Color(0xFFB26A00)
        PlaybackUiState.Error -> Color(0xFFB00020)
        PlaybackUiState.Paused, PlaybackUiState.Stopped, PlaybackUiState.Ready -> Color(0xFF4A4458)
    }
    val statusTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, CircleShape)
            )
            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = statusTextColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun UpdateBanner(
    updateInfo: UpdateInfo,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF0D6)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Yeni surum var: ${updateInfo.latestVersion}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2B2118),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = updateInfo.releaseName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4A3A2A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Button(
                onClick = onOpen,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Text("Indir")
            }
        }
    }
}

@Composable
fun CategoryBar(
    categories: List<String>,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(categories, key = { it }) { category ->
            Button(
                onClick = { onCategoryChange(category) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (category == selectedCategory) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    contentColor = if (category == selectedCategory) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                ),
                modifier = Modifier.height(38.dp)
            ) {
                Text(
                    category,
                    color = if (category == selectedCategory) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun MiniPlayer(
    station: RadioStation,
    playbackState: PlaybackUiState,
    isPlaying: Boolean,
    onToggle: () -> Unit,
    onStop: () -> Unit,
    onShare: () -> Unit,
    onMarkBroken: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StationLogo(station.name, station.logoUrl, Modifier.size(36.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp)
                ) {
                    Text(
                        text = station.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = playbackState.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1
                    )
                }
                SmallActionButton(
                    text = if (isPlaying) "Duraklat" else "Oynat",
                    onClick = onToggle
                )
                Spacer(modifier = Modifier.width(6.dp))
                SmallActionButton(text = "Durdur", onClick = onStop)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallActionButton(
                    text = "Paylas",
                    modifier = Modifier.weight(1f),
                    onClick = onShare
                )
                SmallActionButton(
                    text = "Bozuk bildir",
                    modifier = Modifier.weight(1f),
                    onClick = onMarkBroken
                )
            }
        }
    }
}

@Composable
fun VolumeControl(
    volume: Float,
    isMuted: Boolean,
    onVolumeChange: (Float) -> Unit,
    onMuteToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Telefon sesi ${(volume * 100).roundToInt()}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(112.dp)
        )
        Slider(
            value = volume,
            onValueChange = { onVolumeChange(it.coerceIn(0f, 1f)) },
            modifier = Modifier.weight(1f)
        )
        Button(
            onClick = onMuteToggle,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .padding(start = 8.dp)
                .height(40.dp)
        ) {
            Text(
                if (isMuted) "Ses ac" else "Sessiz",
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun SmallActionButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.height(36.dp),
        contentPadding = ButtonDefaults.ContentPadding
    ) {
        Text(
            text,
            color = MaterialTheme.colorScheme.onPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

fun activeFilterLabel(filter: StationFilter, count: Int): String =
    when (filter) {
        StationFilter.All -> "Tum radyolar: $count"
        StationFilter.Favorites -> "Favoriler: $count"
        StationFilter.Recents -> "Son dinlenenler: $count"
    }

@Composable
fun PlayerPanel(
    station: RadioStation,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onToggle: () -> Unit,
    onStop: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StationLogo(station.name)
                Text(
                    text = station.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp)
                )
                Button(onClick = onToggleFavorite) {
                    Text(
                        if (isFavorite) "Favoriden cikar" else "Favori",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Row(
                modifier = Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(onClick = onToggle) {
                    Text(
                        if (isPlaying) "Duraklat" else "Oynat",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Button(onClick = onStop) {
                    Text("Durdur", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StationList(
    stations: List<RadioStation>,
    selectedStation: RadioStation?,
    favorites: Set<String>,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onSelect: (RadioStation) -> Unit,
    onToggleFavorite: (RadioStation) -> Unit,
    onMarkBroken: (RadioStation) -> Unit,
    onShare: (RadioStation) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(stations, key = { it.streamUrl }) { station ->
            val isSelected = station == selectedStation
            val isFavorite = favorites.contains(station.streamUrl)
            var stationMenuOpen by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(86.dp)
                        .combinedClickable(
                            enabled = enabled,
                            onClick = { onSelect(station) },
                            onLongClick = { stationMenuOpen = true }
                        ),
                    shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                Color(0xFFE9D8F7)
                        } else {
                            Color(0xFFECE1EC)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StationLogo(station.name, station.logoUrl)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 10.dp)
                        ) {
                            Text(
                                text = station.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111111),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = station.category,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF2F2634),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Button(
                            onClick = { onToggleFavorite(station) },
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.size(width = 58.dp, height = 46.dp),
                            contentPadding = ButtonDefaults.ContentPadding
                        ) {
                            Text(
                                if (isFavorite) "*" else "+",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
                DropdownMenu(
                    expanded = stationMenuOpen,
                    onDismissRequest = { stationMenuOpen = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Paylas", color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            stationMenuOpen = false
                            onShare(station)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Bozuk bildir", color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            stationMenuOpen = false
                            onMarkBroken(station)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BottomControls(
    isPlaying: Boolean,
    onPrevious: () -> Unit,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onNext: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundControlButton(
                    icon = ControlIcon.Previous,
                    onClick = onPrevious
                )
                RoundControlButton(
                    icon = if (isPlaying) ControlIcon.Pause else ControlIcon.Play,
                    onClick = { if (isPlaying) onPause() else onPlay() },
                    isPrimary = true
                )
                RoundControlButton(
                    icon = ControlIcon.Stop,
                    onClick = onStop,
                    isStop = true
                )
                RoundControlButton(
                    icon = ControlIcon.Next,
                    onClick = onNext
                )
            }
        }
    }
}

enum class ControlIcon {
    Previous,
    Play,
    Pause,
    Stop,
    Next
}

@Composable
fun RoundControlButton(
    icon: ControlIcon,
    onClick: () -> Unit,
    isPrimary: Boolean = false,
    isStop: Boolean = false
) {
    val containerColor = when {
        isStop -> MaterialTheme.colorScheme.error
        isPrimary -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surface
    }
    val contentColor = when {
        isStop -> MaterialTheme.colorScheme.onError
        isPrimary -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.primary
    }
    val buttonSize = if (isPrimary) 62.dp else 50.dp
    Button(
        onClick = onClick,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        modifier = Modifier
            .size(buttonSize)
            .border(
                width = if (isPrimary) 2.dp else 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                shape = CircleShape
            ),
        contentPadding = PaddingValues(0.dp)
    ) {
        ControlGlyph(icon = icon, color = contentColor)
    }
}

@Composable
fun ControlGlyph(
    icon: ControlIcon,
    color: Color,
    modifier: Modifier = Modifier.size(24.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        when (icon) {
            ControlIcon.Play -> {
                val path = Path().apply {
                    moveTo(w * 0.32f, h * 0.22f)
                    lineTo(w * 0.32f, h * 0.78f)
                    lineTo(w * 0.78f, h * 0.50f)
                    close()
                }
                drawPath(path, color)
            }
            ControlIcon.Pause -> {
                val barWidth = w * 0.18f
                val barHeight = h * 0.58f
                val top = h * 0.21f
                drawRoundRect(
                    color = color,
                    topLeft = Offset(w * 0.29f, top),
                    size = Size(barWidth, barHeight)
                )
                drawRoundRect(
                    color = color,
                    topLeft = Offset(w * 0.53f, top),
                    size = Size(barWidth, barHeight)
                )
            }
            ControlIcon.Stop -> {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(w * 0.28f, h * 0.28f),
                    size = Size(w * 0.44f, h * 0.44f)
                )
            }
            ControlIcon.Previous -> {
                drawLine(
                    color = color,
                    start = Offset(w * 0.24f, h * 0.24f),
                    end = Offset(w * 0.24f, h * 0.76f),
                    strokeWidth = w * 0.10f,
                    cap = StrokeCap.Round
                )
                val first = Path().apply {
                    moveTo(w * 0.74f, h * 0.22f)
                    lineTo(w * 0.38f, h * 0.50f)
                    lineTo(w * 0.74f, h * 0.78f)
                    close()
                }
                drawPath(first, color)
            }
            ControlIcon.Next -> {
                drawLine(
                    color = color,
                    start = Offset(w * 0.76f, h * 0.24f),
                    end = Offset(w * 0.76f, h * 0.76f),
                    strokeWidth = w * 0.10f,
                    cap = StrokeCap.Round
                )
                val first = Path().apply {
                    moveTo(w * 0.26f, h * 0.22f)
                    lineTo(w * 0.62f, h * 0.50f)
                    lineTo(w * 0.26f, h * 0.78f)
                    close()
                }
                drawPath(first, color)
            }
        }
    }
}

@Composable
fun StationLogo(
    name: String,
    logoUrl: String? = null,
    modifier: Modifier = Modifier.size(42.dp)
) {
    val logoBitmap = rememberRemoteBitmap(logoUrl)
    if (logoBitmap != null) {
        Image(
            bitmap = logoBitmap.asImageBitmap(),
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .clip(CircleShape)
                .background(Color.White, CircleShape)
        )
        return
    }

    val color = remember(name) {
        val colors = listOf(
            Color(0xFF006D77),
            Color(0xFF7B2CBF),
            Color(0xFFB23A48),
            Color(0xFF2D6A4F),
            Color(0xFF3A5A40),
            Color(0xFF3D405B)
        )
        colors[Math.abs(name.hashCode()) % colors.size]
    }
    Box(
        modifier = modifier
            .background(color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stationInitials(name),
            color = Color.White,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun rememberRemoteBitmap(url: String?): Bitmap? {
    val bitmapState = produceState<Bitmap?>(initialValue = null, url) {
        value = null
        val normalizedUrl = url?.trim().orEmpty()
        if (!normalizedUrl.startsWith("http://", ignoreCase = true) &&
            !normalizedUrl.startsWith("https://", ignoreCase = true)
        ) {
            return@produceState
        }
        value = withContext(Dispatchers.IO) {
            runCatching {
                val connection = URL(normalizedUrl).openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 8000
                connection.setRequestProperty("User-Agent", "M3U-Radyo-Android")
                try {
                    connection.inputStream.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                } finally {
                    connection.disconnect()
                }
            }.getOrNull()
        }
    }
    return bitmapState.value
}

fun stationInitials(name: String): String {
    val words = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return words.take(2).joinToString("") { it.first().uppercase() }.ifBlank { "R" }
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

fun openUrl(context: Context, url: String) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
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
