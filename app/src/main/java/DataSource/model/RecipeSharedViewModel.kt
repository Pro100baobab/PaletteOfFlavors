package DataSource.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipeSharedViewModel : ViewModel() {
    private val _selectedRecipe = MutableStateFlow<Recipe?>(null)
    val selectedRecipe = _selectedRecipe.asStateFlow()

    fun selectRecipe(recipe: Recipe) {
        viewModelScope.launch {
            _selectedRecipe.emit(recipe)
        }
    }
}