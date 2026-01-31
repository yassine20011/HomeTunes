package com.hometunes.app.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.hometunes.app.data.database.TrackEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class PlayerState(
    val isPlaying: Boolean = false,
    val currentTrack: TrackEntity? = null,
    val currentPosition: Long = 0,
    val duration: Long = 0,
    val queue: List<TrackEntity> = emptyList(),
    val currentIndex: Int = -1
)

@Singleton
class PlayerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var currentQueue: List<TrackEntity> = emptyList()

    fun initialize() {
        val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture?.addListener({
            controller = controllerFuture?.get()
            setupPlayerListener()
        }, MoreExecutors.directExecutor())
    }

    private fun setupPlayerListener() {
        controller?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val index = controller?.currentMediaItemIndex ?: -1
                val track = if (index >= 0 && index < currentQueue.size) currentQueue[index] else null
                _playerState.value = _playerState.value.copy(
                    currentTrack = track,
                    currentIndex = index
                )
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                updatePosition()
            }
        })
    }

    fun updatePosition() {
        controller?.let { ctrl ->
            _playerState.value = _playerState.value.copy(
                currentPosition = ctrl.currentPosition,
                duration = ctrl.duration.coerceAtLeast(0)
            )
        }
    }

    fun playTrack(track: TrackEntity) {
        currentQueue = listOf(track)
        controller?.apply {
            setMediaItem(track.toMediaItem())
            prepare()
            play()
        }
        _playerState.value = _playerState.value.copy(
            currentTrack = track,
            queue = currentQueue,
            currentIndex = 0
        )
    }

    fun playAll(tracks: List<TrackEntity>, startIndex: Int = 0) {
        currentQueue = tracks
        controller?.apply {
            setMediaItems(tracks.map { it.toMediaItem() }, startIndex, 0)
            prepare()
            play()
        }
        _playerState.value = _playerState.value.copy(
            queue = tracks,
            currentIndex = startIndex,
            currentTrack = tracks.getOrNull(startIndex)
        )
    }

    fun togglePlayPause() {
        controller?.let { ctrl ->
            if (ctrl.isPlaying) ctrl.pause() else ctrl.play()
        }
    }

    fun skipToNext() {
        controller?.seekToNext()
    }

    fun skipToPrevious() {
        controller?.seekToPrevious()
    }

    fun seekTo(position: Long) {
        controller?.seekTo(position)
    }

    fun release() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }

    private fun TrackEntity.toMediaItem(): MediaItem {
        val metadataBuilder = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist ?: "Unknown Artist")
            .setAlbumTitle("HomeTunes")
            
        thumbnailPath?.let {
            metadataBuilder.setArtworkUri(Uri.fromFile(File(it)))
        }

        return MediaItem.Builder()
            .setMediaId(id)
            .setUri(File(filePath).toURI().toString())
            .setMediaMetadata(metadataBuilder.build())
            .build()
    }
}
