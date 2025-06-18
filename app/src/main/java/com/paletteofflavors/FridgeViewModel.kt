package com.paletteofflavors

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// For control selected ingredients in filter
class FridgeViewModel : ViewModel() {

    private val _listOfingredients = MutableLiveData<MutableList<String>>(mutableListOf())
    val listOfSelectedIngredients: LiveData<MutableList<String>> = _listOfingredients


    fun addIngredient(name: String) {
        val currentList = _listOfingredients.value ?: mutableListOf()
        val newList = currentList.toMutableList().apply { add(name) }
        _listOfingredients.value = newList
    }

    fun removeIngredient(name: String) {
        val currentList = _listOfingredients.value ?: mutableListOf()
        val newList = currentList.toMutableList().apply { remove(name) }
        _listOfingredients.value = newList
    }

    fun getselectedIngredientsCount(): Int {
        return _listOfingredients.value?.size ?: 0
    }
}