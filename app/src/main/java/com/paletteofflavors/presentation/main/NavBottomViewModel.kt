package com.paletteofflavors.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NavBottomViewModel: ViewModel() {
    private val _selectedNavItem = MutableLiveData<Int>()
    val selectedNavItem: LiveData<Int> = _selectedNavItem

    private val _isContentVisible = MutableLiveData<Boolean>()
    val isContentVisible: LiveData<Boolean> = _isContentVisible

    fun setSelectedNavItem(itemId: Int) {
        _selectedNavItem.value = itemId
    }

    fun setIsContentVisible(flag: Boolean) {
        _isContentVisible.value = flag
    }

}