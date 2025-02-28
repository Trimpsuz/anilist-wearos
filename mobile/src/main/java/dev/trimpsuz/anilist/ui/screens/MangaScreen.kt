package dev.trimpsuz.anilist.ui.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.trimpsuz.anilist.GetMediaListEntriesQuery
import dev.trimpsuz.anilist.type.MediaListStatus
import dev.trimpsuz.anilist.type.MediaType
import dev.trimpsuz.anilist.ui.composables.GridItem
import dev.trimpsuz.anilist.ui.viewModels.LoggedInViewModel
import dev.trimpsuz.anilist.ui.viewModels.MediaListViewModel
import dev.trimpsuz.anilist.ui.viewModels.ViewerViewModel
import dev.trimpsuz.anilist.utils.firstBlocking
import dev.trimpsuz.anilist.utils.sendToWear
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaScreen(
    viewerViewModel: ViewerViewModel,
    mediaListViewModel: MediaListViewModel,
    loggedInViewModel: LoggedInViewModel,
    lazyGridState: LazyGridState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val viewer by viewerViewModel.viewer.collectAsState()

    val isLoading by mediaListViewModel.isLoading.collectAsState()
    val isViewerLoading by viewerViewModel.isLoading.collectAsState()

    val hasNextChunk by mediaListViewModel.hasNextChunk.collectAsState()
    val isFetchingMore by mediaListViewModel.isFetchingMore.collectAsState()
    val mediaList by mediaListViewModel.mediaList.collectAsState()

    val selectedMediaIds by loggedInViewModel.selectedMediaIds.collectAsStateWithLifecycle(loggedInViewModel.selectedMediaIds.firstBlocking())

    fun updateSelectedMediaIds(entry: GetMediaListEntriesQuery. Entry) {
        val newSelectedMediaIds = when {
            entry.media?.id == null -> selectedMediaIds
            selectedMediaIds.contains(entry.media.id) -> selectedMediaIds - entry.media.id
            selectedMediaIds.size < 3 -> selectedMediaIds + entry.media.id
            else -> {
                Toast.makeText(context, "You can only select up to 3 entries in total", Toast.LENGTH_SHORT).show()
                selectedMediaIds
            }
        }

        if(newSelectedMediaIds != selectedMediaIds) {
            loggedInViewModel.setSelectedMediaIds(newSelectedMediaIds)
            scope.launch(Dispatchers.IO) {
                sendToWear("list", newSelectedMediaIds.toString(), context)
            }
        }
    }

    fun fetchList(id: Int?, refresh: Boolean) {
        if(mediaList.isNotEmpty() && !refresh) return
        id?.let {
            mediaListViewModel.fetchMediaList(
                userId = it,
                statusIn = listOf(MediaListStatus.CURRENT, MediaListStatus.REPEATING),
                type = MediaType.MANGA
            )
        }
    }

    LaunchedEffect(viewer?.id) {
        fetchList(viewer?.id, false)
    }

    LaunchedEffect(lazyGridState) {
        snapshotFlow { lazyGridState.layoutInfo }
            .collect { layoutInfo ->
                val totalItems = layoutInfo.totalItemsCount
                val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
                if (totalItems > 0 && lastVisibleItemIndex >= totalItems - 2 && !isFetchingMore && hasNextChunk) {
                    viewer?.id?.let {
                        mediaListViewModel.fetchMoreMediaList(
                            userId = it,
                            statusIn = listOf(MediaListStatus.CURRENT, MediaListStatus.REPEATING),
                            type = MediaType.MANGA
                        )
                    }
                }
            }
    }

    PullToRefreshBox(
        state = rememberPullToRefreshState(),
        isRefreshing = isLoading || isViewerLoading,
        onRefresh = {
            viewerViewModel.fetchViewer()
            fetchList(viewer?.id, true)
        },
        modifier = Modifier.fillMaxSize(),
    ) {
        if (viewer != null) {
            LazyVerticalGrid(
                state = lazyGridState,
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(mediaList) { entry ->
                    entry?.let {
                        val isSelected = selectedMediaIds.contains(it.media?.id)
                        GridItem(it, isSelected) { clickedEntry ->
                            updateSelectedMediaIds(clickedEntry)
                        }
                    }
                }

                if(!hasNextChunk) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = "This is the end of the list. Please note only entries in the \"(re)reading\" status will show up here.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if(isFetchingMore) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

