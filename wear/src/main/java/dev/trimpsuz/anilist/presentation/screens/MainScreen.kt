package dev.trimpsuz.anilist.presentation.screens

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Straight
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.AlertDialog
import dev.trimpsuz.anilist.presentation.composables.ListItem
import dev.trimpsuz.anilist.presentation.viewModels.MainViewModel
import dev.trimpsuz.anilist.presentation.viewModels.MediaListViewModel
import dev.trimpsuz.anilist.presentation.viewModels.ViewerViewModel
import dev.trimpsuz.anilist.type.MediaListSort
import dev.trimpsuz.anilist.type.MediaListStatus
import dev.trimpsuz.anilist.type.MediaType
import dev.trimpsuz.anilist.type.UserTitleLanguage
import dev.trimpsuz.anilist.utils.firstBlocking
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalHorologistApi::class, ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)
@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    viewerViewModel: ViewerViewModel = hiltViewModel(),
    animeListViewModel: MediaListViewModel = hiltViewModel(key = "anime"),
    mangaListViewModel: MediaListViewModel = hiltViewModel(key = "manga"),
) {
    val scope = rememberCoroutineScope()

    val columnState = rememberResponsiveColumnState()

    val pullToRefreshState = rememberPullToRefreshState()

    var selected by remember { mutableStateOf("anime") }

    var showFilterDialog by remember { mutableStateOf(false) }

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

    var selectedStatus by remember { mutableStateOf(mainViewModel.filterStatus.firstBlocking() ?: "Current") }

    var selectedStatusesList = remember {
        when(selectedStatus) {
            "Current" -> listOf(MediaListStatus.CURRENT, MediaListStatus.REPEATING)
            "Unfinished" -> listOf(MediaListStatus.CURRENT, MediaListStatus.REPEATING, MediaListStatus.PLANNING, MediaListStatus.PAUSED)
            else -> listOf(MediaListStatus.CURRENT, MediaListStatus.REPEATING, MediaListStatus.PLANNING, MediaListStatus.PAUSED, MediaListStatus.DROPPED)
        }
    }

    val statusOptions = listOf("Current", "Unfinished", "All")

    var selectedSort by remember { mutableStateOf(mainViewModel.filterSort.firstBlocking() ?: "Update date") }

    var desc by remember { mutableStateOf(mainViewModel.filterSortDesc.firstBlocking() ?: true) }

    val getSort =
        when(selectedSort) {
            "Title" -> viewer?.options?.titleLanguage?.let {
                when(it) {
                    UserTitleLanguage.ENGLISH -> if(desc) listOf(MediaListSort.MEDIA_TITLE_ENGLISH_DESC) else listOf(MediaListSort.MEDIA_TITLE_ENGLISH)
                    UserTitleLanguage.NATIVE -> if(desc) listOf(MediaListSort.MEDIA_TITLE_NATIVE_DESC) else listOf(MediaListSort.MEDIA_TITLE_NATIVE)
                    else -> if(desc) listOf(MediaListSort.MEDIA_TITLE_ROMAJI_DESC) else listOf(MediaListSort.MEDIA_TITLE_ROMAJI)
                }
            } ?: if(desc) listOf(MediaListSort.MEDIA_TITLE_ROMAJI_DESC) else listOf(MediaListSort.MEDIA_TITLE_ROMAJI)
            "Score" -> if(desc) listOf(MediaListSort.SCORE_DESC) else listOf(MediaListSort.SCORE)
            "Progress" -> if(desc) listOf(MediaListSort.PROGRESS_DESC) else listOf(MediaListSort.PROGRESS)
            "Update date" -> if(desc) listOf(MediaListSort.UPDATED_TIME_DESC) else listOf(MediaListSort.UPDATED_TIME)
            "Add date" -> if(desc) listOf(MediaListSort.ADDED_TIME_DESC) else listOf(MediaListSort.ADDED_TIME)
            "Start date" -> if(desc) listOf(MediaListSort.ADDED_TIME_DESC) else listOf(MediaListSort.ADDED_TIME)
            "Finish date" -> if(desc) listOf(MediaListSort.ADDED_TIME_DESC) else listOf(MediaListSort.ADDED_TIME)
            "Repeat count" -> if(desc) listOf(MediaListSort.ADDED_TIME_DESC) else listOf(MediaListSort.ADDED_TIME)
            else -> if(desc) listOf(MediaListSort.UPDATED_TIME_DESC) else listOf(MediaListSort.UPDATED_TIME)
        }

    var sort = remember { getSort }

    val sortOptions = listOf("Title", "Score", "Progress", "Update date", "Add date", "Start date", "Finish date", "Repeat count")

    LaunchedEffect(selectedSort, desc) {
        sort = getSort

        viewer?.id?.let {
            animeListViewModel.fetchMediaList(
                userId = it,
                statusIn = selectedStatusesList,
                type = MediaType.ANIME,
                sort = sort
            )

            mangaListViewModel.fetchMediaList(
                userId = it,
                statusIn = selectedStatusesList,
                type = MediaType.MANGA,
                sort = sort
            )
        }
    }

    LaunchedEffect(selectedStatus) {
        selectedStatusesList = when(selectedStatus) {
            "Current" -> listOf(MediaListStatus.CURRENT, MediaListStatus.REPEATING)
            "Unfinished" -> listOf(MediaListStatus.CURRENT, MediaListStatus.REPEATING, MediaListStatus.PLANNING, MediaListStatus.PAUSED)
            else -> listOf(MediaListStatus.CURRENT, MediaListStatus.REPEATING, MediaListStatus.PLANNING, MediaListStatus.PAUSED, MediaListStatus.DROPPED)
        }

        viewer?.id?.let {
            animeListViewModel.fetchMediaList(
                userId = it,
                statusIn = selectedStatusesList,
                type = MediaType.ANIME,
                sort = sort
            )

            mangaListViewModel.fetchMediaList(
                userId = it,
                statusIn = selectedStatusesList,
                type = MediaType.MANGA,
                sort = sort
            )
        }
    }

    LaunchedEffect(viewer?.id) {
        viewer?.id?.let {
            if(selected == "anime") {
                animeListViewModel.fetchMediaList(
                    userId = it,
                    statusIn = selectedStatusesList,
                    type = MediaType.ANIME,
                    sort = sort
                )
            } else if(selected == "manga") {
                mangaListViewModel.fetchMediaList(
                    userId = it,
                    statusIn = selectedStatusesList,
                    type = MediaType.MANGA,
                    sort = sort
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
                                statusIn = selectedStatusesList,
                                type = MediaType.ANIME,
                                sort = sort
                            )
                        }
                    }
                } else if(selected == "manga") {
                    if (totalItems > 0 && lastVisibleItemIndex >= totalItems - 2 && !mangaIsFetchingMore && mangaHasNextChunk) {
                        viewer?.id?.let {
                            mangaListViewModel.fetchMoreMediaList(
                                userId = it,
                                statusIn = selectedStatusesList,
                                type = MediaType.MANGA,
                                sort = sort
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
                    statusIn = selectedStatusesList,
                    type = MediaType.ANIME,
                    sort = sort
                )
            } else if(selected == "manga") {
                mangaListViewModel.fetchMediaList(
                    userId = it,
                    statusIn = selectedStatusesList,
                    type = MediaType.MANGA,
                    sort = sort
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
                                fontWeight = if(selected == "anime") FontWeight.SemiBold else FontWeight.Medium,
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
                                fontWeight = if(selected == "manga") FontWeight.SemiBold else FontWeight.Medium,
                                color = if(selected == "manga") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = null,
                            modifier = Modifier
                                .clickable { showFilterDialog = true }
                                .size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
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

        AlertDialog(
            showDialog = showFilterDialog,
            onDismiss = { showFilterDialog = false },
            title = "Filter options"
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Status",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )

                    Icon(
                        imageVector = Icons.Outlined.Category,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.Center)
                            .offset(x = (-28).dp - 8.dp)
                    )
                }
            }

            item {
                FlowRow(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    statusOptions.forEach { option ->
                        Box(
                            modifier = Modifier
                                .clickable {
                                    if(selectedStatus != option) {
                                        selectedStatus = option
                                        scope.launch(Dispatchers.IO) {
                                            mainViewModel.setFilterStatus(option)
                                        }
                                    }
                                }
                                .background(
                                    color = if(selectedStatus == option) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                    shape = RoundedCornerShape(
                                        100.dp
                                    )
                                )
                                .border(
                                    width = 2.dp,
                                    color = if(selectedStatus == option) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    shape = RoundedCornerShape(100.dp)
                                )
                                .padding(
                                    horizontal = 6.dp,
                                    vertical = 4.dp
                                )
                        ) {
                            Text(
                                text = option,
                                fontSize = 10.sp,
                                fontWeight = if(selectedStatus == option) FontWeight.SemiBold else FontWeight.Medium,
                                color = if(selectedStatus == option) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sort",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )

                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Sort,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.Center)
                            .offset(x = (-20).dp - 8.dp)
                    )
                }
            }

            item {
                FlowRow(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    sortOptions.forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable {
                                    if(selectedSort == option) {
                                        desc = !desc
                                        scope.launch(Dispatchers.IO){
                                            mainViewModel.setFilterSortDesc(desc)
                                        }
                                    }
                                    if(selectedSort != option) {
                                        selectedSort = option
                                        scope.launch(Dispatchers.IO){
                                            mainViewModel.setFilterSort(option)
                                        }
                                    }
                                }
                                .background(
                                    color = if(selectedSort == option) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                    shape = RoundedCornerShape(
                                        100.dp
                                    )
                                )
                                .border(
                                    width = 2.dp,
                                    color = if(selectedSort == option) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    shape = RoundedCornerShape(100.dp)
                                )
                                .padding(
                                    horizontal = 6.dp,
                                    vertical = 4.dp
                                )
                        ) {
                            Text(
                                text = option,
                                fontSize = 10.sp,
                                fontWeight = if(selectedSort == option) FontWeight.SemiBold else FontWeight.Medium,
                                color = if(selectedSort == option) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary
                            )
                            if(selectedSort == option) {
                                Icon(
                                    imageVector = Icons.Outlined.Straight,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(12.dp)
                                        .graphicsLayer(
                                            scaleY = if(desc) -1f else 1f
                                        ),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            item {
                Box(
                    modifier = Modifier
                        .clickable {
                            showFilterDialog = false
                        }
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(
                                100.dp,
                            )
                        )
                        .padding(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}