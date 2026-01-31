package com.hometunes.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hometunes.app.data.database.TrackEntity
import com.hometunes.app.data.repository.TrackRepository
import com.hometunes.app.player.PlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class LibraryViewModel
@Inject
constructor(
        private val trackRepository: TrackRepository,
        private val playerManager: PlayerManager
) : ViewModel() {

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    val tracks: StateFlow<List<TrackEntity>> =
            trackRepository.allTracks.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList()
            )

    fun scanLocalMusic() {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                val count = trackRepository.importLocalTracks()
                // Could show success message via One-time event or shared flow
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun playTrack(track: TrackEntity) {
        val currentList = tracks.value
        val index = currentList.indexOf(track)
        if (index != -1) {
            playerManager.playAll(currentList, index)
        } else {
            playerManager.playTrack(track)
        }
    }

    fun playAll(startIndex: Int = 0) {
        val currentList = tracks.value
        if (currentList.isNotEmpty()) {
            playerManager.playAll(currentList, startIndex)
        }
    }

    fun deleteTrack(track: TrackEntity) {
        viewModelScope.launch {
            // Delete the actual audio file from storage
            try {
                val audioFile = java.io.File(track.filePath)
                if (audioFile.exists()) {
                    audioFile.delete()
                }
                // Delete thumbnail if exists
                track.thumbnailPath?.let { path ->
                    val thumbnailFile = java.io.File(path)
                    if (thumbnailFile.exists()) {
                        thumbnailFile.delete()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            // Delete from database
            trackRepository.deleteTrack(track)
        }
    }
}
