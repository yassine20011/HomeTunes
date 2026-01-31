package com.hometunes.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.hometunes.app.player.PlayerManager
import com.hometunes.app.ui.MainScreen
import com.hometunes.app.ui.theme.HomeTunesTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var playerManager: PlayerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize player
        playerManager.initialize()

        setContent { HomeTunesTheme { MainScreen() } }
    }

    override fun onDestroy() {
        super.onDestroy()
        playerManager.release()
    }
}
