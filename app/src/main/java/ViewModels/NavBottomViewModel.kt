package ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NavBottomViewModel: ViewModel() {
    private val _selectedNavItem = MutableLiveData<Int>()
    val selectedNavItem: LiveData<Int> = _selectedNavItem

    fun setSelectedNavItem(itemId: Int) {
        _selectedNavItem.value = itemId
    }
}