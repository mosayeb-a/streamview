package com.ma.streamview.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ma.streamview.common.components.material.padding
import com.ma.streamview.common.components.DraggableTabRow
import com.ma.streamview.common.components.TOOLBAR_SIZE
import com.ma.streamview.common.components.screens.EmptyScreen
import com.ma.streamview.common.components.screens.LoadingScreen
import com.ma.streamview.feature.following.UserItem
import com.ma.streamview.feature.profile.CategoryVerticalListItem
import com.ma.streamview.feature.profile.VerticalListItem


@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onVideoClick: (videoId: String, videoThumbUrl: String, slugName: String, channelLogo: String, userId: String, userName: String, description: String, tags: List<String>) -> Unit,
    onStreamClick: (id: String, url: String, slugName: String, channelLogo: String, userId: String, userName: String, description: String, tags: List<String>?) -> Unit,
    onUserClicked: (id: String, login: String) -> Unit,
) {

    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(modifier = Modifier.fillMaxSize()) {
        TextField(
            value = state.query,
            onValueChange = { viewModel.onQueryChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .height(TOOLBAR_SIZE)
                .background(Color.White),
            trailingIcon = {
                if (state.query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear text"
                        )
                    }
                }
            },
            singleLine = true,
            placeholder = {
                Text(
                    text = "Search",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                cursorColor = MaterialTheme.colorScheme.secondary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            )
        )
        DraggableTabRow(
            modifier = Modifier
                .fillMaxSize(),
            tabsList = listOf("Videos", "Stream", "Category", "Channel")
        ) { page: Int ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
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
                        items(
                            count = state.videos.size,
                            key = { state.videos[it].id }
                        ) {
                            val video = state.videos[it]
                            val user = video.owner
                            VerticalListItem(
                                imageUrl = video.thumb,
                                title = video.title,
                                username = user?.displayName,
                                slugName = video.game?.displayName ?: "",
                                viewCount = video.viewCount,
                                createdAt = video.createdAt,
                                onClick = {
                                    onVideoClick.invoke(
                                        video.id,
                                        video.previewThumbnailURL,
                                        video.game?.slug ?: "",
                                        video.owner?.profileImageURL.toString(),
                                        video.owner?.id.toString(),
                                        video.owner?.login.toString(),
                                        video.title,
                                        video.contentTags ?: emptyList()
                                    )
                                }
                            )
                        }
                        if (state.videos.isEmpty() && !state.isLoading) {
                            item {
                                EmptyScreen(
                                    modifier = Modifier
                                        .padding(top = MaterialTheme.padding.extraLarge),
                                    message = "you haven't find any videos yet!",
                                )
                            }
                        }
                    }

                    1 -> {
                        items(
                            count = state.streams.size,
                            key = { state.streams[it].streamNode.id }
                        ) {
                            val stream = state.streams[it].streamNode
                            val user = stream.broadcaster
                            VerticalListItem(
                                imageUrl = stream.preview,
                                title = stream.broadcaster.broadcastSettings.title,
                                username = user.displayName.toString(),
                                slugName = stream.category?.displayName ?: "",
                                viewCount = stream.viewersCount,
                                createdAt = stream.createdAt,
                                onClick = {
                                    onStreamClick(
                                        stream.id,
                                        stream.preview,
                                        stream.category?.slug.toString(),
                                        stream.broadcaster.profileImageURL.toString(),
                                        stream.broadcaster.id.toString(),
                                        stream.broadcaster.login.toString(),
                                        stream.broadcaster.broadcastSettings.title,
                                        stream.freeformTags?.map { it.name }
                                    )
                                }
                            )
                            println("searchViewModel: createat" + stream.createdAt)
                        }
                        if (state.streams.isEmpty() && !state.isLoading) {
                            item {
                                EmptyScreen(
                                    modifier = Modifier
                                        .padding(top = MaterialTheme.padding.extraLarge),
                                    message = "you haven't find any streams yet!",
                                )
                            }
                        }
                    }

                    2 -> {
                        items(
                            count = state.categories.size,
                            key = { state.categories[it].categoryNode.id }
                        ) {
                            val category = state.categories[it].categoryNode
                            CategoryVerticalListItem(
                                imageUrl = category.boxArtURLPreview,
                                title = category.displayName,
                                viewersCount = category.viewersCount ?: 0,
                                onClick = {
                                    // todo : navigate to the list screen
                                }
                            )
                        }
                        if (state.categories.isEmpty() && !state.isLoading) {
                            item {
                                EmptyScreen(
                                    modifier = Modifier
                                        .padding(top = MaterialTheme.padding.extraLarge),
                                    message = "you haven't find any category yet!",
                                )
                            }
                        }
                    }

                    3 -> {
                        items(
                            count = state.channels.size,
                            key = { state.channels[it].node.id }
                        ) {
                            val user = state.channels[it].node
                            UserItem(
                                modifier = Modifier,
                                profilePic = user.profileImageURL,
                                onClick = { onUserClicked.invoke(user.id, user.login) },
                                username = user.displayName,
                                caption = "followers: ${user.followers}",
                            )
                        }
                        if (state.channels.isEmpty() && !state.isLoading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .padding(top = MaterialTheme.padding.extraLarge),
                                ) {
                                    EmptyScreen(message = "you haven't find any channel yet!")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
