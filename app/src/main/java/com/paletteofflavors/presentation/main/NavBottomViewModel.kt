package com.paletteofflavors.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NavBottomViewModel: ViewModel() {
    private val _selectedNavItem = MutableLiveData<Int>()
    private val _isContentVisible = MutableLiveData<Boolean>()

    val isContentVisible: LiveData<Boolean> = _isContentVisible
    val selectedNavItem: LiveData<Int> = _selectedNavItem

    fun setSelectedNavItem(itemId: Int) {
        _selectedNavItem.value = itemId
    }

    fun setIsContentVisible(flag: Boolean) {
        _isContentVisible.value = flag
    }
}