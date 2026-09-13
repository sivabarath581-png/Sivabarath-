package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MusicViewModel
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.NeonCyan

/**
 * A Material 3 Search Bar component designed for Sivabarath Music.
 * Features:
 * - Material 3 pill container styling with dynamic surface elevation.
 * - Reactive search integration with Retrofit network operations.
 * - Indeterminate CircularProgressIndicator during active Retrofit track searches.
 * - One-tap clear button with animations.
 * - Voice search action trigger.
 * - Minimum 48dp accessible touch targets.
 */
@Composable
fun MusicSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search songs, artists, albums...",
    isLoading: Boolean = false,
    onClearClick: () -> Unit = { onQueryChange("") },
    onVoiceSearchClick: (() -> Unit)? = null,
    onSearch: (String) -> Unit = {},
    leadingIcon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("m3_music_search_bar"),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_text_input"),
            enabled = enabled,
            singleLine = true,
            interactionSource = interactionSource,
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = leadingIcon ?: {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search icon",
                    tint = NeonCyan,
                    modifier = Modifier.size(24.dp)
                )
            },
            trailingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    // Loading indicator while Retrofit searches
                    AnimatedVisibility(
                        visible = isLoading,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(20.dp)
                                .testTag("search_progress_indicator"),
                            strokeWidth = 2.dp,
                            color = NeonCyan
                        )
                    }

                    // Clear button
                    AnimatedVisibility(
                        visible = query.isNotEmpty(),
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        IconButton(
                            onClick = {
                                onClearClick()
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("clear_search_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search text",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Voice search button
                    if (onVoiceSearchClick != null) {
                        IconButton(
                            onClick = onVoiceSearchClick,
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("voice_search_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice search",
                                tint = ElectricPurple
                            )
                        }
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    onSearch(query)
                }
            ),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardSurfaceElevated,
                unfocusedContainerColor = CardSurfaceElevated.copy(alpha = 0.85f),
                disabledContainerColor = CardSurfaceElevated.copy(alpha = 0.5f),
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = NeonCyan
            )
        )
    }
}

/**
 * ViewModel-integrated overload of [MusicSearchBar].
 * Automatically subscribes to query state, loading indicators, and submits searches.
 */
@Composable
fun MusicSearchBar(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier,
    placeholder: String = "Search songs, artists, albums...",
    onVoiceSearchClick: (() -> Unit)? = null,
    onSearch: ((String) -> Unit)? = null
) {
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()

    MusicSearchBar(
        query = query,
        onQueryChange = { viewModel.onSearchQueryChanged(it) },
        modifier = modifier,
        placeholder = placeholder,
        isLoading = isSearching,
        onClearClick = { viewModel.clearSearchQuery() },
        onVoiceSearchClick = onVoiceSearchClick,
        onSearch = { submittedQuery ->
            viewModel.searchTracks(submittedQuery)
            onSearch?.invoke(submittedQuery)
        }
    )
}
