package com.example.ui.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.Album
import com.example.data.model.Artist
import com.example.data.model.Song
import com.example.ui.MusicViewModel
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.CreatePlaylistDialog
import com.example.ui.components.MiniPlayer
import com.example.ui.details.AlbumDetailsScreen
import com.example.ui.details.ArtistDetailsScreen
import com.example.ui.details.SongDetailsScreen
import com.example.ui.downloads.DownloadsScreen
import com.example.ui.home.HomeScreen
import com.example.ui.library.FavoritesScreen
import com.example.ui.library.LibraryScreen
import com.example.ui.library.PlaylistDetailsScreen
import com.example.ui.library.PlaylistsScreen
import com.example.ui.player.FullPlayerScreen
import com.example.ui.player.QueueScreen
import com.example.ui.search.SearchScreen
import com.example.ui.settings.AboutScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.splash.SplashScreen
import com.example.ui.theme.CardSurfaceDark
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.NeonCyan

sealed class Screen(val route: String, val title: String = "", val icon: ImageVector? = null) {
    object Splash : Screen("splash")
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Search : Screen("search", "Search", Icons.Default.Search)
    object Library : Screen("library", "Library", Icons.Default.LibraryMusic)
    object Downloads : Screen("downloads", "Downloads", Icons.Default.Download)

    object Favorites : Screen("favorites", "Favorites")
    object Playlists : Screen("playlists", "Playlists")
    object PlaylistDetails : Screen("playlist_details", "Playlist")
    object SongDetails : Screen("song_details", "Song Details")
    object ArtistDetails : Screen("artist_details", "Artist")
    object AlbumDetails : Screen("album_details", "Album")
    object FullPlayer : Screen("full_player", "Now Playing")
    object Queue : Screen("queue", "Queue")
    object Settings : Screen("settings", "Settings")
    object About : Screen("about", "About")
}

@Composable
fun MainScreen(viewModel: MusicViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val isBuffering by viewModel.isBuffering.collectAsStateWithLifecycle()
    val playbackPosition by viewModel.playbackPosition.collectAsStateWithLifecycle()
    val duration by viewModel.duration.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()

    var songForPlaylistDialog by remember { mutableStateOf<Song?>(null) }
    var showCreatePlaylistFromAdd by remember { mutableStateOf(false) }

    // Request POST_NOTIFICATIONS permission on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Permission result handled */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val bottomNavScreens = listOf(Screen.Home, Screen.Search, Screen.Library, Screen.Downloads)
    val showBottomBar = currentRoute in bottomNavScreens.map { it.route }
    val showMiniPlayer = currentSong != null && currentRoute != Screen.FullPlayer.route && currentRoute != Screen.Splash.route

    Scaffold(
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Mini Player docked above navigation bar
                AnimatedVisibility(
                    visible = showMiniPlayer,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    MiniPlayer(
                        song = currentSong,
                        isPlaying = isPlaying,
                        isBuffering = isBuffering,
                        playbackPosition = playbackPosition,
                        duration = duration,
                        onMiniPlayerClick = {
                            navController.navigate(Screen.FullPlayer.route)
                        },
                        onTogglePlayPause = {
                            viewModel.togglePlayPause()
                        },
                        onNextClick = {
                            viewModel.skipToNext()
                        },
                        onCloseClick = {
                            viewModel.stopPlayback()
                        }
                    )
                }

                // Bottom Navigation
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = CardSurfaceDark,
                        tonalElevation = 8.dp,
                        modifier = Modifier.testTag("bottom_nav_bar")
                    ) {
                        bottomNavScreens.forEach { screen ->
                            val selected = currentRoute == screen.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (currentRoute != screen.route) {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    screen.icon?.let {
                                        Icon(imageVector = it, contentDescription = screen.title)
                                    }
                                },
                                label = { Text(screen.title) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = NeonCyan,
                                    selectedTextColor = NeonCyan,
                                    indicatorColor = ElectricPurple.copy(alpha = 0.35f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag("nav_item_${screen.route}")
                            )
                        }
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets.navigationBars
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route
            ) {
                composable(Screen.Splash.route) {
                    SplashScreen(
                        onSplashFinished = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                        onSongClick = { song ->
                            viewModel.selectedSong.value = song
                            navController.navigate(Screen.SongDetails.route)
                        },
                        onArtistClick = { artist ->
                            viewModel.selectedArtist.value = artist
                            navController.navigate(Screen.ArtistDetails.route)
                        },
                        onAlbumClick = { album ->
                            viewModel.selectedAlbum.value = album
                            navController.navigate(Screen.AlbumDetails.route)
                        },
                        onPlaylistClick = { playlist ->
                            viewModel.selectedPlaylist.value = playlist
                            navController.navigate(Screen.PlaylistDetails.route)
                        },
                        onAddToPlaylist = { song ->
                            songForPlaylistDialog = song
                        }
                    )
                }

                composable(Screen.Search.route) {
                    SearchScreen(
                        viewModel = viewModel,
                        onSongClick = { song ->
                            viewModel.selectedSong.value = song
                            navController.navigate(Screen.SongDetails.route)
                        },
                        onArtistClick = { artist ->
                            viewModel.selectedArtist.value = artist
                            navController.navigate(Screen.ArtistDetails.route)
                        },
                        onAlbumClick = { album ->
                            viewModel.selectedAlbum.value = album
                            navController.navigate(Screen.AlbumDetails.route)
                        },
                        onAddToPlaylist = { song ->
                            songForPlaylistDialog = song
                        }
                    )
                }

                composable(Screen.Library.route) {
                    LibraryScreen(
                        viewModel = viewModel,
                        onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                        onNavigateToPlaylists = { navController.navigate(Screen.Playlists.route) },
                        onNavigateToDownloads = { navController.navigate(Screen.Downloads.route) },
                        onPlaylistClick = { playlist ->
                            viewModel.selectedPlaylist.value = playlist
                            navController.navigate(Screen.PlaylistDetails.route)
                        }
                    )
                }

                composable(Screen.Downloads.route) {
                    DownloadsScreen(
                        viewModel = viewModel,
                        onBackClick = { navController.popBackStack() },
                        onSongClick = { song ->
                            viewModel.selectedSong.value = song
                            navController.navigate(Screen.SongDetails.route)
                        },
                        onAddToPlaylist = { song ->
                            songForPlaylistDialog = song
                        }
                    )
                }

                composable(Screen.Favorites.route) {
                    FavoritesScreen(
                        viewModel = viewModel,
                        onBackClick = { navController.popBackStack() },
                        onSongClick = { song ->
                            viewModel.selectedSong.value = song
                            navController.navigate(Screen.SongDetails.route)
                        },
                        onAddToPlaylist = { song ->
                            songForPlaylistDialog = song
                        }
                    )
                }

                composable(Screen.Playlists.route) {
                    PlaylistsScreen(
                        viewModel = viewModel,
                        onBackClick = { navController.popBackStack() },
                        onPlaylistClick = { playlist ->
                            viewModel.selectedPlaylist.value = playlist
                            navController.navigate(Screen.PlaylistDetails.route)
                        }
                    )
                }

                composable(Screen.PlaylistDetails.route) {
                    PlaylistDetailsScreen(
                        viewModel = viewModel,
                        onBackClick = { navController.popBackStack() },
                        onSongClick = { song ->
                            viewModel.selectedSong.value = song
                            navController.navigate(Screen.SongDetails.route)
                        },
                        onAddToPlaylist = { song ->
                            songForPlaylistDialog = song
                        }
                    )
                }

                composable(Screen.SongDetails.route) {
                    SongDetailsScreen(
                        viewModel = viewModel,
                        onBackClick = { navController.popBackStack() },
                        onAddToPlaylist = {
                            viewModel.selectedSong.value?.let { songForPlaylistDialog = it }
                        }
                    )
                }

                composable(Screen.ArtistDetails.route) {
                    ArtistDetailsScreen(
                        viewModel = viewModel,
                        onBackClick = { navController.popBackStack() },
                        onSongClick = { song ->
                            viewModel.selectedSong.value = song
                            navController.navigate(Screen.SongDetails.route)
                        },
                        onAddToPlaylist = { song ->
                            songForPlaylistDialog = song
                        }
                    )
                }

                composable(Screen.AlbumDetails.route) {
                    AlbumDetailsScreen(
                        viewModel = viewModel,
                        onBackClick = { navController.popBackStack() },
                        onSongClick = { song ->
                            viewModel.selectedSong.value = song
                            navController.navigate(Screen.SongDetails.route)
                        },
                        onAddToPlaylist = { song ->
                            songForPlaylistDialog = song
                        }
                    )
                }

                composable(Screen.FullPlayer.route) {
                    FullPlayerScreen(
                        viewModel = viewModel,
                        onBackClick = { navController.popBackStack() },
                        onQueueClick = { navController.navigate(Screen.Queue.route) },
                        onAddToPlaylistClick = {
                            viewModel.currentSong.value?.let { songForPlaylistDialog = it }
                        }
                    )
                }

                composable(Screen.Queue.route) {
                    QueueScreen(
                        viewModel = viewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        viewModel = viewModel,
                        onBackClick = { navController.popBackStack() },
                        onNavigateToAbout = { navController.navigate(Screen.About.route) }
                    )
                }

                composable(Screen.About.route) {
                    AboutScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }

    // Add To Playlist Dialog
    songForPlaylistDialog?.let { song ->
        AddToPlaylistDialog(
            song = song,
            playlists = playlists,
            onDismiss = { songForPlaylistDialog = null },
            onPlaylistSelected = { playlistId ->
                viewModel.addSongToPlaylist(playlistId, song)
                songForPlaylistDialog = null
            },
            onCreateNewPlaylistClick = {
                showCreatePlaylistFromAdd = true
            }
        )
    }

    // Create Playlist Dialog when triggered from Add Dialog
    if (showCreatePlaylistFromAdd && songForPlaylistDialog != null) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistFromAdd = false },
            onCreate = { name, desc ->
                showCreatePlaylistFromAdd = false
                viewModel.createPlaylist(name, desc)
            }
        )
    }
}
