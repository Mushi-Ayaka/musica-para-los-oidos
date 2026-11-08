package com.musica.paralosoidos.ui.downloader

import com.musica.paralosoidos.data.AudioOutputFormat
import com.musica.paralosoidos.data.DownloadSearchResult
import com.musica.paralosoidos.data.DownloadTask
import com.musica.paralosoidos.data.DownloadTaskStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloaderModelTest {

    @Test
    fun formattedDurationFormatsSecondsProperly() {
        val result = DownloadSearchResult(
            id = "test1",
            title = "Test Song",
            channel = "Test Channel",
            durationSeconds = 215,
            thumbnailUrl = ""
        )
        assertEquals("03:35", result.formattedDuration)

        val zeroResult = result.copy(durationSeconds = 0)
        assertEquals("00:00", zeroResult.formattedDuration)

        val longResult = result.copy(durationSeconds = 3665)
        assertEquals("61:05", longResult.formattedDuration)
    }

    @Test
    fun formattedViewsFormatsMillionsAndThousandsProperly() {
        val million = DownloadSearchResult(
            id = "1",
            title = "Song",
            channel = "Channel",
            durationSeconds = 100,
            thumbnailUrl = "",
            viewCount = 2_450_000
        )
        assertTrue(million.formattedViews.contains("2.5M") || million.formattedViews.contains("2,5M"))

        val thousand = million.copy(viewCount = 45_000)
        assertTrue(thousand.formattedViews.contains("45.0K") || thousand.formattedViews.contains("45,0K"))

        val small = million.copy(viewCount = 500)
        assertEquals("500 vistas", small.formattedViews)

        val zero = million.copy(viewCount = 0)
        assertEquals("", zero.formattedViews)
    }

    @Test
    fun qualityOptionsContainDefaultBitratesAndOriginal() {
        assertEquals(4, QUALITY_OPTIONS.size)
        assertEquals(AudioOutputFormat.MP3, QUALITY_OPTIONS[0].format)
        assertEquals(320, QUALITY_OPTIONS[0].bitrate)
        assertEquals(AudioOutputFormat.BEST, QUALITY_OPTIONS[3].format)
        assertEquals(null, QUALITY_OPTIONS[3].bitrate)
    }

    @Test
    fun downloadTaskInitializesWithQueuedStatusAndDefaultValues() {
        val task = DownloadTask(
            id = "task_1",
            videoId = "vid_123",
            title = "Pista de Prueba",
            channel = "Canal Oficial",
            thumbnailUrl = "https://example.com/thumb.jpg",
            format = AudioOutputFormat.MP3,
            bitrate = 320,
            syncLyrics = true
        )

        assertEquals(DownloadTaskStatus.QUEUED, task.status)
        assertEquals(0, task.progress)
        assertEquals("", task.speed)
        assertNull(task.errorMessage)
        assertTrue(task.addedAt > 0L)
    }

    @Test
    fun downloadTaskStatusContainsAllDefinedStates() {
        val statuses = DownloadTaskStatus.entries
        assertEquals(5, statuses.size)
        assertTrue(statuses.contains(DownloadTaskStatus.QUEUED))
        assertTrue(statuses.contains(DownloadTaskStatus.DOWNLOADING))
        assertTrue(statuses.contains(DownloadTaskStatus.PAUSED))
        assertTrue(statuses.contains(DownloadTaskStatus.COMPLETED))
        assertTrue(statuses.contains(DownloadTaskStatus.ERROR))
    }
}
