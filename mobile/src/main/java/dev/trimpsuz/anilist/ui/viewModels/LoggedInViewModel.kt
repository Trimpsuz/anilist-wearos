package dev.trimpsuz.anilist.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.trimpsuz.anilist.utils.DataStoreRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoggedInViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {
    val selectedMediaIds = dataStoreRepository.selectedMedia.map { stringSet ->
        stringSet
            ?.mapNotNull { str -> str.toIntOrNull() }
            ?.toSet()
            ?: emptySet()
    }

    fun setSelectedMediaIds(ids: Set<Int>) {
        viewModelScope.launch {
            dataStoreRepository.setSelectedMedia(ids.map { it.toString() }.toSet())
        }
    }
}