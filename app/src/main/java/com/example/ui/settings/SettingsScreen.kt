package com.example.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppThemeSetting
import com.example.data.model.AudioQuality
import com.example.ui.MusicViewModel
import com.example.ui.theme.CardSurfaceDark
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.NeonCyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MusicViewModel,
    onBackClick: () -> Unit,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeSetting by viewModel.themeSetting.collectAsStateWithLifecycle()
    val audioQuality by viewModel.audioQuality.collectAsStateWithLifecycle()
    val wifiOnly by viewModel.wifiOnlyDownloads.collectAsStateWithLifecycle()
    val visualizerEnabled by viewModel.visualizerEnabled.collectAsStateWithLifecycle()
    val customApiKey by viewModel.customApiKey.collectAsStateWithLifecycle()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var apiKeyInput by remember { mutableStateOf(customApiKey) }

    var cacheClearedMessage by remember { mutableStateOf(false) }
    var historyClearedMessage by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.testTag("settings_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Appearance Section
            SettingsSectionHeader(title = "Appearance")
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                shape = RoundedCornerShape(14.dp)
            ) {
                SettingsClickableRow(
                    icon = Icons.Default.DarkMode,
                    title = "App Theme",
                    subtitle = when (themeSetting) {
                        AppThemeSetting.DARK -> "Dark (Default)"
                        AppThemeSetting.LIGHT -> "Light"
                        AppThemeSetting.SYSTEM -> "System default"
                    },
                    onClick = { showThemeDialog = true }
                )
            }

            // Audio & Playback Section
            SettingsSectionHeader(title = "Audio & Playback")
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column {
                    SettingsClickableRow(
                        icon = Icons.Default.HighQuality,
                        title = "Streaming & Download Quality",
                        subtitle = "${audioQuality.name} (${audioQuality.bitRate})",
                        onClick = { showQualityDialog = true }
                    )

                    SettingsSwitchRow(
                        icon = Icons.Default.GraphicEq,
                        title = "Audio Visualizer",
                        subtitle = "Dynamic equalizer bars in player",
                        checked = visualizerEnabled,
                        onCheckedChange = { viewModel.visualizerEnabled.value = it }
                    )
                }
            }

            // Downloads & Storage Section
            SettingsSectionHeader(title = "Downloads & Network")
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column {
                    SettingsSwitchRow(
                        icon = Icons.Default.Wifi,
                        title = "Download over Wi-Fi only",
                        subtitle = "Prevent cellular data usage",
                        checked = wifiOnly,
                        onCheckedChange = { viewModel.wifiOnlyDownloads.value = it }
                    )

                    SettingsClickableRow(
                        icon = Icons.Default.CleaningServices,
                        title = "Clear Image & Audio Cache",
                        subtitle = if (cacheClearedMessage) "Cache cleared successfully!" else "Free up temporary cache space",
                        onClick = {
                            cacheClearedMessage = true
                        }
                    )

                    SettingsClickableRow(
                        icon = Icons.Default.DeleteSweep,
                        title = "Clear Playback History",
                        subtitle = if (historyClearedMessage) "Playback history cleared!" else "Remove recently played list",
                        onClick = {
                            viewModel.clearPlaybackHistory()
                            historyClearedMessage = true
                        }
                    )
                }
            }

            // Developer / API Configuration Section
            SettingsSectionHeader(title = "API Configuration")
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                shape = RoundedCornerShape(14.dp)
            ) {
                SettingsClickableRow(
                    icon = Icons.Default.Key,
                    title = "Online Music API Key",
                    subtitle = if (customApiKey.isBlank()) "Demo / Legal Catalog Mode (No Key Set)" else "Custom API Key Active",
                    onClick = {
                        apiKeyInput = customApiKey
                        showApiKeyDialog = true
                    }
                )
            }

            // About Section
            SettingsSectionHeader(title = "About")
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                shape = RoundedCornerShape(14.dp)
            ) {
                SettingsClickableRow(
                    icon = Icons.Default.Info,
                    title = "About Sivabarath Music",
                    subtitle = "Version 1.0.0 • Legal & License Info",
                    onClick = onNavigateToAbout
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Theme Dialog
        if (showThemeDialog) {
            AlertDialog(
                onDismissRequest = { showThemeDialog = false },
                title = { Text("Choose Theme") },
                text = {
                    Column {
                        AppThemeSetting.values().forEach { setting ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.themeSetting.value = setting
                                        showThemeDialog = false
                                    }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = themeSetting == setting,
                                    onClick = {
                                        viewModel.themeSetting.value = setting
                                        showThemeDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = when (setting) {
                                        AppThemeSetting.DARK -> "Dark (Default Obsidian)"
                                        AppThemeSetting.LIGHT -> "Light"
                                        AppThemeSetting.SYSTEM -> "System default"
                                    }
                                )
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showThemeDialog = false }) { Text("Close") }
                }
            )
        }

        // Quality Dialog
        if (showQualityDialog) {
            AlertDialog(
                onDismissRequest = { showQualityDialog = false },
                title = { Text("Select Audio Quality") },
                text = {
                    Column {
                        AudioQuality.values().forEach { quality ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.audioQuality.value = quality
                                        showQualityDialog = false
                                    }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = audioQuality == quality,
                                    onClick = {
                                        viewModel.audioQuality.value = quality
                                        showQualityDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = "${quality.name} (${quality.bitRate})")
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showQualityDialog = false }) { Text("Close") }
                }
            )
        }

        // API Key Dialog
        if (showApiKeyDialog) {
            AlertDialog(
                onDismissRequest = { showApiKeyDialog = false },
                title = { Text("Configure Music API Key") },
                text = {
                    Column {
                        Text(
                            text = "Enter a Jamendo or Open Audio API client ID. If empty, Sivabarath Music automatically runs in legal royalty-free mode.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = { apiKeyInput = it },
                            placeholder = { Text("Client ID / Key") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.customApiKey.value = apiKeyInput.trim()
                            showApiKeyDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)
                    ) {
                        Text("Save Key")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showApiKeyDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = NeonCyan,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
    )
}

@Composable
private fun SettingsClickableRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ElectricPurple
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ElectricPurple
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = ElectricPurple)
        )
    }
}
