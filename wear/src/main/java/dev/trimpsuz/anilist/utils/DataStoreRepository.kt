package dev.trimpsuz.anilist.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import dev.trimpsuz.anilist.di.DataStoreModule.getValue
import dev.trimpsuz.anilist.di.DataStoreModule.setValue
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val accessToken = dataStore.getValue(accessTokenKey)

    suspend fun setAccessToken(value: String?) {
        dataStore.setValue(accessTokenKey, value)
    }

    val isLoggedIn = accessToken.map { !it.isNullOrEmpty() }

    val selectedMedia = dataStore.getValue(selectedMediaKey)

    suspend fun setSelectedMedia(value: Set<String>) {
        dataStore.setValue(selectedMediaKey, value)
    }

    val updateInterval = dataStore.getValue(updateIntervalKey)

    suspend fun setUpdateInterval(value: String) {
        dataStore.setValue(updateIntervalKey, value)
    }

    val filterStatus = dataStore.getValue(filterStatusKey)

    suspend fun setFilterStatus(value: String) {
        dataStore.setValue(filterStatusKey, value)
    }

    val filterSort = dataStore.getValue(filterSortKey)

    suspend fun setFilterSort(value: String) {
        dataStore.setValue(filterSortKey, value)
    }

    val filterSortDesc = dataStore.getValue(filterSortDescKey)

    suspend fun setFilterSortDesc(value: Boolean) {
        dataStore.setValue(filterSortDescKey, value)
    }

    companion object {
        private val accessTokenKey = stringPreferencesKey("accessToken")
        private val selectedMediaKey = stringSetPreferencesKey("selectedMedia")
        private val updateIntervalKey = stringPreferencesKey("updateInterval")
        private val filterStatusKey = stringPreferencesKey("filterStatus")
        private val filterSortKey = stringPreferencesKey("filterSort")
        private val filterSortDescKey = booleanPreferencesKey("filterSortDesc")
    }
}