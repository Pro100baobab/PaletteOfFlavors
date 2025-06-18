package com.paletteofflavors.presentation.feature.recipes.viewmodel

import com.paletteofflavors.data.local.database.model.NetworkRecipe
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paletteofflavors.data.local.database.model.Recipe
import com.paletteofflavors.data.local.database.model.SavedRecipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Хранит данные о последнем выбранном рецепте для каждого класс рецептов
// Унифицированное хранилище для предоставления данных в RecipeDetailsFragment/NetworkRecipeDetailsFragment
class RecipeSharedViewModel : ViewModel() {

    // Для актуального экземпляра своих рецептов
    private val _selectedRecipe = MutableStateFlow<Recipe?>(null)
    val selectedRecipe = _selectedRecipe.asStateFlow()

    // Для актуального экземпляра сетевых рецептов
    private val _selectedNetworkRecipe = MutableStateFlow<NetworkRecipe?>(null)
    val selectedNetworkRecipe = _selectedNetworkRecipe.asStateFlow()

    // Для актуального экземпляра сохраненных рецептов
    private val _selectedSavedRecipe = MutableStateFlow<SavedRecipe?>(null)
    val selectedSavedRecipe = _selectedSavedRecipe.asStateFlow()



    // Функции обновления MutableStateFlow значений
    fun selectRecipe(recipe: Recipe) {
        viewModelScope.launch {
            _selectedRecipe.emit(recipe)
        }
    }

    fun selectNetworkRecipe(networkRecipe: NetworkRecipe){
        viewModelScope.launch {
            _selectedNetworkRecipe.emit(networkRecipe)
        }
    }

    fun selectSavedRecipe(savedRecipe: SavedRecipe){
        viewModelScope.launch {
            _selectedSavedRecipe.emit(savedRecipe)
        }
    }
}