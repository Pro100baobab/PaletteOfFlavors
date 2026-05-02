package com.paletteofflavors.presentation.feature.main.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.paletteofflavors.data.remote.repository.RecipeRemoteRepository
import com.paletteofflavors.presentation.feature.main.viewmodel.SearchViewModel

class SearchViewModelFactory(
    private val repository: RecipeRemoteRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}