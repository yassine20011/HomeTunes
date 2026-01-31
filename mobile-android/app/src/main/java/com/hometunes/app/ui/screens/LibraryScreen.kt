package com.hometunes.app.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.hometunes.app.data.database.TrackEntity
import com.hometunes.app.ui.theme.Primary
import com.hometunes.app.ui.theme.Surface
import com.hometunes.app.viewmodel.LibraryViewModel

@Composable
fun LibraryScreen(
    onNavigateToPlayer: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val tracks by viewModel.tracks.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val context = LocalContext.current
    var trackToDelete by remember { mutableStateOf<TrackEntity?>(null) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.scanLocalMusic()
        } else {
            Toast.makeText(context, "Permission needed to scan local music", Toast.LENGTH_SHORT).show()
        }
    }

    if (trackToDelete != null) {
        AlertDialog(
            onDismissRequest = { trackToDelete = null },
            title = { Text("Delete Track") },
            text = { Text("Are you sure you want to delete '${trackToDelete?.title}' from your library?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        trackToDelete?.let { viewModel.deleteTrack(it) }
                        trackToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { trackToDelete = null }) {
                    Text("Cancel")
                }
            },
            containerColor = Surface,
            titleContentColor = Color.White,
            textContentColor = Color.Gray
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F1A))
    ) {
        // App Bar
        CenterAlignedTopAppBar(
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Library", color = Color.White)
                    if (isScanning) {
                        Text(
                            text = "Scanning...",
                            style = MaterialTheme.typography.labelSmall,
                            color = Primary
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color(0xFF0F0F1A)
            ),
            actions = {
                // Scan Button
                IconButton(onClick = {
                    val permission = if (Build.VERSION.SDK_INT >= 33) {
                        Manifest.permission.READ_MEDIA_AUDIO
                    } else {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    }
                    permissionLauncher.launch(permission)
                }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Scan Local", tint = Color.White)
                }

                if (tracks.isNotEmpty()) {
                    IconButton(onClick = { 
                        viewModel.playAll() 
                        onNavigateToPlayer()
                    }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play All", tint = Primary)
                    }
                }
            }
        )

        if (tracks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No tracks yet. Go to Home to download.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 100.dp) // Space for bottom nav
            ) {
                items(tracks) { track ->
                    TrackItem(
                        track = track,
                        onTap = { 
                            viewModel.playTrack(track)
                            onNavigateToPlayer()
                        },
                        onDelete = { trackToDelete = track }
                    )
                }
            }
        }
    }
}

@Composable
fun TrackItem(
    track: TrackEntity, 
    onTap: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTap)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail
        AsyncImage(
            model = track.thumbnailPath ?: "", // Fallback if needed
            contentDescription = null,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Surface),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = track.artist ?: "Unknown Artist",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                maxLines = 1
            )
        }

        // More options
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.Gray)
            }
            
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(Surface)
            ) {
                DropdownMenuItem(
                    text = { Text("Delete", color = Color.Red) },
                    onClick = {
                        showMenu = false
                        onDelete()
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                    }
                )
            }
        }
    }
}
