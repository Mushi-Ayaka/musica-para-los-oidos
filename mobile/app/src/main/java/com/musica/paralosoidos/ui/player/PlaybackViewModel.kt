package com.musica.paralosoidos.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.musica.paralosoidos.data.LibraryRepository
import com.musica.paralosoidos.data.TrackEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlaybackViewModel(private val repository: LibraryRepository) : ViewModel() {
    private var controller: MediaController? = null
    private var listener: Player.Listener? = null
    private var tickerJob: Job? = null
    private var tracksById: Map<String, TrackEntity> = emptyMap()

    private val _nowPlaying = MutableStateFlow<TrackEntity?>(null)
    val nowPlaying: StateFlow<TrackEntity?> = _nowPlaying.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _position = MutableStateFlow(0f)
    val position: StateFlow<Float> = _position.asStateFlow()

    private val _duration = MutableStateFlow(1f)
    val duration: StateFlow<Float> = _duration.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _shuffleEnabled = MutableStateFlow(false)
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _playbackError = MutableStateFlow<String?>(null)
    val playbackError: StateFlow<String?> = _playbackError.asStateFlow()

    private val _queue = MutableStateFlow<List<TrackEntity>>(emptyList())
    val queue: StateFlow<List<TrackEntity>> = _queue.asStateFlow()

    init {
        viewModelScope.launch {
            repository.tracks.collect { list ->
                tracksById = list.associateBy { it.id.toString() }
                _nowPlaying.value = controller?.currentMediaItem?.mediaId?.let(tracksById::get)
            }
        }
    }

    fun attachController(newController: MediaController) {
        if (controller === newController) return
        detachController()
        controller = newController
        val newListener = object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                if (events.contains(Player.EVENT_TIMELINE_CHANGED) ||
                    events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)
                ) {
                    _nowPlaying.value = tracksById[player.currentMediaItem?.mediaId]
                    rebuildQueue(player)
                }
                if (events.contains(Player.EVENT_IS_PLAYING_CHANGED)) {
                    _isPlaying.value = player.isPlaying
                }
                if (events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED)) {
                    _duration.value = player.duration.takeIf { it > 0L }?.toFloat() ?: 1f
                }
                if (events.contains(Player.EVENT_POSITION_DISCONTINUITY)) {
                    _position.value = player.currentPosition.coerceAtLeast(0L).toFloat()
                }
                if (events.contains(Player.EVENT_SHUFFLE_MODE_ENABLED_CHANGED)) {
                    _shuffleEnabled.value = player.shuffleModeEnabled
                }
                if (events.contains(Player.EVENT_REPEAT_MODE_CHANGED)) {
                    _repeatMode.value = player.repeatMode
                }
                if (events.contains(Player.EVENT_PLAYBACK_PARAMETERS_CHANGED)) {
                    _playbackSpeed.value = player.playbackParameters.speed
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                _playbackError.value = error.localizedMessage ?: "No se pudo reproducir este archivo."
                _isPlaying.value = false
            }
        }
        listener = newListener
        newController.addListener(newListener)
        sync(newController)
        startTicker()
    }

    fun detachController() {
        listener?.let { controller?.removeListener(it) }
        listener = null
        controller = null
        tickerJob?.cancel()
        tickerJob = null
    }

    fun playTrack(track: TrackEntity, customQueue: List<TrackEntity>) {
        val player = controller
        if (player == null) {
            _playbackError.value = "El reproductor aún se está iniciando. Inténtalo de nuevo."
            return
        }
        _playbackError.value = null
        val items = customQueue.map(::mediaItemFor)
        val index = customQueue.indexOf(track).coerceAtLeast(0)
        player.setMediaItems(items, index, 0L)
        player.prepare()
        player.play()
    }

    fun togglePlayPause() {
        val player = controller ?: return
        if (player.isPlaying) player.pause() else player.play()
    }

    fun next() {
        controller?.seekToNextMediaItem()
    }

    fun seekTo(index: Int) {
        controller?.seekTo(index, 0L)
        controller?.play()
    }

    fun setSpeed(speed: Float) {
        controller?.setPlaybackSpeed(speed)
    }

    fun moveQueueItem(from: Int, to: Int) {
        controller?.moveMediaItem(from, to)
    }

    fun removeQueueItem(index: Int) {
        controller?.removeMediaItem(index)
    }

    fun addQueueItem(track: TrackEntity) {
        controller?.addMediaItem(mediaItemFor(track))
    }

    private fun sync(player: Player) {
        _nowPlaying.value = tracksById[player.currentMediaItem?.mediaId]
        _isPlaying.value = player.isPlaying
        _position.value = player.currentPosition.coerceAtLeast(0L).toFloat()
        _duration.value = player.duration.takeIf { it > 0L }?.toFloat() ?: 1f
        _shuffleEnabled.value = player.shuffleModeEnabled
        _repeatMode.value = player.repeatMode
        _playbackSpeed.value = player.playbackParameters.speed
        rebuildQueue(player)
    }

    private fun rebuildQueue(player: Player) {
        _queue.value = (0 until player.mediaItemCount).mapNotNull { index ->
            tracksById[player.getMediaItemAt(index).mediaId]
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                if (_isPlaying.value) {
                    controller?.let { player ->
                        _position.value = player.currentPosition.coerceAtLeast(0L).toFloat()
                        _duration.value = player.duration.takeIf { it > 0L }?.toFloat() ?: 1f
                    }
                }
                delay(500)
            }
        }
    }

    override fun onCleared() {
        detachController()
        super.onCleared()
    }

    class Factory(private val repository: LibraryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PlaybackViewModel::class.java)) {
                return PlaybackViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

private fun mediaItemFor(track: TrackEntity): MediaItem =
    MediaItem.Builder()
        .setMediaId(track.id.toString())
        .setUri(track.uri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(track.title)
                .setArtist(track.artist)
                .setAlbumTitle(track.album)
                .build()
        )
        .build()
