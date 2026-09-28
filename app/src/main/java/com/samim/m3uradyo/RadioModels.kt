package com.samim.m3uradyo


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
