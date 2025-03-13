package dev.trimpsuz.anilist.utils

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import dev.trimpsuz.anilist.GetMediaQuery
import dev.trimpsuz.anilist.SaveMediaListEntryMutation

suspend fun fetchMedia(apolloClient: ApolloClient, ids: List<Int>): List<GetMediaQuery.Medium?>? {
    val response = apolloClient.query(GetMediaQuery(Optional.presentIfNotNull(ids))).execute()

    return response.data?.Page?.media
}

suspend fun updateMediaProgress(apolloClient: ApolloClient, entryId: Int, newProgress: Int) {
    val response = apolloClient.mutation(
        SaveMediaListEntryMutation(
            Optional.present(entryId),
            Optional.absent(),
            Optional.present(newProgress)
        )
    ).execute()

    if (response.hasErrors()) {
        println("Error updating progress: ${response.errors}")
    } else {
        println("Updated progress for entry $entryId to $newProgress")
    }
}