package com.hometunes.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hometunes.app.data.api.DownloadRepository
import com.hometunes.app.data.api.DownloadResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val url: String = "",
    val isLoading: Boolean = false,
    val progress: Float = 0f,
    val statusMessage: String = "",
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onUrlChanged(url: String) {
        _uiState.value = _uiState.value.copy(url = url, error = null, successMessage = null)
    }

    fun downloadTrack() {
        val url = _uiState.value.url
        if (url.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please enter a YouTube URL")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                progress = 0f,
                statusMessage = "Starting download...",
                error = null,
                successMessage = null
            )

            val result = downloadRepository.downloadTrack(url) { progress ->
                _uiState.value = _uiState.value.copy(
                    progress = progress,
                    statusMessage = when {
                        progress < 0.2f -> "Connecting to server..."
                        progress < 0.5f -> "Downloading from YouTube..."
                        progress < 0.8f -> "Processing audio..."
                        else -> "Saving to library..."
                    }
                )
            }

            _uiState.value = when (result) {
                is DownloadResult.Success -> {
                    _uiState.value.copy(
                        isLoading = false,
                        url = "",
                        progress = 1f,
                        successMessage = "Track added: ${result.track.title}",
                        statusMessage = "Done"
                    )
                }
                is DownloadResult.Error -> {
                    _uiState.value.copy(
                        isLoading = false,
                        progress = 0f,
                        error = result.message,
                        statusMessage = ""
                    )
                }
                is DownloadResult.AlreadyExists -> {
                    _uiState.value.copy(
                        isLoading = false,
                        progress = 0f,
                        error = result.message,
                        statusMessage = ""
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
