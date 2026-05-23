package com.paletteofflavors.presentation.auth.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.paletteofflavors.data.remote.repository.UserRemoteRepository
import com.paletteofflavors.presentation.auth.viewmodel.RegistrationViewModel

class RegistrationViewModelFactory(
    private val userRemoteRepository: UserRemoteRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegistrationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegistrationViewModel(userRemoteRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
