package com.paletteofflavors.presentation.feature.recipes.viewmodel

import com.paletteofflavors.data.local.database.model.NetworkRecipe
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Хранит данные о последнем выбранном NetworkRecipe рецепте */
class RecipeSharedViewModel : ViewModel() {
    private val _selectedRecipe = MutableStateFlow<NetworkRecipe?>(null)
    val selectedRecipe = _selectedRecipe.asStateFlow()

    fun selectNetworkRecipe(networkRecipe: NetworkRecipe){
        viewModelScope.launch {
            _selectedRecipe.emit(networkRecipe)
        }
    }
}