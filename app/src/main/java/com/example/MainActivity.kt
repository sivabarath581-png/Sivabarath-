package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppThemeSetting
import com.example.ui.MusicViewModel
import com.example.ui.navigation.MainScreen
import com.example.ui.theme.SivabarathMusicTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MusicViewModel by viewModels {
        val app = application as SivabarathMusicApp
        MusicViewModel.Factory(
            repository = app.repository,
            downloadManager = app.downloadManager,
            playerManager = app.playerManager
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeSetting by viewModel.themeSetting.collectAsStateWithLifecycle()
            val darkTheme = when (themeSetting) {
                AppThemeSetting.DARK -> true
                AppThemeSetting.LIGHT -> false
                AppThemeSetting.SYSTEM -> isSystemInDarkTheme()
            }

            SivabarathMusicTheme(darkTheme = darkTheme) {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
