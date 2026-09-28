package com.samim.m3uradyo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StationRepositoryTest {
    @Test
    fun parseM3uKeepsMetadataAndFirstOccurrenceOfEachStream() {
        val stations = parseM3u(
            """
            #EXTM3U
            #EXTINF:-1 tvg-logo="https://example.com/logo.png" group-title="Türkü", Radyo Bir
            https://example.com/live
            #EXTINF:-1 group-title="Pop", Sonraki Kayit
            https://example.com/live
            #EXTINF:-1, Haber FM
            http://example.com/news
            """.trimIndent()
        )

        assertEquals(
            listOf(
                RadioStation("Radyo Bir", "https://example.com/live", "https://example.com/logo.png", "Turku"),
                RadioStation("Haber FM", "http://example.com/news", category = "Haber")
            ),
            stations
        )
    }

    @Test
    fun parseM3uIgnoresUnsupportedLinesAndNamesBareUrls() {
        val stations = parseM3u(
            """
            #EXTM3U
            not-a-stream
            # A comment
            HTTPS://example.com/radio.mp3?quality=high
            """.trimIndent()
        )

        assertEquals(1, stations.size)
        assertEquals("radio.mp3", stations.single().name)
        assertEquals("Genel", stations.single().category)
    }

    @Test
    fun categoryOptionsKeepPreferredOrderAndAppendCustomCategories() {
        val options = buildCategoryOptions(
            listOf(
                RadioStation("A", "https://example.com/a", category = "Jazz"),
                RadioStation("B", "https://example.com/b", category = "Pop"),
                RadioStation("C", "https://example.com/c", category = "Jazz")
            )
        )

        assertEquals("Tum", options.first())
        assertEquals(1, options.count { it == "Pop" })
        assertEquals("Jazz", options.last())
    }

    @Test
    fun attributesAreCaseInsensitiveAndMissingAttributesStayNull() {
        assertEquals("Spor", extractM3uAttribute("#EXTINF:-1 GROUP-TITLE=\"Spor\", Test", "group-title"))
        assertNull(extractM3uAttribute("#EXTINF:-1, Test", "tvg-logo"))
    }
}
