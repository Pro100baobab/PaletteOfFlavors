package DataSource.model

import DataSource.Local.RecipeDao
import DataSource.Local.SavedRecipeDao
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.Recipe
import domain.savedRecipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val recipeDao: RecipeDao,
    private val savedRecipeDao: SavedRecipeDao
) : ViewModel() {

    private val _radioButtonId = MutableLiveData<Int>()
    val radioButton get() = _radioButtonId

    fun setRadioButtonId(id: Int) {
        _radioButtonId.value = id
    }

    // Get all recipes from local database (User_recipe) as Flow
    val myRecipes: Flow<List<Recipe>> = recipeDao.getAllRecipes()

    suspend fun getRecipes(): List<Recipe> {
        return myRecipes.first()
    }
    suspend fun getRecipeCount(): Int{
        return myRecipes.count()
    }
    fun addRecipe(recipe: Recipe){
        viewModelScope.launch {
            recipeDao.insert(recipe)
        }
    }
    fun deleteRecipe(recipe: Recipe){
        viewModelScope.launch {
            recipeDao.delete(recipe)
        }
    }



    // Get all recipes from local database (User_recipe) as Flow
    val savedRecipes: Flow<List<savedRecipe>> = savedRecipeDao.getAllRecipes()

    suspend fun getSavedRecipes(): List<savedRecipe> {
        return savedRecipes.first()
    }
    suspend fun getSavedRecipeCount(): Int{
        return savedRecipes.count()
    }
    fun addSavedRecipe(recipe: savedRecipe){
        viewModelScope.launch {
            savedRecipeDao.insert(recipe)
        }
    }
    fun deleteSavedRecipe(recipe: savedRecipe){
        viewModelScope.launch {
            savedRecipeDao.delete(recipe)
        }
    }


}