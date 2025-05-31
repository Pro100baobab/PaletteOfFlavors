package DataSource.model

import DataSource.Local.RecipeDao
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.Recipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val recipeDao: RecipeDao
) : ViewModel() {

    private val _radioHint = MutableLiveData<String>()
    val radioHint get() = _radioHint

    private fun setRadioHint(name: String) {
        _radioHint.value = name
    }

    // Get all recipes from local database as Flow
    val myRecipes: Flow<List<Recipe>> = recipeDao.getAllRecipes()

    fun addRecipes(recipe: Recipe){
        viewModelScope.launch {
            recipeDao.insert(recipe)
        }
    }

    fun deleteRecipes(recipe: Recipe){
        viewModelScope.launch {
            recipeDao.delete(recipe)
        }
    }
}