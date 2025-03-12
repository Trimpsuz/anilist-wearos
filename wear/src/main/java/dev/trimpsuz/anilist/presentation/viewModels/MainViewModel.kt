package dev.trimpsuz.anilist.presentation.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.trimpsuz.anilist.utils.DataStoreRepository
import dev.trimpsuz.anilist.utils.GlobalVariables
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val globalVariables: GlobalVariables,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {
    val accessToken = dataStoreRepository.accessToken
    val isLoggedIn = dataStoreRepository.isLoggedIn
    val filterStatus = dataStoreRepository.filterStatus
    val filterSort = dataStoreRepository.filterSort
    val filterSortDesc = dataStoreRepository.filterSortDesc

    fun setToken(token: String?) {
        globalVariables.accessToken = token
    }

    fun setFilterStatus(value: String) {
        viewModelScope.launch {
            dataStoreRepository.setFilterStatus(value)
        }
    }

    fun setFilterSort(value: String) {
        viewModelScope.launch {
            dataStoreRepository.setFilterSort(value)
        }
    }

    fun setFilterSortDesc(value: Boolean) {
        viewModelScope.launch {
            dataStoreRepository.setFilterSortDesc(value)
        }
    }

    init {
        accessToken
            .onEach { setToken(it) }
            .launchIn(viewModelScope)
    }
}