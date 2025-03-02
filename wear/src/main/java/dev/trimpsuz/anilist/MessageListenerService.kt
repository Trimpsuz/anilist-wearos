package dev.trimpsuz.anilist

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.wear.tiles.TileService
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import dev.trimpsuz.anilist.tile.MainTileService
import dev.trimpsuz.anilist.utils.DataStoreRepository
import dev.trimpsuz.anilist.utils.GlobalVariables
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject


@AndroidEntryPoint
class MessageListenerService : WearableListenerService() {
    @Inject
    lateinit var dataStoreRepository: DataStoreRepository

    @Inject
    lateinit var globalVariables: GlobalVariables

    @Inject
    lateinit var apolloClient: ApolloClient

    @Inject
    lateinit var client: OkHttpClient

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var listJob: Job? = null
    private val debounceDelay = 1500L

    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            "/accessToken" -> {
                serviceScope.launch {
                    dataStoreRepository.setAccessToken(String(messageEvent.data))
                    globalVariables.accessToken = String(messageEvent.data)
                    TileService.getUpdater(applicationContext).requestUpdate(MainTileService::class.java)
                }
            }
            "/list" -> {
                listJob?.cancel()

                listJob = serviceScope.launch {
                    delay(debounceDelay)

                    val selectedMediaIds = String(messageEvent.data)
                        .removeSurrounding("[", "]")
                        .split(", ")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .toSet()

                    dataStoreRepository.setSelectedMedia(selectedMediaIds)

                    val downloadSuccess = fetchMediaDetails(selectedMediaIds)

                    if (downloadSuccess) {
                        TileService.getUpdater(applicationContext)
                            .requestUpdate(MainTileService::class.java)
                        globalVariables.RESOURCES_VERSION =
                            (globalVariables.RESOURCES_VERSION.toInt() + 1).toString()
                    }
                }
            }
            "/interval" -> {
                serviceScope.launch {
                    dataStoreRepository.setUpdateInterval(String(messageEvent.data))
                    globalVariables.REFRESH_INTERVAL_TILE = (String(messageEvent.data).toLong())
                    TileService.getUpdater(applicationContext).requestUpdate(MainTileService::class.java)
                }
            }
        }
    }

    private suspend fun fetchMediaDetails(ids: Set<String>): Boolean {
        return try {
            val response = apolloClient.query(GetMediaQuery(Optional.presentIfNotNull(ids.toList().map { it.toInt() }))).execute()

            val downloadJobs = mutableListOf<Deferred<Boolean>>()

            val filesDir = applicationContext.filesDir
            val imageFiles = filesDir.listFiles { file -> file.extension == "png"}

            val alreadySaved = mutableSetOf<String>()

            imageFiles?.forEach { file ->
                if(!ids.contains(file.nameWithoutExtension)) file.delete() else alreadySaved.add(file.nameWithoutExtension)
            }

            response.data?.Page?.media?.forEach { media ->
                media?.coverImage?.medium?.let { imageUrl ->
                    if(!alreadySaved.contains(media.id.toString())) {
                        val job = downloadAndSaveImageAsync(imageUrl, media.id.toString())
                        downloadJobs.add(job)
                    }
                }
            }

            val results = downloadJobs.awaitAll()
            results.all { it }

        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun downloadAndSaveImageAsync(url: String, fileName: String): Deferred<Boolean> {
        return serviceScope.async(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful && response.body != null) {
                    val inputStream = response.body!!.byteStream()
                    val bitmap = BitmapFactory.decodeStream(inputStream)

                    if (bitmap != null) {
                        saveBitmapToFile(bitmap, fileName)
                        true
                    } else {
                        println("MessageListenerService, Decoded bitmap is null")
                        false
                    }
                } else {
                    println("MessageListenerService, Image download failed: ${response.code}")
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap, fileName: String): Boolean {
        return try {
            val file = File(applicationContext.filesDir, "$fileName.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}