package dev.trimpsuz.anilist.ui.viewModels

import androidx.lifecycle.ViewModel
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.trimpsuz.anilist.GetMediaStatusQuery
import dev.trimpsuz.anilist.type.MediaListStatus
import javax.inject.Inject

@HiltViewModel
class MediaStatusViewModel @Inject constructor(
    private val client: ApolloClient
) : ViewModel() {
    suspend fun fetchMediaStatuses(mediaIds: Set<Int>): Map<Int, MediaListStatus?> {
        return try {
            val response = client.query(
                GetMediaStatusQuery(Optional.presentIfNotNull(mediaIds.toList()))
            ).execute()

            response.data?.Page?.media?.mapNotNull { media ->
                media?.id?.let { id ->
                    id to media.mediaListEntry?.status
                }
            }?.toMap() ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
}