package com.paletteofflavors.presentation.feature.main.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.paletteofflavors.data.remote.repository.RecipeRemoteRepository
import com.paletteofflavors.data.remote.repository.UserRemoteRepository
import com.paletteofflavors.presentation.feature.main.viewmodel.ProfileViewModel

class ProfileViewModelFactory(
    private val recipeRepository: RecipeRemoteRepository,
    private val userRepository: UserRemoteRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(recipeRepository, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
