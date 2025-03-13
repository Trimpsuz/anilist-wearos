package dev.trimpsuz.anilist.presentation.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.trimpsuz.anilist.GetMediaQuery
import dev.trimpsuz.anilist.SaveMediaListEntryMutation
import dev.trimpsuz.anilist.type.MediaListStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val client: ApolloClient
) : ViewModel() {

    private var job: Job? = null

    private val _media = MutableStateFlow<GetMediaQuery.Medium?>(null)
    val media: StateFlow<GetMediaQuery.Medium?> = _media

    private val _isLoading = MutableStateFlow(false)

    fun fetchMedia(mediaId: Int) {
        if(_isLoading.value) job?.cancel()

        job = viewModelScope.launch {
            _isLoading.value = true

            try{
                val response = client.query(GetMediaQuery(Optional.presentIfNotNull(listOf(mediaId)))).execute()

                _media.value = response.data?.Page?.media?.first()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetMedia() {
        if(_isLoading.value) job?.cancel()
        _isLoading.value = false
        _media.value = null
    }

    suspend fun updateMedia(
        entryId: Int?,
        status: MediaListStatus? = null,
        progress: Int? = null,
        progressVolumes: Int? = null,
        repeat: Int? = null
    ) {
        if(entryId == null) return

        val response = client.mutation(
            SaveMediaListEntryMutation(
                Optional.present(entryId),
                Optional.presentIfNotNull(status),
                Optional.presentIfNotNull(progress),
                Optional.presentIfNotNull(progressVolumes),
                Optional.presentIfNotNull(repeat)
            )
        ).execute()

        if (response.hasErrors()) {
            println("Error updating entry: ${response.errors}")
        } else {
            println("Updated entry $entryId")
        }
    }
}