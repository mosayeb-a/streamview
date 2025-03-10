package com.ma.streamview.feature.profile.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ma.streamview.Route
import com.ma.streamview.feature.player.navigation.USER_ID
import com.ma.streamview.feature.player.navigation.navigateToPlayer
import com.ma.streamview.feature.profile.ProfileScreen
import com.ma.streamview.feature.profile.ProfileViewModel

const val LOGIN = "login"
fun NavController.navigateToProfile(id: String, login: String) {
    this.navigate("${Route.PROFILE}/$id/$login")
}

fun NavGraphBuilder.profileScreen(
    navController: NavController
) {
    composable(
        route = "${Route.PROFILE}/{$USER_ID}/{$LOGIN}",
        arguments = listOf(
            navArgument(USER_ID) { type = NavType.StringType },
            navArgument(LOGIN) { type = NavType.StringType }
        )
    ) { navBackStackEntry ->
        val viewModel: ProfileViewModel = hiltViewModel(navBackStackEntry)
        ProfileScreen(
            viewModel = viewModel,
            onVideoClick = { id, url, slug, logo, userId, userName, description, tags ->
                println("logoProf: $logo")
                navController.navigateToPlayer(
                    playerId = id,
                    playerUrl = url,
                    slugName = slug,
                    channelLogo = logo,
                    userId = userId,
                    userName = userName,
                    title = description,
                    tags = tags,
                    false
                )
            },
            navController = navController
        )
    }
}