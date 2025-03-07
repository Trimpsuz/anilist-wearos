package dev.trimpsuz.anilist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.trimpsuz.anilist.ui.screens.LoginScreen
import dev.trimpsuz.anilist.ui.screens.MainScreen
import dev.trimpsuz.anilist.ui.screens.SettingsScreen
import dev.trimpsuz.anilist.ui.theme.AnilistWearOSTheme
import dev.trimpsuz.anilist.ui.viewModels.MainViewModel
import dev.trimpsuz.anilist.utils.firstBlocking

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    private fun handleDeepLink(intent: Intent) {
        val data: Uri? = intent.data
        if (data != null && data.scheme == "anilistwearos" && data.host == "auth-callback") {
            val accessToken = "access_token=([^&]+)".toRegex().find(data.fragment.toString())?.groups?.get(1)?.value
            if(accessToken != null) {
                viewModel.saveToken(accessToken, applicationContext)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        handleDeepLink(intent)

        viewModel.setToken(viewModel.accessToken.firstBlocking())
        val initialIsLoggedIn = viewModel.isLoggedIn.firstBlocking()
        val initialTheme = viewModel.theme.firstBlocking()

        setContent {
            val theme by viewModel.theme.collectAsStateWithLifecycle(initialTheme)
            val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle(initialIsLoggedIn)

            val startDestination = if (isLoggedIn) "main" else "login"

            AnilistWearOSTheme(
                darkTheme = when (theme) {
                    "Dark" -> true
                    "Light" -> false
                    else -> isSystemInDarkTheme()
                }
            ) {
                val navController = rememberNavController()

                NavHost(
                    navController,
                    startDestination = startDestination,
                    modifier = Modifier.padding(
                        start = PaddingValues().calculateStartPadding(LocalLayoutDirection.current),
                        top = PaddingValues().calculateTopPadding(),
                        end = PaddingValues().calculateEndPadding(LocalLayoutDirection.current),
                    ),
                    enterTransition = {
                        if(initialState.destination.route == navController.currentDestination?.route) EnterTransition.None else {
                            fadeIn() +
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            )
                        }
                    },
                    exitTransition = {
                        if (initialState.destination.route == startDestination) ExitTransition.None else {
                            fadeOut() +
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.End,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            )
                        }
                    },
                    popEnterTransition = {
                        if(initialState.destination.route == navController.currentDestination?.route) EnterTransition.None else
                        fadeIn()
                    },
                    popExitTransition = {
                        if (initialState.destination.route == startDestination) ExitTransition.None else {
                            fadeOut() +
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.End,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            )
                        }
                    }
                    ) {
                    composable("login") { LoginScreen(navController) }
                    composable("settings") { SettingsScreen(navController) }
                    composable("main") { MainScreen() }
                }
            }
        }
    }
}