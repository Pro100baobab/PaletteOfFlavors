package com.paletteofflavors.presentation.auth.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paletteofflavors.data.remote.repository.UserRemoteRepository
import com.paletteofflavors.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegistrationViewModel(
    private val userRemoteRepository: UserRemoteRepository
) : ViewModel() {

    private val _fullName = MutableLiveData<String?>()
    private val _userName = MutableLiveData<String?>()
    private val _email = MutableLiveData<String?>()
    private val _phone = MutableLiveData<String?>()
    private val _password = MutableLiveData<String?>()

    val fullName: LiveData<String?> = _fullName
    val userName: LiveData<String?> = _userName
    val email: LiveData<String?> = _email
    val phone: LiveData<String?> = _phone
    val password: LiveData<String?> = _password

    private val _isUniqueResult = MutableStateFlow<Result<Pair<Boolean, String?>>?>(null)
    val isUniqueResult: StateFlow<Result<Pair<Boolean, String?>>?> = _isUniqueResult

    private val _registrationResult = MutableStateFlow<Result<Boolean>?>(null)
    val registrationResult: StateFlow<Result<Boolean>?> = _registrationResult

    fun checkUnique(username: String, email: String) {
        _isUniqueResult.value = null
        Log.d("RegistrationViewModel", "checkUnique started for $username, $email")
        viewModelScope.launch {
            try {
                val result = userRemoteRepository.checkUniqueUsernameAndEmail(username, email)
                Log.d("RegistrationViewModel", "checkUnique result: $result")
                _isUniqueResult.value = Result.success(result)
            } catch (e: Exception) {
                Log.e("RegistrationViewModel", "checkUnique error: ${e.message}", e)
                _isUniqueResult.value = Result.failure(e)
            }
        }
    }

    fun register() {
        Log.d("RegistrationViewModel", "register() called. Current data: fullName=${_fullName.value}, username=${_userName.value}, email=${_email.value}")
        if (_fullName.value.isNullOrBlank() || _userName.value.isNullOrBlank() || _email.value.isNullOrBlank()) {
            Log.e("RegistrationViewModel", "register() aborted: missing user data!")
            _registrationResult.value = Result.failure(Exception("Missing user data"))
            return
        }
        viewModelScope.launch {
            try {
                val user = User(
                    fullName = _fullName.value ?: "",
                    username = _userName.value ?: "",
                    email = _email.value ?: "",
                    phoneNumber = _phone.value ?: "",
                    passwordHash = (_password.value ?: "").hashCode()
                )
                Log.d("RegistrationViewModel", "Registering user: $user")
                val success = userRemoteRepository.registerUser(user)
                Log.d("RegistrationViewModel", "Registration successful: $success")
                _registrationResult.value = Result.success(success)
            } catch (e: Exception) {
                Log.e("RegistrationViewModel", "Registration error: ${e.message}", e)
                _registrationResult.value = Result.failure(e)
            }
        }
    }

    fun setFullName(name: String) {
        Log.d("RegistrationViewModel", "setFullName: $name")
        _fullName.value = name
    }

    fun setUserName(name: String) {
        Log.d("RegistrationViewModel", "setUserName: $name")
        _userName.value = name
    }

    fun setEmail(eml: String) {
        Log.d("RegistrationViewModel", "setEmail: $eml")
        _email.value = eml
    }

    fun setPhone(phn: String) {
        Log.d("RegistrationViewModel", "setPhone: $phn")
        _phone.value = phn
    }

    fun setPassword(psw: String) {
        Log.d("RegistrationViewModel", "setPassword: [HIDDEN]")
        _password.value = psw
    }

    fun clearResults() {
        Log.d("RegistrationViewModel", "clearResults() called")
        _fullName.value = null
        _userName.value = null
        _email.value = null
        _phone.value = null
        _password.value = null
        _isUniqueResult.value = null
        _registrationResult.value = null
    }

    fun clearUniqueResult() {
        _isUniqueResult.value = null
    }
}
