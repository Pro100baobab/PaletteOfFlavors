package com.paletteofflavors.logIn.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginViewModel: ViewModel() {

    private val _useername = MutableLiveData<String>()
    private val _password = MutableLiveData<String>()
    private val _email = MutableLiveData<String>()
    private val _resetemail = MutableLiveData<String>()
    private val _resetphone = MutableLiveData<String>()
    private val _typeOfConnection = MutableLiveData<String>()

    val username: LiveData<String> = _useername
    val password: LiveData<String> = _password
    val curemail: LiveData<String> = _email
    val resetemail: LiveData<String> = _resetemail
    val resetphone: LiveData<String> = _resetphone
    val typeOfVConnection: LiveData<String> = _typeOfConnection


    // Functions for update MutableLiveData values
    fun setUserName(usrname: String) {
        _useername.value = usrname
    }

    fun setPassword(pswd: String){
        _password.value = pswd
    }

    fun setEmail(eml: String){
        _email.value = eml
    }

    fun setResetEmail(eml: String){
        _resetemail.value = eml
    }

    fun setResetPhone(phn: String){
        _resetphone.value = phn
    }

    fun setTypeOfConnection(type: String){
        _typeOfConnection.value = type
    }
}