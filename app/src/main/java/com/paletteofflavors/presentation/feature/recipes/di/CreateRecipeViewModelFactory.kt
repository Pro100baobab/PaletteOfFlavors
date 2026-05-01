package com.paletteofflavors.presentation.feature.recipes.di

import com.paletteofflavors.presentation.feature.recipes.viewmodel.CreateRecipeViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CreateRecipeViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateRecipeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateRecipeViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}