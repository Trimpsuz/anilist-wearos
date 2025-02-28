package dev.trimpsuz.anilist.utils

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import dev.trimpsuz.anilist.GetMediaQuery
import dev.trimpsuz.anilist.SaveMediaListEntryMutation
import kotlinx.coroutines.runBlocking

fun fetchMediaProgress(apolloClient: ApolloClient, ids: List<Int>): Map<Int, Triple<Int?, Int?, Int?>> = runBlocking {
    val response = apolloClient.query(GetMediaQuery(Optional.presentIfNotNull(ids))).execute()

    response.data?.Page?.media?.associate { media ->
        val total = media?.episodes ?: media?.chapters
        val progress = media?.mediaListEntry?.progress
        val entryId = media?.mediaListEntry?.id
        (media?.id ?: 0) to Triple(progress, total, entryId)
    } ?: emptyMap()
}

fun updateMediaProgress(apolloClient: ApolloClient, entryId: Int, newProgress: Int) = runBlocking {
    val response = apolloClient.mutation(
        SaveMediaListEntryMutation(
            Optional.present(entryId),
            Optional.present(newProgress)
        )
    ).execute()

    if (response.hasErrors()) {
        println("Error updating progress: ${response.errors}")
    } else {
        println("Updated progress for entry $entryId to $newProgress")
    }
}