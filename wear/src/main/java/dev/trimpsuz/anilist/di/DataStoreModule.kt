package dev.trimpsuz.anilist.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Singleton
    @Provides
    fun providePreferencesDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { appContext.preferencesDataStoreFile("default") }
        )
    }

    fun <T> DataStore<Preferences>.getValue(key: Preferences.Key<T>) = data.map { it[key] }

    fun <T> DataStore<Preferences>.getValue(
        key: Preferences.Key<T>,
        default: T,
    ) = data.map { it[key] ?: default }

    suspend fun <T> DataStore<Preferences>.setValue(
        key: Preferences.Key<T>,
        value: T?
    ) = edit {
        if (value != null) it[key] = value
        else it.remove(key)
    }

    fun <T> DataStore<Preferences>.getValueBlocking(key: Preferences.Key<T>) =
        runBlocking { data.first() }[key]
}