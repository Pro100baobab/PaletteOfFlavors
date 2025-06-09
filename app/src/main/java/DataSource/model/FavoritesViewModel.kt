package DataSource.model

import DataSource.Local.RecipeDao
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.Recipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val recipeDao: RecipeDao
) : ViewModel() {

    private val _radioButtonId = MutableLiveData<Int>()
    val radioButton get() = _radioButtonId

    fun setRadioButtonId(id: Int) {
        _radioButtonId.value = id
    }

    // Get all recipes from local database as Flow
    val myRecipes: Flow<List<Recipe>> = recipeDao.getAllRecipes()

    suspend fun getRecipeCount(): Int{
        return myRecipes.count()
    }

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