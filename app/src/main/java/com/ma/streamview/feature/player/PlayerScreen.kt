package com.ma.streamview.feature.player

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.ma.streamview.common.components.material.padding
import com.ma.streamview.common.components.screens.LoadingScreen
import com.ma.streamview.feature.home.UserItem


@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    viewModel: PlayerViewmodel,
    profilePic: String,
    userId: String,
    username: String,
    categoryName: String,
    tags: List<String>? = null,
    title: String,
    onUserClicked: () -> Unit,
    isStream: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val state by viewModel.state.collectAsStateWithLifecycle()

        val context = LocalContext.current
        val myPlayerView = remember {
            PlayerView(context).also {
                it.useController = false
                it.player = viewModel.player
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            StreamVideoPlayer(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(.28f),
                playerView = myPlayerView,
                onPlayPauseClick = {
                    if (viewModel.isPlaying()) {
                        viewModel.pause()
                    } else {
                        viewModel.play()
                    }
                },
                currentPosition = state.currentPosition,
                onPositionChanged = {
                    viewModel.seekTo(it.toLong())
                },
                totalPosition = state.totalPosition,
                updatePositions = {
                    viewModel.updatePositions()
                },
                onBackwardClick = {
                    viewModel.seekBackward()
                },
                onForwardClick = {
                    viewModel.seekForward()
                },
                isPlaying = viewModel.isPlaying(),
                isSubOnly = state.isSubOnly,
                isLoading = state.isLoading,
                isLive = isStream
            )
            println("islive: $isStream")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .padding(horizontal = MaterialTheme.padding.medium)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    UserItem(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .weight(1f),
                        profilePictureUrl = profilePic,
                        username = username,
                        title = title,
                        categoryName = categoryName,
                        tags = tags,
                        onClick = {
                            onUserClicked.invoke()
                            viewModel.pause()
                        },
                    )
                    ToggleableButton(
                        modifier = Modifier
                            .align(Alignment.CenterVertically),
                        text = if (!state.isFollowed) "follow" else "unfollow",
                        selected = state.isFollowed
                    ) {
                        if (!state.isFollowed) {
                            viewModel.followChannel(
                                com.ma.streamview.data.repo.source.Channel(
                                    id = userId,
                                    userName = username,
                                    userLogin = username,
                                    channelLogo = profilePic
                                )
                            )
                        } else {
                            viewModel.unfollowChannel(userId)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ToggleableButton(
    modifier: Modifier = Modifier,
    text: String,
    selected: Boolean,
    onClick: () -> Unit = {},
) {
    var filled by remember { mutableStateOf(!selected) }
    val containerColor by animateColorAsState(
        targetValue = if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
        animationSpec = tween(durationMillis = 200), label = "containerColor"
    )
    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = containerColor,
        contentColor = if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary
    )
    Button(
        modifier = modifier,
        onClick = {
            onClick.invoke()
            filled = !filled
        },
        shape = MaterialTheme.shapes.medium,
        colors = buttonColors,
        border = if (filled) {
            null
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        },
        contentPadding = PaddingValues(MaterialTheme.padding.small)
    ) {
        AnimatedText(targetText = text) {
            Text(
                text = it,
                color = if (filled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun AnimatedText(targetText: String, content: @Composable (text: String) -> Unit) {
    AnimatedContent(
        targetState = targetText,
        transitionSpec = {
            slideInVertically { height -> height } + fadeIn() togetherWith
                    slideOutVertically { height -> -height } + fadeOut()
        }, label = "currentMessageLabel"
    ) { text ->
        content.invoke(text)
    }
}
