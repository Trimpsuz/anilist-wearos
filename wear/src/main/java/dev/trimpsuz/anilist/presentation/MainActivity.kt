package dev.trimpsuz.anilist.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.android.horologist.compose.layout.AppScaffold
import dagger.hilt.android.AndroidEntryPoint
import dev.trimpsuz.anilist.presentation.screens.AnimeScreen
import dev.trimpsuz.anilist.presentation.theme.AnilistWearOSTheme
import dev.trimpsuz.anilist.presentation.viewModels.MainViewModel
import dev.trimpsuz.anilist.utils.firstBlocking

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        viewModel.setToken(viewModel.accessToken.firstBlocking())
        val initialIsLoggedIn = viewModel.isLoggedIn.firstBlocking()

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle(initialIsLoggedIn)

            val startDestination = if (isLoggedIn) "anime" else "home"

            AnilistWearOSTheme {
                AppScaffold(timeText = { TimeText() }) {
                    val navController = rememberSwipeDismissableNavController()

                    SwipeDismissableNavHost(
                        startDestination = startDestination,
                        navController = navController
                    ) {
                        composable("home") {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Please log in in the companion app.",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        composable("anime") { AnimeScreen() }
                    }
                }
            }
        }
    }
}