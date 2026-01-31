package com.hometunes.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hometunes.app.R
import com.hometunes.app.ui.theme.Primary
import com.hometunes.app.ui.theme.Surface
import com.hometunes.app.viewmodel.HomeViewModel

@Composable
fun HomeScreen(showSnackbar: (String) -> Unit, viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            if (it.isNotEmpty()) {
                // We show the error in the UI, so we don't clear it immediately.
                // It will be cleared when the user starts typing a new URL.
            }
        }
    }

    Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Header - App Logo
        Box(
                modifier = Modifier.size(80.dp).clip(CircleShape).background(Primary),
                contentAlignment = Alignment.Center
        ) {
            Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "HomeTunes Logo",
                    modifier = Modifier.size(64.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "HomeTunes", style = MaterialTheme.typography.displayLarge, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
                text = "Your personal music library",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Input Section
        Text(
                text = "Paste YouTube URL",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
        )

        OutlinedTextField(
                value = uiState.url,
                onValueChange = viewModel::onUrlChanged,
                placeholder = { Text("https://youtube.com/watch?v=...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors =
                        OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Surface,
                                unfocusedContainerColor = Surface,
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Color(0xFF2A2A4A)
                        ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                enabled = !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.downloadTrack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors =
                        ButtonDefaults.buttonColors(
                                containerColor = Primary,
                                disabledContainerColor = Color(0xFF4B4B8F)
                        ),
                enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = uiState.statusMessage.ifEmpty { "Processing..." })
                }
            } else {
                Text(
                        text = "Download",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                )
            }
        }

        if (uiState.isLoading && uiState.progress > 0) {
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                    progress = { uiState.progress },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = Primary,
                    trackColor = Color(0xFF2A2A4A),
            )
        }

        uiState.error?.let { error ->
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                            Modifier.fillMaxWidth()
                                    .background(Color(0xFF2A1A1A), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = error, color = Color.Red, modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Instructions
        Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Surface),
                shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                        text = "How it works",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))
                InstructionItem("1. Copy a YouTube URL from the YouTube app")
                InstructionItem("2. Paste it above and tap Download")
                InstructionItem("3. The audio will be saved to your library")
                InstructionItem("4. Play anytime, even offline!")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun InstructionItem(text: String) {
    Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
    )
}
