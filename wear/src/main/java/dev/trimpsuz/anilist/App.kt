package dev.trimpsuz.anilist

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.apollographql.apollo.ApolloClient
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import dev.trimpsuz.anilist.utils.DataStoreRepository
import dev.trimpsuz.anilist.utils.GlobalVariables
import dev.trimpsuz.anilist.utils.UpdateTileWorker
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface HiltWorkerFactoryEntryPoint {
        fun workerFactory(): CustomWorkerFactory
    }

    override val workManagerConfiguration: Configuration =
        Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setWorkerFactory(EntryPoints.get(this, HiltWorkerFactoryEntryPoint::class.java).workerFactory())
            .build()
}

class CustomWorkerFactory @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
    private val globalVariables: GlobalVariables,
    private val apolloClient: ApolloClient
): WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker = UpdateTileWorker(dataStoreRepository, globalVariables, apolloClient, appContext, workerParameters)
}