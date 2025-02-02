package com.ma.streamview.feature.home.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.ma.streamview.Route
import com.ma.streamview.feature.home.HomeScreen
import com.ma.streamview.feature.home.HomeViewModel
import com.ma.streamview.feature.player.navigation.navigateToPlayer
import com.ma.streamview.feature.profile.navigation.navigateToProfile


fun NavGraphBuilder.homeScreen(
    navController: NavController
) {
    composable(
        route = Route.HOME
    ) { navBackStackEntry ->
        val viewModel: HomeViewModel = hiltViewModel(navBackStackEntry)
        HomeScreen(
            onVideoClick = { id, url, slug, logo, userId, userName, description, tags ->
                navController.navigateToPlayer(
                    playerId = id,
                    playerUrl = url,
                    slugName = slug,
                    channelLogo = logo,
                    userId = userId,
                    userName = userName,
                    title = description,
                    tags = tags,
                    isStream = false
                )
            },
            viewModel = viewModel,
//            onCategoryClick = { categoryId ->
//                navController.navigate(Route.LIST + "/$categoryId")
//            },
            navController = navController,
            onUserClick = { id: String, login: String ->
                navController.navigateToProfile(id, login)
            },
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