package com.samim.m3uradyo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateCheckerTest {
    @Test
    fun radioVersionFromApkNameAcceptsOnlyKanalixorRadioPackages() {
        assertEquals("0.5.7", radioVersionFromApkName("Kanalixor-Radio-0.5.7.apk"))
        assertNull(radioVersionFromApkName("kanalixor-radio-1.0.0.APK"))
        assertNull(radioVersionFromApkName("Kanalixor-IPTV-Demo-0.9.0.apk"))
        assertNull(radioVersionFromApkName("M3U-Radyo-0.5.3.apk"))
        assertNull(radioVersionFromApkName("Kanalixor-Radio-beta.apk"))
        assertNull(radioVersionFromApkName("Kanalixor-Radio-0.5.apk"))
        assertNull(radioVersionFromApkName("Kanalixor-Radio-0.5.7.1.apk"))
        assertNull(radioVersionFromApkName("Kanalixor-Radio-0.5.7-debug.apk"))
    }

    @Test
    fun selectLatestRadioReleaseUsesSemanticVersionOrder() {
        val selected = selectLatestRadioRelease(
            listOf(
                candidate("0.5.9"),
                candidate("0.10.0"),
                candidate("0.6.0")
            )
        )

        assertEquals("0.10.0", selected?.version)
    }

    @Test
    fun compareVersionsHandlesDifferentSegmentCounts() {
        assertEquals(0, compareVersions("v0.5.6", "0.5.6.0"))
        assertTrue(compareVersions("0.5.7", "0.5.6") > 0)
        assertTrue(compareVersions("1.0.0", "0.99.99") > 0)
    }

    private fun candidate(version: String) = RadioReleaseCandidate(
        version = version,
        releaseName = "Kanalixor Radio $version",
        releaseUrl = "https://github.com/Samim1971/box/releases/tag/v$version",
        downloadUrl =
            "https://github.com/Samim1971/box/releases/download/v$version/" +
                "Kanalixor-Radio-$version.apk"
    )
}
