package dev.trimpsuz.anilist.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.ApolloClient
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.trimpsuz.anilist.GetViewerQuery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewerViewModel @Inject constructor(
    private val client: ApolloClient
) : ViewModel() {
    private val _viewer = MutableStateFlow<GetViewerQuery.Viewer?>(null)
    val viewer: StateFlow<GetViewerQuery.Viewer?> = _viewer

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        if(_viewer.value == null) fetchViewer()
    }

    fun fetchViewer() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = client.query(GetViewerQuery()).execute()
                _viewer.value = response.data?.Viewer
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}