package dev.trimpsuz.anilist.utils

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.wear.tiles.TileService
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.apollographql.apollo.ApolloClient
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.trimpsuz.anilist.tile.MainTileService
import dev.trimpsuz.anilist.type.MediaListStatus

@HiltWorker
class UpdateTileWorker @AssistedInject constructor(
    @Assisted private val dataStoreRepository: DataStoreRepository,
    @Assisted private val globalVariables: GlobalVariables,
    @Assisted private val apolloClient: ApolloClient,
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        var selectedMediaIds = dataStoreRepository.selectedMedia.firstBlocking()?.toList() ?: emptyList()
        if (selectedMediaIds.isEmpty()) return Result.success()

        val mediaList = fetchMedia(apolloClient, selectedMediaIds.map { it.toInt() })

        val newSelectedMediaIds = selectedMediaIds.toMutableList()
        mediaList?.forEach { media ->
            if (media?.mediaListEntry?.status !in listOf(
                    MediaListStatus.CURRENT,
                    MediaListStatus.REPEATING
                )
            ) {
                newSelectedMediaIds.remove(media?.id.toString())
            }
        }
        selectedMediaIds = newSelectedMediaIds.toList()
        sendToMobile("list", selectedMediaIds.toString(), applicationContext)
        dataStoreRepository.setSelectedMedia(selectedMediaIds.toSet())
        val imageFiles = applicationContext.filesDir.listFiles { file -> file.extension == "png" }
        imageFiles?.forEach { file ->
            if (!selectedMediaIds.contains(file.nameWithoutExtension)) file.delete()
        }

        globalVariables.mediaList = mediaList

        TileService.getUpdater(applicationContext).requestUpdate(MainTileService::class.java)

        return Result.success()
    }
}