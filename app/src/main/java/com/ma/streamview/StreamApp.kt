package com.ma.streamview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.ma.streamview.feature.following.navigation.followingScreen
import com.ma.streamview.feature.home.navigation.homeScreen
import com.ma.streamview.feature.player.navigation.playerScreen
import com.ma.streamview.feature.profile.navigation.profileScreen
import com.ma.streamview.feature.search.navigation.searchScreen

@Composable
fun StreamApp(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    isOnline: Boolean,
) {
//    val context = LocalContext.current
//    var retryTriggered by remember { mutableStateOf(false) }
//    var showOfflineMessage by remember { mutableStateOf(false) }
//    var retrying by remember { mutableStateOf(false) }

//    LaunchedEffect(retryTriggered) {
//        if (retryTriggered) {
//            retrying = true
//            if (context.retryConnectionCheck()) {
//                navController.navigate(Route.HOME) {
//                    popUpTo(0)
//                }
//            } else {
//                showOfflineMessage = true
//            }
//            retrying = false
//            retryTriggered = false
//        }
//    }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = !isOnline,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(MaterialTheme.colorScheme.error)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No Internet Connection",
                    fontSize = 14.sp,
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        NavHost(
            modifier = Modifier.fillMaxSize(),
            navController = navController,
            startDestination = Route.HOME,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            homeScreen(navController)
            playerScreen(navController)
            profileScreen(navController)
            followingScreen(navController)
            searchScreen(navController)
        }
    }
}


object Route {
    const val HOME = "Home"
    const val FOLLOWING = "Following"
    const val SEARCH = "Explore"
    const val PLAYER = "player"
    const val PROFILE = "profile"
    const val EMPTY_SCREEN = "empty_screen"
}