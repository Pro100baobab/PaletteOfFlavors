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

    private val _fullName = MutableLiveData<String>()
    private val _userName = MutableLiveData<String>()
    private val _email = MutableLiveData<String>()
    private val _phone = MutableLiveData<String>()
    private val _password = MutableLiveData<String>()

    val fullName: LiveData<String> = _fullName
    val userName: LiveData<String> = _userName
    val email: LiveData<String> = _email
    val phone: LiveData<String> = _phone
    val password: LiveData<String> = _password

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
        viewModelScope.launch {
            try {
                val user = User(
                    fullName = _fullName.value ?: "",
                    username = _userName.value ?: "",
                    email = _email.value ?: "",
                    phoneNumber = _phone.value ?: "",
                    passwordHash = (_password.value ?: "").hashCode()
                )
                val success = userRemoteRepository.registerUser(user)
                _registrationResult.value = Result.success(success)
            } catch (e: Exception) {
                _registrationResult.value = Result.failure(e)
            }
        }
    }

    fun setFullName(name: String) {
        _fullName.value = name
    }

    fun setUserName(name: String) {
        _userName.value = name
    }

    fun setEmail(eml: String) {
        _email.value = eml
    }

    fun setPhone(phn: String) {
        _phone.value = phn
    }

    fun setPassword(psw: String) {
        _password.value = psw
    }

    fun clearResults() {
        _fullName.value = ""
        _userName.value = ""
        _email.value = ""
        _phone.value = ""
        _password.value = ""
        _isUniqueResult.value = null
        _registrationResult.value = null
    }

    fun clearUniqueResult() {
        _isUniqueResult.value = null
    }
}
