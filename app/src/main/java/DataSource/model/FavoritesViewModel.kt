package DataSource.model

import DataSource.Local.RecipeDao
import DataSource.Local.SavedRecipeDao
import DataSource.Network.NetworkRecipe
import Repositories.RecipeRepository
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.Recipe
import domain.SavedRecipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class FavoritesViewModel(
    //private val recipeDao: RecipeDao,
    //private val savedRecipeDao: SavedRecipeDao
    private val repository: RecipeRepository
) : ViewModel() {

    private val _radioButtonId = MutableLiveData<Int>()
    val radioButton get() = _radioButtonId

    fun setRadioButtonId(id: Int) {
        _radioButtonId.value = id
    }

    // Get all recipes from local database (User_recipe) as Flow
    val myRecipes: Flow<List<Recipe>> = repository.getAllUsersRecipes()

    suspend fun getRecipes(): List<Recipe> {
        return myRecipes.first()
    }
    suspend fun getRecipeCount(): Int{
        return myRecipes.count()
    }
    fun addRecipe(recipe: Recipe){
        viewModelScope.launch {
            repository.insertOwn(recipe)
        }
    }
    fun deleteRecipe(recipe: Recipe){
        viewModelScope.launch {
            repository.deleteOwn(recipe)
        }
    }



    // Get all recipes from local database (User_recipe) as Flow
    val savedRecipes: Flow<List<SavedRecipe>> = repository.getAllSavedRecipes()

    suspend fun getSavedRecipes(): List<SavedRecipe> {
        return savedRecipes.first()
    }
    suspend fun getSavedRecipeCount(): Int{
        return savedRecipes.count()
    }
    fun addSavedRecipe(recipe: SavedRecipe){
        viewModelScope.launch(Dispatchers.IO) {
            //repository.insertSaved(recipe)

            try {
                Log.d("FavoritesViewModel", "Adding recipe: ${recipe.title}")
                repository.insertSaved(recipe)
                Log.d("FavoritesViewModel", "Recipe added successfully")
            } catch (e: Exception) {
                Log.e("FavoritesViewModel", "Error adding recipe", e)
            }
        }

    }
    fun deleteSavedRecipe(recipe: SavedRecipe){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSaved(recipe)
        }
    }

    fun isRecipeSaved(id: Int): Flow<Boolean> = flow {
        emit(repository.getSavedRecipeById(id) != null)
    }.flowOn(Dispatchers.IO)



    val cashedRecipes: Flow<List<NetworkRecipe>> = repository.getAllCashedRecipes()

    fun deleteCashRecipes(){
        repository.deleteCash()
    }

    fun addCashedRecipe(recipe: NetworkRecipe?){
        viewModelScope.launch(Dispatchers.IO) {

            Log.d("FavoritesViewModel", "получен ${recipe?.title}")
            try {
                Log.d("FavoritesViewModel", "Adding recipe: ${recipe?.title}")
                repository.insertCashed(recipe!!)
                Log.d("FavoritesViewModel", "Recipe added successfully")
            } catch (e: Exception) {
                Log.e("FavoritesViewModel", "Error adding recipe", e)
            }
        }

    }
}