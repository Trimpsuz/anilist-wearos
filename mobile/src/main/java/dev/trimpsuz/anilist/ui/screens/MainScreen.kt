package dev.trimpsuz.anilist.ui.screens

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.trimpsuz.anilist.type.MediaListStatus
import dev.trimpsuz.anilist.ui.viewModels.LoggedInViewModel
import dev.trimpsuz.anilist.ui.viewModels.MediaListViewModel
import dev.trimpsuz.anilist.ui.viewModels.MediaStatusViewModel
import dev.trimpsuz.anilist.ui.viewModels.ViewerViewModel
import dev.trimpsuz.anilist.utils.NavigationItem
import dev.trimpsuz.anilist.utils.firstBlocking
import dev.trimpsuz.anilist.utils.navigateOrPopBackStack
import kotlinx.coroutines.launch

private val items = listOf(
    NavigationItem("Anime", Icons.Outlined.Tv, "anime"),
    NavigationItem("Manga", Icons.Outlined.Book, "manga"),
    NavigationItem("Settings", Icons.Outlined.Settings, "settings")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    loggedInViewModel: LoggedInViewModel = hiltViewModel(),
    viewerViewModel: ViewerViewModel = hiltViewModel(),
    animeListViewModel: MediaListViewModel = hiltViewModel(key = "anime"),
    mangaListViewModel: MediaListViewModel = hiltViewModel(key = "manga"),
    mediaStatusViewModel: MediaStatusViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val selectedMediaIds = loggedInViewModel.selectedMediaIds.firstBlocking()

    val animeLazyGridState = rememberLazyGridState()
    val mangaLazyGridState = rememberLazyGridState()

    LaunchedEffect(Unit) {
        if(selectedMediaIds.isNotEmpty()) {
            val filteredSelectedMediaIds = selectedMediaIds.let { ids ->
                val statusMap = mediaStatusViewModel.fetchMediaStatuses(ids)
                ids.filter { mediaId ->
                    statusMap[mediaId] == MediaListStatus.CURRENT
                }.toSet()
            }

            if(filteredSelectedMediaIds != selectedMediaIds) loggedInViewModel.setSelectedMediaIds(filteredSelectedMediaIds)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    text = items.firstOrNull { it.route == currentRoute }?.title ?: ""
                ) }
            )
        },
        bottomBar = { BottomNavigationBar(navController, animeLazyGridState, mangaLazyGridState) },
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        NavHost(navController, startDestination = "anime",
            modifier = Modifier.padding(padding).fillMaxSize(),
            enterTransition = { fadeIn(animationSpec = tween(durationMillis = 250)) },
            exitTransition = { fadeOut(animationSpec = tween(durationMillis = 250)) }
        ) {
            composable("login") { LoginScreen(navController) }
            composable("anime") { AnimeScreen(viewerViewModel, animeListViewModel, loggedInViewModel, animeLazyGridState) }
            composable("manga") { MangaScreen(viewerViewModel, mangaListViewModel, loggedInViewModel, mangaLazyGridState) }
            composable("settings") { SettingsScreen(navController) }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController, animeLazyGridState: LazyGridState, mangaLazyGridState: LazyGridState) {
    NavigationBar {
        val currentBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = currentBackStackEntry?.destination?.route
        val scope = rememberCoroutineScope()

        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if(currentRoute == item.route && currentRoute == "anime") {
                        scope.launch {
                            animeLazyGridState.scrollToItem(0)
                        }
                    } else if (currentRoute == item.route && currentRoute == "manga") {
                        scope.launch {
                            mangaLazyGridState.scrollToItem(0)
                        }
                    } else {
                        navController.navigateOrPopBackStack(item.route)
                    }
                },
                label = { Text(item.title, textAlign = TextAlign.Center) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                }
            )
        }
    }
}
