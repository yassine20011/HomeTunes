package com.hometunes.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.hometunes.app.ui.theme.Primary
import com.hometunes.app.viewmodel.PlayerViewModel

@Composable
fun PlayerScreen(
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val playerState by viewModel.playerState.collectAsState()
    val track = playerState.currentTrack

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F1A))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Minimize", tint = Color.White)
            }
            Text("Now Playing", color = Color.Gray)
            IconButton(onClick = { /* More options */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Artwork
        AsyncImage(
            model = track?.thumbnailPath,
            contentDescription = null,
            modifier = Modifier
                .size(300.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF2A2A4A)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Info
        Text(
            text = track?.title ?: "Select a track",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = track?.artist ?: "HomeTunes",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray,
            maxLines = 1
        )

        Spacer(modifier = Modifier.weight(1f))

        // Progress
        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = playerState.currentPosition.toFloat(),
                onValueChange = { viewModel.seekTo(it.toLong()) },
                valueRange = 0f..(playerState.duration.toFloat().coerceAtLeast(1f)),
                colors = SliderDefaults.colors(
                    thumbColor = Primary,
                    activeTrackColor = Primary,
                    inactiveTrackColor = Color(0xFF2A2A4A)
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(playerState.currentPosition),
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = formatTime(playerState.duration),
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.skipToPrevious() }, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(32.dp))
            }

            IconButton(
                onClick = { viewModel.togglePlayPause() },
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Primary)
            ) {
                Icon(
                    imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            IconButton(onClick = { viewModel.skipToNext() }, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
