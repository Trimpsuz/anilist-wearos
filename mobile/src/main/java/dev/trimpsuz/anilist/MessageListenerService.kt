package dev.trimpsuz.anilist

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import dev.trimpsuz.anilist.utils.DataStoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MessageListenerService : WearableListenerService() {
    @Inject
    lateinit var dataStoreRepository: DataStoreRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            "/list" -> {

                serviceScope.launch {
                    val selectedMediaIds = String(messageEvent.data)
                        .removeSurrounding("[", "]")
                        .split(", ")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .toSet()

                    dataStoreRepository.setSelectedMedia(selectedMediaIds)
                }
            }
        }
    }
}