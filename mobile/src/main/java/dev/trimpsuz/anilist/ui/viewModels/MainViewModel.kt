package dev.trimpsuz.anilist.ui.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.trimpsuz.anilist.utils.DataStoreRepository
import dev.trimpsuz.anilist.utils.GlobalVariables
import dev.trimpsuz.anilist.utils.sendToWear
import kotlinx.coroutines.Dispatchers
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
    val theme = dataStoreRepository.theme
    val isLoggedIn = dataStoreRepository.isLoggedIn

    fun setToken(token: String?) {
        globalVariables.accessToken = token
    }

    fun saveToken(token: String, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            sendToWear("accessToken", token, context)
            dataStoreRepository.setAccessToken(token)
        }
    }

    fun setTheme(value: String) {
        viewModelScope.launch {
            dataStoreRepository.setTheme(value)
        }
    }

    init {
        accessToken
            .onEach { setToken(it) }
            .launchIn(viewModelScope)
    }
}