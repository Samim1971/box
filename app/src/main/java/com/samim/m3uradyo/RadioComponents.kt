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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    accentColor: AccentColor,
    onAccentColorChange: (AccentColor) -> Unit,
    onOpenBluetooth: () -> Unit,
    onRefreshBluetooth: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
            .padding(top = 12.dp),
        shape = RoundedCornerShape(8.dp),
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
                text = "Buton rengi",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
            ThemeColorPicker(
                selectedColor = accentColor,
                onColorChange = onAccentColorChange
            )
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
fun ThemeColorPicker(
    selectedColor: AccentColor,
    onColorChange: (AccentColor) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(AccentColor.entries, key = { it.name }) { accentColor ->
            val selected = accentColor == selectedColor
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = { onColorChange(accentColor) },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentPreviewColor(accentColor),
                        contentColor = accentPreviewContentColor(accentColor)
                    ),
                    modifier = Modifier
                        .size(44.dp)
                        .border(
                            width = if (selected) 3.dp else 1.dp,
                            color = if (selected) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    accentPreviewContentColor(accentColor),
                                    CircleShape
                                )
                        )
                    }
                }
                Text(
                    text = accentColor.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

private val AccentColor.label: String
    get() = when (this) {
        AccentColor.Coral -> "Mercan"
        AccentColor.Turquoise -> "Turkuaz"
        AccentColor.Blue -> "Mavi"
        AccentColor.Green -> "Yesil"
        AccentColor.Amber -> "Sari"
        AccentColor.Purple -> "Mor"
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
        shape = RoundedCornerShape(8.dp),
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
fun RadioTopBar(
    isMenuOpen: Boolean,
    searchText: String,
    onMenuToggle: () -> Unit,
    onSearchChange: (String) -> Unit,
    onClearSearch: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onMenuToggle,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(width = 50.dp, height = 44.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                MenuLines()
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = "KANALIXOR",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1
                )
                Text(
                    text = if (isMenuOpen) "MENU" else "RADIO",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            Text(
                text = "CANLI",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier
                    .padding(start = 6.dp)
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = onSearchChange,
                placeholder = {
                    Text(
                        text = "Radyo ara",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
            )
            Button(
                onClick = onClearSearch,
                enabled = searchText.isNotBlank(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(46.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("X", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun NowPlayingPanel(
    station: RadioStation,
    playbackState: PlaybackUiState,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StationLogo(
                    name = station.name,
                    logoUrl = station.logoUrl,
                    modifier = Modifier.size(72.dp)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = station.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = station.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier.padding(top = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(
                                    if (isPlaying) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline,
                                    CircleShape
                                )
                        )
                        Text(
                            text = playbackState.label.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isPlaying) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
                EqualizerBars(active = isPlaying)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallActionButton(
                    text = if (isFavorite) "FAVORIDEN CIKAR" else "FAVORI",
                    modifier = Modifier.weight(1f),
                    onClick = onToggleFavorite
                )
                SmallActionButton(
                    text = "PAYLAS",
                    modifier = Modifier.weight(1f),
                    onClick = onShare
                )
            }
        }
    }
}

@Composable
private fun EqualizerBars(active: Boolean) {
    val color = if (active) MaterialTheme.colorScheme.secondary
    else MaterialTheme.colorScheme.outline
    Canvas(modifier = Modifier.size(width = 34.dp, height = 42.dp)) {
        val heights = listOf(0.38f, 0.72f, 1f, 0.62f, 0.84f)
        val barWidth = size.width / 9f
        heights.forEachIndexed { index, factor ->
            val barHeight = size.height * factor
            drawRoundRect(
                color = color,
                topLeft = Offset(index * barWidth * 2f, (size.height - barHeight) / 2f),
                size = Size(barWidth, barHeight)
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
        shape = RoundedCornerShape(8.dp),
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
        shape = RoundedCornerShape(8.dp),
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
                shape = RoundedCornerShape(6.dp),
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
                shape = RoundedCornerShape(8.dp),
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
        shape = RoundedCornerShape(6.dp),
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
fun StationSectionHeader(
    filter: StationFilter,
    count: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "RADYO ISTASYONLARI",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = activeFilterLabel(filter, count),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun PlayerDock(
    station: RadioStation?,
    playbackState: PlaybackUiState,
    isPlaying: Boolean,
    volume: Float,
    isMuted: Boolean,
    onPrevious: () -> Unit,
    onToggle: () -> Unit,
    onStop: () -> Unit,
    onNext: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onMuteToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StationLogo(
                    name = station?.name ?: APP_DISPLAY_NAME,
                    logoUrl = station?.logoUrl,
                    modifier = Modifier.size(36.dp)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp)
                ) {
                    Text(
                        text = station?.name ?: "Bir radyo secin",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = playbackState.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                Text(
                    text = "${(volume * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundControlButton(icon = ControlIcon.Previous, onClick = onPrevious)
                RoundControlButton(
                    icon = if (isPlaying) ControlIcon.Pause else ControlIcon.Play,
                    onClick = onToggle,
                    isPrimary = true
                )
                RoundControlButton(
                    icon = ControlIcon.Stop,
                    onClick = onStop,
                    isStop = true
                )
                RoundControlButton(icon = ControlIcon.Next, onClick = onNext)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SES",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(34.dp)
                )
                Slider(
                    value = volume,
                    onValueChange = { onVolumeChange(it.coerceIn(0f, 1f)) },
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.secondary,
                        activeTrackColor = MaterialTheme.colorScheme.secondary,
                        inactiveTrackColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = onMuteToggle,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMuted) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface,
                        contentColor = if (isMuted) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .height(38.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Text(
                        text = if (isMuted) "SES AC" else "SESSIZ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
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
        verticalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(bottom = 2.dp)
    ) {
        items(stations, key = { it.streamUrl }) { station ->
            val isSelected = station == selectedStation
            val isFavorite = favorites.contains(station.streamUrl)
            var stationMenuOpen by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .combinedClickable(
                            enabled = enabled,
                            onClick = { onSelect(station) },
                            onLongClick = { stationMenuOpen = true }
                        ),
                    shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StationLogo(
                            station.name,
                            station.logoUrl,
                            modifier = Modifier.size(50.dp)
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 10.dp)
                        ) {
                            Text(
                                text = station.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                )
                                Text(
                                    text = "CANLI  ${station.category}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(start = 6.dp)
                                )
                            }
                        }
                        Button(
                            onClick = { onToggleFavorite(station) },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFavorite) MaterialTheme.colorScheme.tertiary
                                else MaterialTheme.colorScheme.surface,
                                contentColor = if (isFavorite) MaterialTheme.colorScheme.onTertiary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.size(42.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            FavoriteGlyph(
                                color = if (isFavorite) MaterialTheme.colorScheme.onTertiary
                                else MaterialTheme.colorScheme.onSurfaceVariant
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
private fun FavoriteGlyph(color: Color) {
    Canvas(modifier = Modifier.size(22.dp)) {
        val outer = size.minDimension / 2f
        val inner = outer * 0.46f
        val center = Offset(size.width / 2f, size.height / 2f)
        val path = Path()
        repeat(10) { index ->
            val angle = Math.toRadians((-90.0 + index * 36.0))
            val radius = if (index % 2 == 0) outer else inner
            val point = Offset(
                center.x + (kotlin.math.cos(angle) * radius).toFloat(),
                center.y + (kotlin.math.sin(angle) * radius).toFloat()
            )
            if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
        }
        path.close()
        drawPath(path, color)
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
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White, RoundedCornerShape(8.dp))
        )
        return
    }

    Image(
        painter = painterResource(R.drawable.ic_radio_player),
        contentDescription = "$name standart radyo logosu",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
    )
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
