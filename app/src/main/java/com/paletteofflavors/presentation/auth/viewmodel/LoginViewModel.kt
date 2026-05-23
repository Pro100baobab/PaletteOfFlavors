package com.paletteofflavors.presentation.auth.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paletteofflavors.data.remote.repository.UserRemoteRepository
import com.paletteofflavors.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val userRemoteRepository: UserRemoteRepository
) : ViewModel() {

    private val _username = MutableLiveData<String>()
    private val _password = MutableLiveData<String>()
    private val _email = MutableLiveData<String>()
    private val _resetemail = MutableLiveData<String>()
    private val _resetphone = MutableLiveData<String>()
    private val _typeOfConnection = MutableLiveData<String>()

    val username: LiveData<String> = _username
    val password: LiveData<String> = _password
    val curemail: LiveData<String> = _email
    val resetemail: LiveData<String> = _resetemail
    val resetphone: LiveData<String> = _resetphone
    val typeOfVConnection: LiveData<String> = _typeOfConnection

    private val _loginResult = MutableStateFlow<Result<User?>>(Result.success(null))
    val loginResult: StateFlow<Result<User?>> = _loginResult

    private val _findUserResult = MutableStateFlow<Result<String?>>(Result.success(null))
    val findUserResult: StateFlow<Result<String?>> = _findUserResult

    private val _resetPasswordResult = MutableStateFlow<Result<Boolean>>(Result.success(false))
    val resetPasswordResult: StateFlow<Result<Boolean>> = _resetPasswordResult

    fun login(username: String, passwordHash: Int) {
        viewModelScope.launch {
            try {
                val user = userRemoteRepository.loginUser(username, passwordHash)
                _loginResult.value = Result.success(user)
            } catch (e: Exception) {
                _loginResult.value = Result.failure(e)
            }
        }
    }

    fun findUserByEmail(email: String) {
        viewModelScope.launch {
            try {
                val phone = userRemoteRepository.findUserByEmail(email)
                _findUserResult.value = Result.success(phone)
            } catch (e: Exception) {
                _findUserResult.value = Result.failure(e)
            }
        }
    }

    fun setNewPassword(email: String, phone: String, passwordHash: Int) {
        viewModelScope.launch {
            try {
                val success = userRemoteRepository.updatePassword(email, phone, passwordHash)
                _resetPasswordResult.value = Result.success(success)
            } catch (e: Exception) {
                _resetPasswordResult.value = Result.failure(e)
            }
        }
    }

    fun setUserName(usrname: String) {
        _username.value = usrname
    }

    fun setPassword(pswd: String) {
        _password.value = pswd
    }

    fun setEmail(eml: String) {
        _email.value = eml
    }

    fun setResetEmail(eml: String) {
        _resetemail.value = eml
    }

    fun setResetPhone(phn: String) {
        _resetphone.value = phn
    }

    fun setTypeOfConnection(type: String) {
        _typeOfConnection.value = type
    }

    fun clearResults() {
        _loginResult.value = Result.success(null)
        _findUserResult.value = Result.success(null)
        _resetPasswordResult.value = Result.success(false)
    }

    fun clearUserData() {
        _username.value = ""
        _password.value = ""
    }
}
