package com.samim.m3uradyo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RadioApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            setWakeMode(C.WAKE_MODE_NETWORK)
            volume = 1f
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
    var volume by remember { mutableStateOf(1f) }
    var previousAudibleVolume by remember { mutableStateOf(1f) }
    var isMuted by remember { mutableStateOf(false) }
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
        val maximumVolume = setMediaVolumeRatio(context, 1f)
        player.volume = 1f
        volume = maximumVolume
        previousAudibleVolume = maximumVolume.coerceAtLeast(0.05f)
        isMuted = false

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
                    Player.STATE_IDLE -> Unit
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
