package com.paletteofflavors.logIn.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RegistrationViewModel: ViewModel() {

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


    // Functions for update MutableLiveData values
    fun setFullName(name: String){
        _fullName.value = name
    }

    fun setUserName(name: String){
        _userName.value = name
    }

    fun setEmail(eml: String){
        _email.value = eml
    }

    fun setPhone(phn: String){
        _phone.value = phn
    }

    fun setPassword(psw: String){
        _password.value = psw
    }
}