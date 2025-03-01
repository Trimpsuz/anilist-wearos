package dev.trimpsuz.anilist.utils

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable

fun sendToMobile(path: String, data: String, context: Context) {
    val nodes = getNodes(context)

    if(nodes.isNotEmpty()) {
        nodes.first().also {nodeId ->
            Wearable.getMessageClient(context).sendMessage(
                nodeId,
                "/$path",
                data.toByteArray()
            )
        }
    }
}

fun getNodes(context: Context): Collection<String> {
    return Tasks.await(Wearable.getNodeClient(context).connectedNodes).map { it.id }
}