package dev.trimpsuz.anilist.presentation.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.trimpsuz.anilist.GetMediaListEntriesQuery
import dev.trimpsuz.anilist.type.MediaListSort
import dev.trimpsuz.anilist.type.MediaListStatus
import dev.trimpsuz.anilist.type.MediaType
import dev.trimpsuz.anilist.utils.fetchMedia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val perChunk = 500;

@HiltViewModel
class MediaListViewModel @Inject constructor(
    private val client: ApolloClient
) : ViewModel() {

    private val _mediaList = MutableStateFlow<List<GetMediaListEntriesQuery.Entry?>>(emptyList())
    val mediaList: StateFlow<List<GetMediaListEntriesQuery.Entry?>> = _mediaList

    private val _hasNextChunk = MutableStateFlow(true)
    val hasNextChunk: StateFlow<Boolean> = _hasNextChunk

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isFetchingMore = MutableStateFlow(false)
    val isFetchingMore: StateFlow<Boolean> = _isFetchingMore

    private var currentChunk = 1

    fun fetchMediaList(userId: Int, statusIn: List<MediaListStatus>, type: MediaType, sort: List<MediaListSort?> = listOf(null)) {
        if (_isLoading.value) return // Prevent multiple simultaneous fetches

        viewModelScope.launch {
            _isLoading.value = true
            currentChunk = 1
            try {
                val response = client.query(
                    GetMediaListEntriesQuery(
                        userId = Optional.presentIfNotNull(userId),
                        statusIn = Optional.presentIfNotNull(statusIn),
                        type = Optional.presentIfNotNull(type),
                        chunk = Optional.presentIfNotNull(currentChunk),
                        perChunk = Optional.presentIfNotNull(perChunk),
                        sort = Optional.presentIfNotNull(sort)
                    )
                ).execute()

                _mediaList.value = response.data?.MediaListCollection?.lists?.flatMap { it?.entries ?: emptyList() } ?: emptyList()
                _hasNextChunk.value = response.data?.MediaListCollection?.hasNextChunk ?: false

                if (_hasNextChunk.value) currentChunk++
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchMoreMediaList(userId: Int, statusIn: List<MediaListStatus>, type: MediaType, sort: List<MediaListSort?> = listOf(null)) {
        if (_isFetchingMore.value || !_hasNextChunk.value) return // Prevent multiple simultaneous fetches

        viewModelScope.launch {
            _isFetchingMore.value = true
            try {
                val response = client.query(
                    GetMediaListEntriesQuery(
                        userId = Optional.presentIfNotNull(userId),
                        statusIn = Optional.presentIfNotNull(statusIn),
                        type = Optional.presentIfNotNull(type),
                        chunk = Optional.presentIfNotNull(currentChunk),
                        perChunk = Optional.presentIfNotNull(perChunk),
                        sort = Optional.presentIfNotNull(sort)
                    )
                ).execute()

                val newEntries = response.data?.MediaListCollection?.lists?.flatMap {
                    it?.entries ?: emptyList()
                } ?: emptyList()
                _mediaList.value += newEntries
                _hasNextChunk.value = response.data?.MediaListCollection?.hasNextChunk ?: false

                if (_hasNextChunk.value) currentChunk++
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isFetchingMore.value = false
            }
        }
    }

    suspend fun updateMediaProgress(mediaId: Int, entryId: Int) {
        val media = fetchMedia(client, listOf(mediaId))?.get(0)
        val progress = media?.mediaListEntry?.progress

        if(progress != null) {
            val total: Int? = media.episodes ?: media.chapters

            if(total == null || progress < total) {
                val newProgress = progress.plus(1)

                dev.trimpsuz.anilist.utils.updateMediaProgress(client, entryId, newProgress)

                _mediaList.update { list ->
                    list.map { entry ->
                        if (entry?.id == entryId) entry.copy(
                            progress = newProgress,
                            status = if (total != null && newProgress >= total) MediaListStatus.COMPLETED else media.mediaListEntry.status
                        ) else entry
                    }
                }
            }
        }
    }
}