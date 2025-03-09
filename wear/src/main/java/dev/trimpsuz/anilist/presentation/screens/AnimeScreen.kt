package dev.trimpsuz.anilist.presentation.screens

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Text
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import dev.trimpsuz.anilist.presentation.viewModels.MediaListViewModel
import dev.trimpsuz.anilist.presentation.viewModels.ViewerViewModel
import dev.trimpsuz.anilist.type.MediaListStatus
import dev.trimpsuz.anilist.type.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalHorologistApi::class, ExperimentalMaterial3Api::class,
    ExperimentalGlideComposeApi::class
)
@Composable
fun AnimeScreen(
    viewerViewModel: ViewerViewModel = hiltViewModel(),
    mediaListViewModel: MediaListViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()

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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                columnState = columnState
            ) {
                items(mediaList) { entry ->
                    entry?.let {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(
                                        14.dp,
                                    )
                                )
                                .fillParentMaxWidth()
                                .padding(
                                    vertical = 6.dp,
                                    horizontal = 8.dp
                                )
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.height(IntrinsicSize.Min)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(6.dp))
                                        .weight(1f)
                                ) {
                                    GlideImage(
                                        model = entry.media?.coverImage?.medium,
                                        contentDescription = "Cover Image",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .aspectRatio(3f / 4f),
                                        contentScale = ContentScale.FillBounds,
                                    )

                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .background(
                                                MaterialTheme.colorScheme.secondaryContainer,
                                                RoundedCornerShape(
                                                    topEnd = 4.dp,
                                                )
                                            )
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = when (entry.status) {
                                                MediaListStatus.CURRENT -> Icons.Outlined.PlayCircleOutline
                                                MediaListStatus.PLANNING -> Icons.Outlined.Schedule
                                                MediaListStatus.COMPLETED -> Icons.Outlined.CheckCircle
                                                MediaListStatus.DROPPED -> Icons.Outlined.DeleteOutline
                                                MediaListStatus.PAUSED -> Icons.Outlined.PauseCircle
                                                MediaListStatus.REPEATING -> Icons.Outlined.Replay
                                                MediaListStatus.UNKNOWN__ -> Icons.AutoMirrored.Outlined.ListAlt
                                                null -> Icons.AutoMirrored.Outlined.ListAlt
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp),
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Column(
                                    horizontalAlignment = Alignment.Start,
                                    verticalArrangement = Arrangement.SpaceEvenly,
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(2f)
                                ) {
                                    Text(
                                        text = entry.media?.title?.userPreferred ?: "Title",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.weight(2f))

                                    Text(
                                        text = entry.progress?.let { progress ->
                                            "${if (progress > 0) progress else 0}${entry.media?.episodes?.let { "/$it" } ?: entry.media?.chapters?.let { "/$it" } ?: ""}"
                                        } ?: entry.media?.episodes?.let { "0/$it" }
                                        ?: entry.media?.chapters?.let { "0/$it" } ?: "0",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillParentMaxWidth()
                                            .height(2.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.secondaryContainer,
                                                shape = RoundedCornerShape(
                                                    100.dp,
                                                )
                                            )
                                    ) {
                                        if(entry.media?.episodes != null || entry.media?.chapters != null) {
                                            var fraction = 0f
                                            if(entry.progress != null) fraction =
                                                (entry.progress.toFloat() / (entry.media.episodes ?: entry.media.chapters
                                                ?: 0))

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(fraction)
                                                    .height(2.dp)
                                                    .background(
                                                        color = MaterialTheme.colorScheme.secondary,
                                                        shape = RoundedCornerShape(
                                                            100.dp,
                                                        )
                                                    )
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(
                                                    100.dp,
                                            )
                                        )
                                        .padding(
                                            vertical = 6.dp,
                                            horizontal = 10.dp
                                        )
                                        .clickable {
                                            scope.launch(Dispatchers.IO) {
                                                if(entry.media != null) mediaListViewModel.updateMediaProgress(entry.media.id, entry.id)
                                            }
                                        }
                                ) {
                                    Text(
                                        text = "+",
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
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
                    containerColor = MaterialTheme.colorScheme.surface,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}