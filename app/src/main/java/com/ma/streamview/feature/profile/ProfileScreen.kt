package com.ma.streamview.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ma.streamview.common.components.material.padding
import com.ma.streamview.common.components.StreamToolbar
import com.ma.streamview.common.components.screens.EmptyScreen
import com.ma.streamview.common.components.screens.LoadingScreen
import com.ma.streamview.feature.home.StreamImage
import com.ma.streamview.theme.White


@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    navController: NavController,
    onVideoClick: (id: String, url: String, slugName: String, channelLogo: String, userId: String, userName: String, description: String, tags: List<String>?) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val scrollState = rememberLazyListState()
    val alphaValue by remember {
        derivedStateOf {
            val maxScroll = 300
            val scrollOffset = scrollState.firstVisibleItemScrollOffset
            val firstVisibleIndex = scrollState.firstVisibleItemIndex

            if (firstVisibleIndex == 0) {
                when {
                    scrollOffset < maxScroll -> {
                        scrollOffset / maxScroll.toFloat()
                    }

                    else -> 1f
                }.coerceIn(0f, 1f)
            } else {
                1f
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        state.user?.let { user->
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(White),
            ) {
                item {
                    val imageTranslationY =
                        remember { derivedStateOf { (scrollState.firstVisibleItemScrollOffset / 2f) } }
                    StreamImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(175.dp)
                            .graphicsLayer { translationY = imageTranslationY.value },
                        url = user.bannerImageURL!!,
                        scale = ContentScale.FillWidth
                    )
                }
                if (state.videos.isEmpty() && !state.isLoading) {
                    item {
                        EmptyScreen(
                            modifier = Modifier
                                .padding(top = MaterialTheme.padding.extraLarge),
                            message = "this channel haven't got any videos!",
                        )
                    }
                }
                val videos = state.videos
                if (videos.isNotEmpty()) {
                    items(count = videos.size, key = { videos[it].videoNode.id }) {
                        val video = videos[it].videoNode
                        print("videoNodes: $video")
                        VerticalListItem(
                            modifier = Modifier
                                .background(White),
                            imageUrl = video.thumb,
                            title = video.title,
                            shouldShowUsername = false,
                            slugName = video.game?.displayName.toString(),
                            viewCount = video.viewCount,
                            createdAt = video.createdAt,
                            onClick = {
                                onVideoClick.invoke(
                                    video.id,
                                    video.previewThumbnailURL,
                                    video.game?.slug ?: "",
                                    user.profileImageURL.toString(),
                                    user.id.toString(),
                                    user.displayName?.lowercase().toString(),
                                    video.title,
                                    video.contentTags ?: emptyList()
                                )
                            }
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
            StreamToolbar(
                alpha = alphaValue,
                navController = navController,
                shouldShowTitle = true,
                title = user.displayName,
                followerCount = user.followers?.totalCount,
                shouldSearch = true,
                shouldBack = true,
            )
            LoadingScreen(displayProgressBar = state.isLoading)
        }
    }
}
