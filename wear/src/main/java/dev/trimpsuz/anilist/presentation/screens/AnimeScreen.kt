package dev.trimpsuz.anilist.presentation.screens

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import dev.trimpsuz.anilist.presentation.viewModels.MediaListViewModel
import dev.trimpsuz.anilist.presentation.viewModels.ViewerViewModel
import dev.trimpsuz.anilist.type.MediaListStatus
import dev.trimpsuz.anilist.type.MediaType

@OptIn(ExperimentalHorologistApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AnimeScreen(
    viewerViewModel: ViewerViewModel = hiltViewModel(),
    mediaListViewModel: MediaListViewModel = hiltViewModel(),
) {
    val columnState = rememberResponsiveColumnState()

    val pullToRefreshState = rememberPullToRefreshState()

    val viewer by viewerViewModel.viewer.collectAsState()

    val isLoading by mediaListViewModel.isLoading.collectAsState()
    val isViewerLoading by viewerViewModel.isLoading.collectAsState()

    val hasNextChunk by mediaListViewModel.hasNextChunk.collectAsState()
    val isFetchingMore by mediaListViewModel.isFetchingMore.collectAsState()
    val mediaList by mediaListViewModel.mediaList.collectAsState()

    val scaleFraction = {
        if (isLoading || isViewerLoading) 1f
        else LinearOutSlowInEasing.transform(pullToRefreshState.distanceFraction).coerceIn(0f, 1f)
    }

    fun fetchList(id: Int?, refresh: Boolean) {
        if(mediaList.isNotEmpty() && !refresh) return
        id?.let {
            mediaListViewModel.fetchMediaList(
                userId = it,
                statusIn = listOf(MediaListStatus.CURRENT, MediaListStatus.REPEATING),
                type = MediaType.ANIME
            )
        }
    }

    LaunchedEffect(viewer?.id) {
        fetchList(viewer?.id, false)
    }

    LaunchedEffect(columnState) {
        snapshotFlow { columnState.state.layoutInfo }
            .collect { layoutInfo ->
                val totalItems = layoutInfo.totalItemsCount
                val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
                if (totalItems > 0 && lastVisibleItemIndex >= totalItems - 2 && !isFetchingMore && hasNextChunk) {
                    viewer?.id?.let {
                        mediaListViewModel.fetchMoreMediaList(
                            userId = it,
                            statusIn = listOf(MediaListStatus.CURRENT, MediaListStatus.REPEATING),
                            type = MediaType.ANIME
                        )
                    }
                }
            }
    }

    ScreenScaffold(scrollState = columnState) {
        Box(
            modifier =
            Modifier.pullToRefresh(
                state = pullToRefreshState,
                isRefreshing = isLoading || isViewerLoading,
                onRefresh = {
                    viewerViewModel.fetchViewer()
                    fetchList(viewer?.id, true)
                },
                threshold = PullToRefreshDefaults.PositionalThreshold / 2
            )
        ) {
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                columnState = columnState
            ) {
                items(mediaList) { entry ->
                    entry?.let {
                        entry.media?.title?.userPreferred?.let {
                            Text(text = it)
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
                    isRefreshing = isLoading || isViewerLoading,
                    containerColor = MaterialTheme.colors.surface,
                    color = MaterialTheme.colors.onSurface
                )
            }
        }
    }
}