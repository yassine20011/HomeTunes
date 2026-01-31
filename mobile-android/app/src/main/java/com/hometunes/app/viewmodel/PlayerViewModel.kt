package com.hometunes.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hometunes.app.player.PlayerManager
import com.hometunes.app.player.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerManager: PlayerManager
) : ViewModel() {

    val playerState: StateFlow<PlayerState> = playerManager.playerState

    // Poll for progress updates when playing
    init {
        viewModelScope.launch {
            while (isActive) {
                if (playerState.value.isPlaying) {
                    playerManager.updatePosition()
                }
                delay(1000)
            }
        }
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun skipToNext() {
        playerManager.skipToNext()
    }

    fun skipToPrevious() {
        playerManager.skipToPrevious()
    }

    fun seekTo(position: Long) {
        playerManager.seekTo(position)
    }
}
