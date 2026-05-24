package com.paletteofflavors.presentation.feature.main.di

import com.paletteofflavors.data.local.repository.RecipeRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.paletteofflavors.data.remote.repository.RecipeRemoteRepository
import com.paletteofflavors.presentation.feature.main.viewmodel.FavoritesViewModel

class FavoritesViewModelFactory(
    private val repository: RecipeRepository,
    private val remoteRepository: RecipeRemoteRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoritesViewModel(repository, remoteRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
