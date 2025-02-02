package com.ma.streamview

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ma.streamview.common.ObserveAsEvents
import com.ma.streamview.common.UiMessageManager
import com.ma.streamview.common.components.material.SecondrySnackbar
import com.ma.streamview.common.connectivityState
import com.ma.streamview.theme.StreamTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StreamTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                val navController = rememberNavController()
                val isOnline by connectivityState()
                val shouldShowBottomBar = shouldShowBottomBar(navController, isOnline)
                val scope = rememberCoroutineScope()
                ObserveAsEvents(
                    flow = UiMessageManager.events,
                    snackbarHostState
                ) { event ->
                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        val result = snackbarHostState.showSnackbar(
                            message = event.message,
                            actionLabel = event.action?.name,
                            duration = SnackbarDuration.Short
                        )

                        if (result == SnackbarResult.ActionPerformed) {
                            event.action?.action?.invoke()
                        }
                    }
                }
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize(),
                    snackbarHost = {
                        SecondrySnackbar(hostState = snackbarHostState)
                    },
                    bottomBar = {
                        AnimatedVisibility(
                            visible = shouldShowBottomBar,
                            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                        ) {
                            BottomNavBar(navController = navController)
                        }
                    }
                ) {
                    StreamApp(
                        navController = navController,
                        modifier = Modifier
                            .padding(bottom = if (shouldShowBottomBar) 56.dp else 0.dp)
                            .background(Color.White),
                        isOnline = isOnline
                    )
                }
            }
        }
    }
}

@Composable
fun shouldShowBottomBar(navController: NavHostController, isOnline: Boolean): Boolean {
    val currentDestination by navController.currentBackStackEntryAsState()
    return isOnline && when (currentDestination?.destination?.route?.substringBefore('/')) {
        Route.PLAYER -> false
        Route.EMPTY_SCREEN -> false
        else -> true
    }
}


//   val alpha = if (pagerState.currentPage == tabIndex) {
//                        1f - pagerState.currentPageOffsetFraction.absoluteValue
//                    } else if (pagerState.currentPage + 1 == tabIndex) {
//                        pagerState.currentPageOffsetFraction.absoluteValue
//                    } else {
//                        1f
//                    }
