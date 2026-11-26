package com.musica.paralosoidos.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.musica.paralosoidos.MainActivity
import com.musica.paralosoidos.R
import com.musica.paralosoidos.playback.PlaybackService
import kotlin.concurrent.thread

class MusicWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateWidgetsAsync(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            PlaybackService.ACTION_WIDGET_STATE_CHANGED -> {
                val manager = AppWidgetManager.getInstance(context)
                val ids = manager.getAppWidgetIds(ComponentName(context, MusicWidgetProvider::class.java))
                if (ids.isNotEmpty()) {
                    updateWidgetsAsync(context, manager, ids)
                }
            }
            PlaybackService.ACTION_WIDGET_PLAY_PAUSE,
            PlaybackService.ACTION_WIDGET_PREV,
            PlaybackService.ACTION_WIDGET_NEXT -> {
                val serviceIntent = Intent(context, PlaybackService::class.java).apply {
                    action = intent.action
                }
                ContextCompat.startForegroundService(context, serviceIntent)
            }
        }
    }

    private fun updateWidgetsAsync(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        thread(name = "MusicWidgetUpdate") {
            try {
                val prefs = context.getSharedPreferences(PlaybackService.WIDGET_PREFS_NAME, Context.MODE_PRIVATE)
                val title = prefs.getString(PlaybackService.KEY_WIDGET_TITLE, "")
                    ?.takeIf { it.isNotBlank() }
                    ?: context.getString(R.string.widget_default_title)
                val artist = prefs.getString(PlaybackService.KEY_WIDGET_ARTIST, "")
                    ?.takeIf { it.isNotBlank() }
                    ?: context.getString(R.string.widget_default_artist)
                val artworkUri = prefs.getString(PlaybackService.KEY_WIDGET_ARTWORK_URI, "").orEmpty()
                val isPlaying = prefs.getBoolean(PlaybackService.KEY_WIDGET_IS_PLAYING, false)
                val coverBitmap = artworkUri.takeIf { it.isNotBlank() }?.let { loadScaledBitmap(context, it) }

                for (widgetId in appWidgetIds) {
                    val views = buildRemoteViews(
                        context = context,
                        title = title,
                        artist = artist,
                        isPlaying = isPlaying,
                        coverBitmap = coverBitmap
                    )
                    appWidgetManager.updateAppWidget(widgetId, views)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun buildRemoteViews(
        context: Context,
        title: String,
        artist: String,
        isPlaying: Boolean,
        coverBitmap: Bitmap?
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_music_4x1)
        views.setTextViewText(R.id.widget_title, title)
        views.setTextViewText(R.id.widget_artist, artist)
        views.setImageViewResource(
            R.id.widget_play_pause,
            if (isPlaying) R.drawable.ic_widget_pause else R.drawable.ic_widget_play
        )
        if (coverBitmap != null) {
            views.setImageViewBitmap(R.id.widget_cover, coverBitmap)
        } else {
            views.setImageViewResource(R.id.widget_cover, R.drawable.ic_widget_music)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            100,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_cover, openAppPendingIntent)
        views.setOnClickPendingIntent(R.id.widget_text_container, openAppPendingIntent)

        views.setOnClickPendingIntent(
            R.id.widget_prev,
            actionPendingIntent(context, PlaybackService.ACTION_WIDGET_PREV, 101)
        )
        views.setOnClickPendingIntent(
            R.id.widget_play_pause,
            actionPendingIntent(context, PlaybackService.ACTION_WIDGET_PLAY_PAUSE, 102)
        )
        views.setOnClickPendingIntent(
            R.id.widget_next,
            actionPendingIntent(context, PlaybackService.ACTION_WIDGET_NEXT, 103)
        )
        return views
    }

    private fun actionPendingIntent(context: Context, action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, MusicWidgetProvider::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun loadScaledBitmap(context: Context, uriString: String): Bitmap? =
        runCatching {
            val uri = Uri.parse(uriString)
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            }
            val targetSize = 144
            var sampleSize = 1
            while (boundsOptions.outWidth / sampleSize > targetSize * 2 ||
                boundsOptions.outHeight / sampleSize > targetSize * 2
            ) {
                sampleSize *= 2
            }
            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            }
        }.getOrNull()

    companion object {
        fun requestPin(context: Context): Boolean {
            val manager = AppWidgetManager.getInstance(context)
            if (!manager.isRequestPinAppWidgetSupported) return false
            val provider = ComponentName(context, MusicWidgetProvider::class.java)
            return manager.requestPinAppWidget(provider, null, null)
        }
    }
}
