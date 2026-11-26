package com.musica.paralosoidos.playback

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private var exoPlayer: ExoPlayer? = null
    private lateinit var stateStore: PlaybackStateStore

    private val handler = Handler(Looper.getMainLooper())
    private var isFadingIn = false
    private val crossfadeRunnable = object : Runnable {
        override fun run() {
            val player = exoPlayer
            if (player?.isPlaying == true) {
                applyCrossfadeTick(player)
            }
            handler.postDelayed(this, 250L)
        }
    }

    private val persistRunnable = object : Runnable {
        override fun run() {
            if (exoPlayer?.isPlaying == true) {
                persistState()
            }
            handler.postDelayed(this, PERSIST_INTERVAL_MS)
        }
    }

    private val persistenceListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)) {
                applyTransitionFadeIn()
            }
            if (events.contains(Player.EVENT_TIMELINE_CHANGED) ||
                events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION) ||
                events.contains(Player.EVENT_POSITION_DISCONTINUITY) ||
                events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED) ||
                events.contains(Player.EVENT_IS_PLAYING_CHANGED) ||
                events.contains(Player.EVENT_PLAY_WHEN_READY_CHANGED) ||
                events.contains(Player.EVENT_PLAYBACK_PARAMETERS_CHANGED) ||
                events.contains(Player.EVENT_REPEAT_MODE_CHANGED)
            ) {
                persistState()
            }
            if (events.contains(Player.EVENT_TIMELINE_CHANGED) ||
                events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION) ||
                events.contains(Player.EVENT_IS_PLAYING_CHANGED) ||
                events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED)
            ) {
                notifyWidgetStateChanged(player)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        stateStore = PlaybackStateStore(this)
        val player = ExoPlayer.Builder(this)
            .setHandleAudioBecomingNoisy(true)
            .build()
            .apply {
                setAudioAttributes(AudioAttributes.DEFAULT, true)
                setWakeMode(C.WAKE_MODE_LOCAL)
                addListener(object : Player.Listener {
                    override fun onAudioSessionIdChanged(audioSessionId: Int) {
                        AudioEffectsManager.attachSession(audioSessionId)
                    }
                })
            }
        exoPlayer = player
        AudioEffectsManager.attachSession(player.audioSessionId)
        restorePlaybackState(player)
        player.addListener(persistenceListener)
        mediaSession = MediaSession.Builder(this, player)
            .apply { createSessionActivity()?.let { setSessionActivity(it) } }
            .build()
        handler.postDelayed(persistRunnable, PERSIST_INTERVAL_MS)
        handler.postDelayed(crossfadeRunnable, 250L)
        notifyWidgetStateChanged(player)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val player = exoPlayer
        when (intent?.action) {
            ACTION_WIDGET_PLAY_PAUSE -> {
                if (player != null && player.mediaItemCount > 0) {
                    if (player.isPlaying) {
                        player.pause()
                    } else {
                        if (player.playbackState == Player.STATE_IDLE) {
                            player.prepare()
                        }
                        player.play()
                    }
                }
            }
            ACTION_WIDGET_NEXT -> {
                if (player != null && player.mediaItemCount > 0) {
                    if (player.hasNextMediaItem()) {
                        player.seekToNextMediaItem()
                    } else {
                        player.seekTo(0, 0L)
                    }
                    if (!player.isPlaying) player.play()
                }
            }
            ACTION_WIDGET_PREV -> {
                if (player != null && player.mediaItemCount > 0) {
                    if (player.currentPosition > 3_000L) {
                        player.seekTo(0L)
                    } else if (player.hasPreviousMediaItem()) {
                        player.seekToPreviousMediaItem()
                    } else {
                        player.seekTo(0L)
                    }
                    if (!player.isPlaying) player.play()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        persistState(synchronous = true)
        val player = exoPlayer
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    private fun restorePlaybackState(player: ExoPlayer) {
        val state = stateStore.load() ?: return
        val items = state.items.mapNotNull { stored ->
            val uri = Uri.parse(stored.uri)
            if (uri == Uri.EMPTY) {
                null
            } else {
                MediaItem.Builder()
                    .setMediaId(stored.mediaId)
                    .setUri(uri)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(stored.title)
                            .setArtist(stored.artist)
                            .setAlbumTitle(stored.album)
                            .apply {
                                stored.artworkUri?.takeIf(String::isNotBlank)?.let {
                                    setArtworkUri(Uri.parse(it))
                                }
                            }
                            .build()
                    )
                    .build()
            }
        }.distinctBy { item ->
            item.mediaId.ifBlank { item.localConfiguration?.uri?.toString().orEmpty() }
        }
        if (items.isEmpty()) return

        val index = state.currentIndex.coerceIn(0, items.lastIndex)
        player.setMediaItems(items, index, state.positionMs.coerceAtLeast(0L))
        player.setPlaybackSpeed(state.playbackSpeed.coerceIn(0.5f, 2.0f))
        player.repeatMode = state.repeatMode.coerceIn(
            Player.REPEAT_MODE_OFF,
            Player.REPEAT_MODE_ONE
        )
        player.prepare()
        player.playWhenReady = false
    }

    private fun persistState(synchronous: Boolean = false) {
        val player = exoPlayer ?: return
        if (player.mediaItemCount == 0) {
            stateStore.clear(synchronous)
            return
        }

        val items = (0 until player.mediaItemCount).mapNotNull { index ->
            val item = player.getMediaItemAt(index)
            val uri = item.localConfiguration?.uri ?: return@mapNotNull null
            StoredMediaItem(
                mediaId = item.mediaId,
                uri = uri.toString(),
                title = item.mediaMetadata.title?.toString(),
                artist = item.mediaMetadata.artist?.toString(),
                album = item.mediaMetadata.albumTitle?.toString(),
                artworkUri = item.mediaMetadata.artworkUri?.toString()
            )
        }
        if (items.isEmpty()) {
            stateStore.clear(synchronous)
            return
        }

        stateStore.save(
            StoredPlaybackState(
                items = items,
                currentIndex = player.currentMediaItemIndex.coerceIn(0, items.lastIndex),
                positionMs = player.currentPosition.coerceAtLeast(0L),
                playbackSpeed = player.playbackParameters.speed,
                repeatMode = player.repeatMode
            ),
            synchronous = synchronous
        )
    }

    private fun createSessionActivity(): PendingIntent? {
        val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return null
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onDestroy() {
        handler.removeCallbacks(persistRunnable)
        handler.removeCallbacks(crossfadeRunnable)
        persistState(synchronous = true)
        AudioEffectsManager.release()
        mediaSession?.release()
        exoPlayer?.release()
        mediaSession = null
        exoPlayer = null
        notifyWidgetStateChanged(null)
        super.onDestroy()
    }

    private fun notifyWidgetStateChanged(player: Player?) {
        val current = player?.currentMediaItem
        val title = current?.mediaMetadata?.title?.toString().orEmpty()
        val artist = current?.mediaMetadata?.artist?.toString().orEmpty()
        val artworkUri = current?.mediaMetadata?.artworkUri?.toString().orEmpty()
        val isPlaying = player?.isPlaying == true
        getSharedPreferences(WIDGET_PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putString(KEY_WIDGET_TITLE, title)
            .putString(KEY_WIDGET_ARTIST, artist)
            .putString(KEY_WIDGET_ARTWORK_URI, artworkUri)
            .putBoolean(KEY_WIDGET_IS_PLAYING, isPlaying)
            .apply()
        sendBroadcast(
            Intent(ACTION_WIDGET_STATE_CHANGED).setPackage(packageName)
        )
    }


    private fun applyCrossfadeTick(player: ExoPlayer) {
        if (isFadingIn) return
        val prefs = getSharedPreferences("playback_prefs", android.content.Context.MODE_PRIVATE)
        val crossfadeSec = prefs.getInt("crossfade_seconds", 0)
        if (crossfadeSec <= 0) {
            if (player.volume < 1f) player.volume = 1f
            return
        }
        val duration = player.duration
        val position = player.currentPosition
        if (duration <= 0L || position < 0L) return
        val remainingMs = duration - position
        val fadeMs = crossfadeSec * 1000L
        if (remainingMs in 0..fadeMs && (player.hasNextMediaItem() || player.repeatMode != Player.REPEAT_MODE_OFF)) {
            val factor = (remainingMs.toFloat() / fadeMs).coerceIn(0.05f, 1f)
            player.volume = factor
        } else if (player.volume < 1f) {
            player.volume = 1f
        }
    }

    private fun applyTransitionFadeIn() {
        val player = exoPlayer ?: return
        val prefs = getSharedPreferences("playback_prefs", android.content.Context.MODE_PRIVATE)
        val crossfadeSec = prefs.getInt("crossfade_seconds", 0)
        if (crossfadeSec <= 0) {
            player.volume = 1f
            return
        }
        val fadeMs = crossfadeSec * 1000L
        val startTime = System.currentTimeMillis()
        isFadingIn = true
        player.volume = 0.05f

        val fadeInRunnable = object : Runnable {
            override fun run() {
                val elapsed = System.currentTimeMillis() - startTime
                val factor = (elapsed.toFloat() / fadeMs).coerceIn(0.05f, 1f)
                val p = exoPlayer
                if (p != null) {
                    p.volume = factor
                    if (elapsed < fadeMs && p.isPlaying) {
                        handler.postDelayed(this, 50L)
                    } else {
                        p.volume = 1f
                        isFadingIn = false
                    }
                } else {
                    isFadingIn = false
                }
            }
        }
        handler.post(fadeInRunnable)
    }

    companion object {
        private const val PERSIST_INTERVAL_MS = 5_000L
        const val WIDGET_PREFS_NAME = "widget_state"
        const val KEY_WIDGET_TITLE = "title"
        const val KEY_WIDGET_ARTIST = "artist"
        const val KEY_WIDGET_ARTWORK_URI = "artwork_uri"
        const val KEY_WIDGET_IS_PLAYING = "is_playing"
        const val ACTION_WIDGET_STATE_CHANGED = "com.musica.paralosoidos.action.WIDGET_STATE_CHANGED"
        const val ACTION_WIDGET_PLAY_PAUSE = "com.musica.paralosoidos.action.WIDGET_PLAY_PAUSE"
        const val ACTION_WIDGET_PREV = "com.musica.paralosoidos.action.WIDGET_PREV"
        const val ACTION_WIDGET_NEXT = "com.musica.paralosoidos.action.WIDGET_NEXT"
    }
}
