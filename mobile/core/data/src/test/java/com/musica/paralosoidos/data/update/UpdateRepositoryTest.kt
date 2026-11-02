package com.musica.paralosoidos.data.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateRepositoryTest {

    @Test
    fun versionComparatorDetectsNewerVersionsCorrectly() {
        // Mismo release o inferior
        assertFalse(VersionComparator.isNewer("1.1.4", "1.1.4"))
        assertFalse(VersionComparator.isNewer("v1.1.4", "1.1.4"))
        assertFalse(VersionComparator.isNewer("1.1.4", "v1.1.4"))
        assertFalse(VersionComparator.isNewer("1.1.3", "1.1.4"))
        assertFalse(VersionComparator.isNewer("v1.0.9", "1.1.0"))

        // Mayor versión
        assertTrue(VersionComparator.isNewer("1.2.0", "1.1.4"))
        assertTrue(VersionComparator.isNewer("v1.2.0", "1.1.4"))
        assertTrue(VersionComparator.isNewer("v1.1.5", "1.1.4"))
        assertTrue(VersionComparator.isNewer("v1.1.10", "1.1.4"))
        assertTrue(VersionComparator.isNewer("2.0.0", "1.1.4"))

        // Variaciones con guiones y formatos incompletos
        assertFalse(VersionComparator.isNewer("1.1.4-debug", "1.1.4"))
        assertTrue(VersionComparator.isNewer("1.2", "1.1.4"))
        assertFalse(VersionComparator.isNewer("1.1.4", "1.2"))
    }

    @Test
    fun releaseParserSelectsArm64AssetAndExtractsFields() {
        val sampleJson = """
        {
          "tag_name": "v1.2.0",
          "name": "Música para los Oídos v1.2.0",
          "assets": [
            {
              "name": "Musica-para-los-Oidos-v1.2.0-universal.apk",
              "size": 5000000,
              "browser_download_url": "https://example.com/universal.apk"
            },
            {
              "name": "Musica-para-los-Oidos-v1.2.0-arm64-release.apk",
              "size": 3100000,
              "browser_download_url": "https://example.com/arm64-release.apk",
              "digest": "sha256:abcd1234ef5678"
            }
          ]
        }
        """.trimIndent()

        val parsed = ReleaseParser.parse(sampleJson)
        assertNotNull(parsed)
        assertEquals("v1.2.0", parsed!!.tagName)
        assertEquals("1.2.0", parsed.versionName)
        assertEquals("Musica-para-los-Oidos-v1.2.0-arm64-release.apk", parsed.assetName)
        assertEquals("https://example.com/arm64-release.apk", parsed.downloadUrl)
        assertEquals(3100000L, parsed.sizeBytes)
        assertEquals("sha256:abcd1234ef5678", parsed.digest)
    }

    @Test
    fun releaseParserFallbacksToGenericApkWhenArm64Absent() {
        val sampleJson = """
        {
          "tag_name": "v1.2.0",
          "assets": [
            {
              "name": "Musica-generic.apk",
              "size": 4200000,
              "browser_download_url": "https://example.com/generic.apk"
            }
          ]
        }
        """.trimIndent()

        val parsed = ReleaseParser.parse(sampleJson)
        assertNotNull(parsed)
        assertEquals("Musica-generic.apk", parsed!!.assetName)
        assertEquals("https://example.com/generic.apk", parsed.downloadUrl)
    }

    @Test
    fun releaseParserReturnsNullWhenNoApkOrEmptyTag() {
        assertNull(ReleaseParser.parse("{}"))
        assertNull(ReleaseParser.parse("""{"tag_name": "v1.0", "assets": [{"name": "readme.txt", "browser_download_url": "https://example.com"}]}"""))
        assertNull(ReleaseParser.parse("""{"tag_name": "", "assets": [{"name": "app.apk", "browser_download_url": "https://example.com"}]}"""))
    }
}
