package DataSource.model

import DataSource.Network.NetworkRecipe
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipeSharedViewModel : ViewModel() {

    // Для актуального списка своих рецептов
    private val _selectedRecipe = MutableStateFlow<Recipe?>(null)
    val selectedRecipe = _selectedRecipe.asStateFlow()

    // Для актуального списка сетевых рецептов, полученных любым запросом
    private val _selectedNetworkRecipe = MutableStateFlow<NetworkRecipe?>(null)
    val selectedNetworkRecipe = _selectedNetworkRecipe.asStateFlow()

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
}