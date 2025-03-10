package com.ma.streamview.feature.home

import android.util.Log
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ma.streamview.common.components.material.padding
import com.ma.streamview.common.components.screens.EmptyScreen
import com.ma.streamview.common.components.screens.EmptyScreenAction
import com.ma.streamview.common.components.screens.LoadingScreen
import com.ma.streamview.feature.search.navigation.navigateToSearch

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onVideoClick: (id: String, url: String, slugName: String, channelLogo: String, userId: String, userName: String, description: String, tags: List<String>?) -> Unit,
    onStreamClick: (id: String, url: String, slugName: String, channelLogo: String, userId: String, userName: String, description: String, tags: List<String>?) -> Unit,
    onUserClick: (id: String, login: String) -> Unit,
    navController: NavController
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                println("checkIfRecommendationReady. HomeScreen resumed")
                viewModel.checkIfRecommendationReady()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val sliderValue1 by remember { mutableStateOf(0.848f) }
    val sliderValue2 by remember { mutableStateOf(1.129f) }
    val width by remember { mutableStateOf(235.56.dp) }
    val height by remember { mutableStateOf(134.34.dp) }
    val value3 by remember { mutableStateOf(0) }


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val topLiveChannels = state.topLiveChannels
        val topLiveChannelsPreviews = topLiveChannels.map { it.streamNode.preview }
        if (topLiveChannelsPreviews.isNotEmpty()) {
            item {
                ImageSlider(
                    modifier = Modifier
                        .padding(top = 8.dp),
                    previews = topLiveChannelsPreviews,
                    value1 = sliderValue1,
                    value2 = sliderValue2,
                    height = height,
                    width = width,
                    value3 = value3,
                    paddingValues = 56.dp,
                    viewersCount = topLiveChannels.map { it.streamNode.viewersCount },
                    content = {
                        topLiveChannels[it].streamNode.freeformTags?.map { tags -> tags.name }
                            ?.let { it1 ->
                                StreamTag(
                                    modifier = Modifier.padding(
                                        start = 16.dp,
                                        top = 28.dp,
                                        end = 12.dp
                                    ),
                                    channelName = topLiveChannels[it].streamNode.broadcaster.displayName.toString(),
                                    streamTitle = topLiveChannels[it].streamNode.category?.displayName
                                        ?: "N/A",
                                    tags = it1
                                )
                            }
                    },
                    isStream = true,
                    onClickItem = {
                        onStreamClick(
                            topLiveChannels[it].streamNode.id,
                            topLiveChannels[it].streamNode.preview,
                            topLiveChannels[it].streamNode.category?.slug.toString(),
                            topLiveChannels[it].streamNode.broadcaster.profileImageURL.toString(),
                            topLiveChannels[it].streamNode.broadcaster.id.toString(),
                            topLiveChannels[it].streamNode.broadcaster.login.toString(),
                            topLiveChannels[it].streamNode.broadcaster.broadcastSettings.title,
                            topLiveChannels[it].streamNode.freeformTags?.map { it.name }
                        )
                    }
                )
            }
        }

        val categories = state.topCategories
        val previews = state.topCategories.map { it.categoryNode.boxArtURLPreview }
        if (categories.isNotEmpty()) {
            item {
                TitleLabel(primaryText = "TOP", secondaryText = "CATEGORIES")
            }
            item {
                CategoryHorizontalList(
                    modifier = Modifier.padding(top = 8.dp),
                    previews = previews,
                    onClick = {})
            }
        }

        val recommendedVideos = state.recommendedVideos
        val recommendedVideoPreviews = recommendedVideos.map { it.animatedPreviewURL }
        if (recommendedVideoPreviews.isNotEmpty()) {
            item {
                TitleLabel(primaryText = "VIDEOS")
            }
            item {
                HorizontalLazyList(
                    modifier = Modifier.padding(top = 8.dp),
                    previews = recommendedVideoPreviews,
                    viewersCount = recommendedVideos.map { it.viewCount },
                    isStream = false,
                    profilePic = recommendedVideos.map {
                        Log.i(
                            "profilePic", it.owner?.profileImageURL.toString()
                        )
                        it.owner?.profileImageURL
                    },
                    tags = recommendedVideos.map { it.contentTags },
                    usernames = recommendedVideos.map { it.owner?.displayName },
                    categoryNames = recommendedVideos.map { it.game?.displayName.toString() },
                    titles = recommendedVideos.map { it.title },
                    onUserClick = {
                        onUserClick.invoke(
                            recommendedVideos[it].owner?.id.toString(),
                            recommendedVideos[it].owner?.login.toString()
                        )
                    },
                    onPlaybackClick = {
                        onVideoClick.invoke(
                            recommendedVideos[it].id,
                            recommendedVideos[it].previewThumbnailURL,
                            recommendedVideos[it].game?.slug ?: "",
                            recommendedVideos[it].owner?.profileImageURL.toString(),
                            recommendedVideos[it].owner?.id.toString(),
                            recommendedVideos[it].owner?.login.toString(),
                            recommendedVideos[it].title,
                            recommendedVideos[it].contentTags ?: emptyList()
                        )
                    }
                )
            }
        }

        val recommendedStreams = state.recommendedStreams.map { it.streamNode }
        val recommendedStreamsPreviews = recommendedStreams.map { it.preview }
        if (recommendedStreamsPreviews.isNotEmpty()) {
            item {
                TitleLabel(primaryText = "STREAMS")
            }
            item {
                HorizontalLazyList(
                    previews = recommendedStreamsPreviews,
                    modifier = Modifier.padding(top = 8.dp),
                    viewersCount = recommendedStreams.map { it.viewersCount },
                    isStream = false,
                    profilePic = recommendedStreams.map { it.broadcaster.profileImageURL },
                    tags = recommendedStreams.map {
                        it.freeformTags?.map { it.name } ?: emptyList()
                    },
                    usernames = recommendedStreams.map { it.broadcaster.displayName },
                    categoryNames = recommendedStreams.map { it.category?.displayName.toString() },
                    titles = recommendedStreams.map { it.broadcaster.broadcastSettings.title },
                    onPlaybackClick = {
                        onStreamClick(
                            topLiveChannels[it].streamNode.id,
                            topLiveChannels[it].streamNode.preview,
                            topLiveChannels[it].streamNode.category?.slug.toString(),
                            topLiveChannels[it].streamNode.broadcaster.profileImageURL.toString(),
                            topLiveChannels[it].streamNode.broadcaster.id.toString(),
                            topLiveChannels[it].streamNode.broadcaster.login.toString(),
                            topLiveChannels[it].streamNode.broadcaster.broadcastSettings.title,
                            topLiveChannels[it].streamNode.freeformTags?.map { it.name },
                        )
                    },
                    onUserClick = {
                        onUserClick.invoke(
                            recommendedStreams[it].broadcaster.id.toString(),
                            recommendedStreams[it].broadcaster.login.toString()
                        )
                    }
                )
            }
        }

        if (viewModel.isHomeEmpty && !state.isLoading) {
            item {
                EmptyScreen(
                    message = "Your home items is empty.",
                    modifier = Modifier
                        .padding(top = 58.dp)
                        .height(400.dp),
                    navController = navController,
                    action = EmptyScreenAction(hint = "Retry", onClick = { viewModel.load() }
                    )
                )
            }
        }

        if ((state.recommendedVideos.isEmpty() || state.recommendedStreams.isEmpty())
            && !state.isLoading && !viewModel.isHomeEmpty
        ) {
            item(state.recommendedVideos) {
                EmptyScreen(
                    message = "you haven't got any recommendation.",
                    modifier = Modifier
                        .padding(top = MaterialTheme.padding.medium),
                    action = EmptyScreenAction(
                        hint = "Explore",
                        onClick = { navController.navigateToSearch() }),
                    navController = navController
                )
            }
        }

        item { Spacer(modifier = Modifier.height(MaterialTheme.padding.extraLarge)) }
    }
    LoadingScreen(displayProgressBar = state.isLoading)
}

@Composable
fun TitleLabel(
    modifier: Modifier = Modifier,
    primaryText: String,
    secondaryText: String = "WE THINK YOU'LL LIKE"
) {
    Row(
        modifier = modifier
            .padding(top = 28.dp, start = 16.dp)
            .fillMaxWidth(),

        ) {
        Text(
            text = "$primaryText ",
            style = MaterialTheme.typography.titleSmall
                .copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
        )
        Text(
            text = secondaryText,
            style = MaterialTheme.typography.titleSmall.copy(
                color = MaterialTheme.colorScheme.secondary
                    .copy(alpha = .7f), fontWeight = FontWeight.Bold
            )
        )
    }
}