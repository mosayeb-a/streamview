package com.ma.streamview.feature.following.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.ma.streamview.Route
import com.ma.streamview.feature.following.FollowingScreen
import com.ma.streamview.feature.following.FollowingViewModel
import com.ma.streamview.feature.player.navigation.navigateToPlayer
import com.ma.streamview.feature.profile.navigation.navigateToProfile

fun NavGraphBuilder.followingScreen(
    navController: NavController
) {
    composable(
        route = Route.FOLLOWING
    ) { navBackStackEntry ->
        val viewModel: FollowingViewModel = hiltViewModel(navBackStackEntry)
        FollowingScreen(
            viewModel = viewModel,
            onUserClicked = { id,login ->
                navController.navigateToProfile(id,login)
            },
            navController = navController,
            onStreamClick = { id, url,slug, logo, userId, userName, description, tags ->
                navController.navigateToPlayer(
                    playerId = id,
                    playerUrl = url,
                    slugName = slug,
                    channelLogo = logo,
                    userId = userId,
                    userName = userName,
                    title = description,
                    tags = tags,
                    isStream = true
                )
            },
        )
    }
}