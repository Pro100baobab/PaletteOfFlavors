package com.paletteofflavors.presentation.auth.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.paletteofflavors.data.remote.repository.UserRemoteRepository
import com.paletteofflavors.presentation.auth.viewmodel.LoginViewModel


class LoginViewModelFactory(
    private val userRemoteRepository: UserRemoteRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(userRemoteRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
