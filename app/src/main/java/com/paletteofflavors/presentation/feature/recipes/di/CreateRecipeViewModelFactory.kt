package com.paletteofflavors.presentation.feature.recipes.di

import com.paletteofflavors.presentation.feature.recipes.viewmodel.CreateRecipeViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.paletteofflavors.data.remote.repository.RecipeRemoteRepository

class CreateRecipeViewModelFactory(
    private val remoteRepository: RecipeRemoteRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateRecipeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateRecipeViewModel(remoteRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
