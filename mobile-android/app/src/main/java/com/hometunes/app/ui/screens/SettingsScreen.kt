package com.hometunes.app.ui.screens

import android.content.Intent
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hometunes.app.ui.theme.Primary
import com.hometunes.app.ui.theme.Surface
import com.hometunes.app.viewmodel.SettingsViewModel
import java.io.File

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
        val context = LocalContext.current
        val uiState by viewModel.uiState.collectAsState()
        val serverUrl by viewModel.serverUrl.collectAsState()
        val audioQuality by viewModel.audioQuality.collectAsState()
        val musicDirectory by viewModel.musicDirectory.collectAsState()
        var tempUrl by remember(serverUrl) { mutableStateOf(serverUrl) }

        // Folder picker launcher - converts URI to file path
        val folderPickerLauncher =
                rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.OpenDocumentTree()
                ) { uri ->
                        uri?.let {
                                // Take persistable permission
                                context.contentResolver.takePersistableUriPermission(
                                        it,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                )
                                // Convert URI to file path (e.g., "primary:Music/MyFolder" ->
                                // "/storage/emulated/0/Music/MyFolder")
                                val path =
                                        it.lastPathSegment?.replace(
                                                "primary:",
                                                "/storage/emulated/0/"
                                        )
                                                ?: it.toString()
                                viewModel.setMusicDirectory(path)
                        }
                }

        // Get display path for music directory
        val displayPath =
                if (musicDirectory.isBlank()) {
                        val defaultDir =
                                File(
                                        Environment.getExternalStoragePublicDirectory(
                                                Environment.DIRECTORY_MUSIC
                                        ),
                                        "HomeTunes"
                                )
                        "Default: ${defaultDir.absolutePath}"
                } else {
                        musicDirectory
                }

        Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F1A)).padding(20.dp)) {
                CenterAlignedTopAppBar(
                        title = { Text("Settings", color = Color.White) },
                        colors =
                                TopAppBarDefaults.centerAlignedTopAppBarColors(
                                        containerColor = Color(0xFF0F0F1A)
                                )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                        "Server Configuration",
                        style = MaterialTheme.typography.titleMedium,
                        color = Primary
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                        value = tempUrl,
                        onValueChange = { tempUrl = it },
                        label = { Text("Server URL") },
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                                OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Surface,
                                        unfocusedContainerColor = Surface,
                                        focusedBorderColor = Primary,
                                        unfocusedBorderColor = Color(0xFF2A2A4A)
                                )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                        onClick = { viewModel.setServerUrl(tempUrl) },
                        modifier = Modifier.align(Alignment.End),
                        enabled = !uiState.isChecking
                ) {
                        if (uiState.isChecking) {
                                CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Save & Check")
                }

                if (uiState.serverStatus != "Unknown") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                                val icon =
                                        when (uiState.serverStatus) {
                                                "Online" -> Icons.Default.CheckCircle
                                                "Offline" -> Icons.Default.Error
                                                else -> Icons.Default.Info
                                        }
                                val color =
                                        when (uiState.serverStatus) {
                                                "Online" -> Color.Green
                                                "Offline" -> Color.Red
                                                else -> Color.Gray
                                        }

                                Icon(icon, contentDescription = null, tint = color)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Server: ${uiState.serverStatus}", color = color)
                        }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Music Storage Section
                Text("Music Storage", style = MaterialTheme.typography.titleMedium, color = Primary)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Icon(
                                Icons.Default.Folder,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                                displayPath,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                        )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                        OutlinedButton(
                                onClick = { folderPickerLauncher.launch(null) },
                                modifier = Modifier.weight(1f)
                        ) { Text("Choose Folder") }

                        if (musicDirectory.isNotBlank()) {
                                OutlinedButton(
                                        onClick = { viewModel.setMusicDirectory("") },
                                        modifier = Modifier.weight(1f)
                                ) { Text("Use Default") }
                        }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text("Audio Quality", style = MaterialTheme.typography.titleMedium, color = Primary)
                Spacer(modifier = Modifier.height(16.dp))

                QualityOption("High (320 kbps)", "320", audioQuality) {
                        viewModel.setAudioQuality("320")
                }
                QualityOption("Medium (192 kbps)", "192", audioQuality) {
                        viewModel.setAudioQuality("192")
                }
                QualityOption("Low (128 kbps)", "128", audioQuality) {
                        viewModel.setAudioQuality("128")
                }
        }
}

@Composable
fun QualityOption(label: String, value: String, selectedValue: String, onSelect: () -> Unit) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
                RadioButton(
                        selected = value == selectedValue,
                        onClick = onSelect,
                        colors = RadioButtonDefaults.colors(selectedColor = Primary)
                )
                Text(label, color = Color.White, modifier = Modifier.padding(start = 8.dp))
        }
}
