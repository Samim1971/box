package com.samim.m3uradyo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt

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
