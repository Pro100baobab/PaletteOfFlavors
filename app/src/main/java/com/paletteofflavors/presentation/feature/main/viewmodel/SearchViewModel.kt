package com.paletteofflavors.presentation.feature.main.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paletteofflavors.data.remote.repository.RecipeRemoteRepository
import com.paletteofflavors.domain.exception.NoInternetException
import com.paletteofflavors.domain.model.NetworkRecipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class SearchViewModel(
    private val remoteRepository: RecipeRemoteRepository
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<NetworkRecipe>>(emptyList())
    val searchResults: StateFlow<List<NetworkRecipe>> = _searchResults

    // Канал для ошибок, не связанных с результатами
    private val _errorEvent = MutableStateFlow<String?>(null)
    val errorEvent: StateFlow<String?> = _errorEvent

    // Флаг, что последний запрос завершился неудачей из-за сети
    private val _isNetworkError = MutableStateFlow(false)
    val isNetworkError: StateFlow<Boolean> = _isNetworkError

    fun searchAll() {
        executeWithFallback {
            remoteRepository.getAllRecipes()
        }

    }

    fun searchByMainCategory(category: String) {
        executeWithFallback {
            remoteRepository.searchByMainCategory(category)
        }
    }

    fun searchBySecondaryCategory(category: String) {
        executeWithFallback {
            remoteRepository.searchBySecondaryCategory(category)
        }
    }

    fun searchByIngredients(ingredients: List<String>) {
        executeWithFallback {
            remoteRepository.searchByIngredients(ingredients)
        }
    }

    fun searchByTitleOrIngredient(words: List<String>) {
        executeWithFallback {
            remoteRepository.searchByTitleOrIngredient(words)
        }
    }

    private fun executeWithFallback(block: suspend () -> List<NetworkRecipe>) {
        viewModelScope.launch {
            try {
                _searchResults.value = block()
                _isNetworkError.value = false
                _errorEvent.value = null
            } catch (e: NoInternetException) {
                Log.w("SearchViewModel", "No internet – will use cache")
                _isNetworkError.value = true
                _errorEvent.value = "No internet"
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error fetching recipes", e)
                _errorEvent.value = e.message
                _isNetworkError.value = false
            }
        }
    }
}