package com.hometunes.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hometunes.app.data.api.DownloadRepository
import com.hometunes.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
        val serverStatus: String = "Unknown", // "Online", "Offline", "Checking", "Unknown"
        val isChecking: Boolean = false
)

@HiltViewModel
class SettingsViewModel
@Inject
constructor(
        private val settingsRepository: SettingsRepository,
        private val downloadRepository: DownloadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val serverUrl: StateFlow<String> =
            settingsRepository.serverUrl.stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5000),
                    ""
            )

    val audioQuality: StateFlow<String> =
            settingsRepository.audioQuality.stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5000),
                    "192"
            )

    val musicDirectory: StateFlow<String> =
            settingsRepository.musicDirectory.stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5000),
                    ""
            )

    fun setServerUrl(url: String) {
        viewModelScope.launch {
            settingsRepository.setServerUrl(url)
            checkServerStatus()
        }
    }

    fun setAudioQuality(quality: String) {
        viewModelScope.launch { settingsRepository.setAudioQuality(quality) }
    }

    fun setMusicDirectory(uri: String) {
        viewModelScope.launch { settingsRepository.setMusicDirectory(uri) }
    }

    fun checkServerStatus() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isChecking = true, serverStatus = "Checking")
            val isOnline = downloadRepository.checkServerHealth()
            _uiState.value =
                    _uiState.value.copy(
                            isChecking = false,
                            serverStatus = if (isOnline) "Online" else "Offline"
                    )
        }
    }
}
