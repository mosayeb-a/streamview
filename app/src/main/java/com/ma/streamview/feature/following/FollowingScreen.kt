package com.ma.streamview.feature.following

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ma.streamview.common.animatedScaleOnTouch
import com.ma.streamview.common.components.DraggableTabRow
import com.ma.streamview.common.components.material.padding
import com.ma.streamview.common.components.screens.EmptyScreen
import com.ma.streamview.common.components.screens.EmptyScreenAction
import com.ma.streamview.common.components.screens.LoadingScreen
import com.ma.streamview.feature.home.StreamImage
import com.ma.streamview.feature.profile.VerticalListItem
import com.ma.streamview.feature.search.navigation.navigateToSearch


@Composable
fun FollowingScreen(
    viewModel: FollowingViewModel,
    onUserClicked: (id: String, login: String) -> Unit,
    onStreamClick: (id: String, url: String, slugName: String, channelLogo: String, userId: String, userName: String, description: String, tags: List<String>?) -> Unit,
    navController: NavController
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                println("FollowingScreen resumed")
                viewModel.reload()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DraggableTabRow(
        modifier = Modifier
            .fillMaxSize(),
        tabsList = listOf("Live", "Channels")
    ) { page: Int ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            item(key = state.isLoading) {
                LoadingScreen(
                    modifier = Modifier
                        .padding(top = MaterialTheme.padding.extraLarge),
                    displayProgressBar = state.isLoading,
                )
            }
            when (page) {
                0 -> {
                    val userMedia = state.liveChannels
                        .filter { it.stream != null }

                    println("liveChannels " + state.liveChannels)
                    if (userMedia.isNotEmpty()) {
                        items(
                            count = userMedia.size,
                            key = { userMedia[it].stream?.id!! }
                        ) {
                            VerticalListItem(
                                imageUrl = userMedia[it].stream?.preview.toString(),
                                title = userMedia[it].stream?.broadcaster?.broadcastSettings?.title.toString(),
                                username = userMedia[it].displayName,
                                slugName = userMedia[it].stream?.category?.displayName.toString(),
                                viewCount = userMedia[it].stream?.viewersCount ?: 0,
                                createdAt = userMedia[it].stream?.createdAt.toString(),
                                onClick = {
                                    onStreamClick(
                                        userMedia[it].stream?.id.toString(),
                                        userMedia[it].stream?.previewImageURL.toString(),
                                        userMedia[it].stream?.category?.slug.toString(),
                                        userMedia[it].profileImageURL.toString(),
                                        userMedia[it].id.toString(),
                                        userMedia[it].login.toString(),
                                        userMedia[it].stream?.broadcaster?.broadcastSettings?.title.toString(),
                                        userMedia[it].stream?.freeformTags?.map { it.name }
                                    )
                                }
                            )
                        }
                    }
                    if (userMedia.isEmpty() && !state.isLoading) {
                        if (state.channels.isEmpty()) {
                            item {
                                EmptyScreen(
                                    modifier = Modifier
                                        .padding(top = MaterialTheme.padding.extraLarge),
                                    message = "you haven't followed anyone yet!",
                                )
                            }
                        } else {
                            item {
                                EmptyScreen(
                                    modifier = Modifier
                                        .padding(top = MaterialTheme.padding.extraLarge),
                                    message = "there aren't any live followed channels!",
                                )
                            }
                        }
                    }
                }

                1 -> {
                    if (state.channels.isNotEmpty()) {
                        items(
                            count = state.channels.size,
                            key = { state.channels[it].id }
                        ) {
                            val channel = state.channels[it]
                            UserItem(
                                profilePic = channel.profileImageURL,
                                onClick = { onUserClicked.invoke(channel.id, channel.login) },
                                username = channel.displayName,
                                caption = channel.lastBroadcast.startedAt,
                            )
                        }
                    }
                    if (state.channels.isEmpty() && !state.isLoading) {
                        item {
                            EmptyScreen(
                                modifier = Modifier
                                    .padding(top = MaterialTheme.padding.extraLarge),
                                message = "you haven't followed anyone yet!\n explore channels",
                                action = EmptyScreenAction("explore") {
                                    navController.navigateToSearch()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun UserItem(
    modifier: Modifier = Modifier,
    profilePic: String,
    onClick: () -> Unit,
    username: String,
    caption: String,
) {
    Row(
        modifier = modifier
            .animatedScaleOnTouch { onClick.invoke() }
            .fillMaxWidth()
            .padding(
                vertical = MaterialTheme.padding.extraSmall,
                horizontal = MaterialTheme.padding.medium
            ),
    ) {
        StreamImage(
            url = profilePic,
            modifier = Modifier
                .size(64.dp)
                .clip(MaterialTheme.shapes.large),
        )
        Spacer(modifier = Modifier.width(MaterialTheme.padding.extraSmall))
        Column {
            Text(
                text = username,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                ),
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = .7f)
                )
            )
        }
    }

}