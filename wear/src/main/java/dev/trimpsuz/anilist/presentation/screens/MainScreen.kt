package dev.trimpsuz.anilist.presentation.screens

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import dev.trimpsuz.anilist.presentation.composables.ListItem
import dev.trimpsuz.anilist.presentation.viewModels.MediaListViewModel
import dev.trimpsuz.anilist.presentation.viewModels.ViewerViewModel
import dev.trimpsuz.anilist.type.MediaListStatus
import dev.trimpsuz.anilist.type.MediaType

@OptIn(ExperimentalHorologistApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewerViewModel: ViewerViewModel = hiltViewModel(),
    animeListViewModel: MediaListViewModel = hiltViewModel(key = "anime"),
    mangaListViewModel: MediaListViewModel = hiltViewModel(key = "manga"),
) {
    val columnState = rememberResponsiveColumnState()

    val pullToRefreshState = rememberPullToRefreshState()

    var selected by remember { mutableStateOf("anime") }

    val viewer by viewerViewModel.viewer.collectAsState()
    val isViewerLoading by viewerViewModel.isLoading.collectAsState()

    val isAnimeLoading by animeListViewModel.isLoading.collectAsState()
    val isMangaLoading by mangaListViewModel.isLoading.collectAsState()

    val animeHasNextChunk by animeListViewModel.hasNextChunk.collectAsState()
    val animeIsFetchingMore by animeListViewModel.isFetchingMore.collectAsState()
    val animeList by animeListViewModel.mediaList.collectAsState()

    val mangaHasNextChunk by mangaListViewModel.hasNextChunk.collectAsState()
    val mangaIsFetchingMore by mangaListViewModel.isFetchingMore.collectAsState()
    val mangaList by mangaListViewModel.mediaList.collectAsState()

    LaunchedEffect(viewer?.id) {
        viewer?.id?.let {
            if(selected == "anime") {
                animeListViewModel.fetchMediaList(
                    userId = it,
                    statusIn = listOf(MediaListStatus.CURRENT, MediaListStatus.REPEATING),
                    type = MediaType.ANIME
                )
            } else if(selected == "manga") {
                mangaListViewModel.fetchMediaList(
                    userId = it,
                    statusIn = listOf(MediaListStatus.CURRENT, MediaListStatus.REPEATING),
                    type = MediaType.MANGA
                )
            }
        }
    }

    LaunchedEffect(columnState) {
        snapshotFlow { columnState.state.layoutInfo }
            .collect { layoutInfo ->
                val totalItems = layoutInfo.totalItemsCount
                val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
                if(selected == "anime") {
                    if (totalItems > 0 && lastVisibleItemIndex >= totalItems - 2 && !animeIsFetchingMore && animeHasNextChunk) {
                        viewer?.id?.let {
                            animeListViewModel.fetchMoreMediaList(
                                userId = it,
                                statusIn = listOf(MediaListStatus.CURRENT, MediaListStatus.REPEATING),
                                type = MediaType.ANIME
                            )
                        }
                    }
                } else if(selected == "manga") {
                    if (totalItems > 0 && lastVisibleItemIndex >= totalItems - 2 && !mangaIsFetchingMore && mangaHasNextChunk) {
                        viewer?.id?.let {
                            mangaListViewModel.fetchMoreMediaList(
                                userId = it,
                                statusIn = listOf(MediaListStatus.CURRENT, MediaListStatus.REPEATING),
                                type = MediaType.MANGA
                            )
                        }
                    }
                }
            }
    }

    fun refresh() {
        viewer?.id?.let {
            if(selected == "anime") {
                animeListViewModel.fetchMediaList(
                    userId = it,
                    statusIn = listOf(MediaListStatus.CURRENT, MediaListStatus.REPEATING),
                    type = MediaType.ANIME
                )
            } else if(selected == "manga") {
                mangaListViewModel.fetchMediaList(
                    userId = it,
                    statusIn = listOf(MediaListStatus.CURRENT, MediaListStatus.REPEATING),
                    type = MediaType.MANGA
                )
            }
        }
    }

    val scaleFraction = {
        if (isViewerLoading) 1f
        else LinearOutSlowInEasing.transform(pullToRefreshState.distanceFraction).coerceIn(0f, 1f)
    }

    ScreenScaffold(
        scrollState = columnState
    ) {
        Box(
            modifier = Modifier
                .pullToRefresh(
                    state = pullToRefreshState,
                    isRefreshing = isViewerLoading,
                    onRefresh = {
                        viewerViewModel.fetchViewer()
                        refresh()
                    },
                    threshold = PullToRefreshDefaults.PositionalThreshold / 2
                )
        ) {
            ScalingLazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                columnState = columnState
            ) {
                item {
                    Row(
                        modifier = Modifier.fillParentMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clickable {
                                    if(selected != "anime") selected = "anime"
                                }
                                .background(
                                    color = if(selected == "anime") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                    shape = RoundedCornerShape(
                                        100.dp
                                    )
                                )
                                .border(
                                    width = 2.dp,
                                    color = if(selected == "anime") MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    shape = RoundedCornerShape(100.dp)
                                )
                                .padding(
                                    horizontal = 6.dp,
                                    vertical = 4.dp
                                )
                        ) {
                            Text(
                                text = "Anime",
                                fontSize = 10.sp,
                                color = if(selected == "anime") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Box(
                            modifier = Modifier
                                .clickable {
                                    if(selected != "manga") selected = "manga"
                                }
                                .background(
                                    color = if(selected == "manga") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                    shape = RoundedCornerShape(100.dp)
                                )
                                .border(
                                    width = 2.dp,
                                    color = if(selected == "manga") MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    shape = RoundedCornerShape(100.dp)
                                )
                                .padding(
                                    horizontal = 6.dp,
                                    vertical = 4.dp
                                )
                        ) {
                            Text(
                                text = "Manga",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if(selected == "manga") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if(selected == "anime") {
                    items(animeList) { entry ->
                        entry?.let {
                            ListItem(entry, animeListViewModel)
                        }
                    }
                } else if(selected == "manga") {
                    items(mangaList) { entry ->
                        entry?.let {
                            ListItem(entry, mangaListViewModel)
                        }
                    }
                }
            }

            Box(
                Modifier.align(Alignment.TopCenter).graphicsLayer {
                    scaleX = scaleFraction()
                    scaleY = scaleFraction()
                }
            ) {
                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = isViewerLoading || isAnimeLoading || isMangaLoading,
                    containerColor = MaterialTheme.colorScheme.surface,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

    }
}