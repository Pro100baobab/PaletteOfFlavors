package com.paletteofflavors.logIn.viewmodels

import DataSource.API.ApiResponse
import DataSource.API.EmailRequest
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/*
class AuthViewModel : ViewModel() {

    fun sendVerificationCode(email: String, code: String): Flow<ApiResponse> = flow  {
        try {
            val response = ApiClient.emailApi.sendVerificationCode(EmailRequest(email, code))
            emit(response.body() ?: ApiResponse(false, "Empty response"))

        } catch (e: Exception) {
            emit(ApiResponse(false, e.message))
        }
    }
}*/