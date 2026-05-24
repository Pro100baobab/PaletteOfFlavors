package com.paletteofflavors.presentation.feature.main.viewmodel

import com.paletteofflavors.domain.model.NetworkRecipe
import com.paletteofflavors.data.local.repository.RecipeRepository
import com.paletteofflavors.data.remote.repository.RecipeRemoteRepository
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val repository: RecipeRepository,
    private val remoteRepository: RecipeRemoteRepository? = null
) : ViewModel() {

    // For checking current group of recipes
    private val _radioButtonId = MutableLiveData<Int>()
    val radioButton get() = _radioButtonId

    fun setRadioButtonId(id: Int) {
        _radioButtonId.value = id
    }

    // Get all recipes from local database (Saved_recipes) as Flow
    val savedRecipes: Flow<List<NetworkRecipe>> = repository.getAllSavedRecipes()

    private val _myRecipes = MutableStateFlow<List<NetworkRecipe>>(emptyList())
    val myRecipes: StateFlow<List<NetworkRecipe>> = _myRecipes

    fun fetchMyRecipes(userId: Int) {
        viewModelScope.launch {
            try {
                val recipes = remoteRepository?.getUserRecipes(userId) ?: emptyList()
                _myRecipes.value = recipes
            } catch (e: Exception) {
                Log.e("FavoritesViewModel", "Error fetching my recipes", e)
            }
        }
    }

    fun deleteOwnRecipe(recipe: NetworkRecipe) {
        viewModelScope.launch {
            try {
                val success = remoteRepository?.deleteRecipe(recipe.recipeId) ?: false
                if (success) {
                    _myRecipes.value = _myRecipes.value.filter { it.recipeId != recipe.recipeId }
                }
            } catch (e: Exception) {
                Log.e("FavoritesViewModel", "Error deleting own recipe", e)
            }
        }
    }

    // Functions for using with saved recipes state
    fun addSavedRecipe(recipe: NetworkRecipe){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("FavoritesViewModel", "Adding recipe: ${recipe.title}")
                repository.insertSaved(recipe)
                Log.d("FavoritesViewModel", "Recipe added successfully")
            } catch (e: Exception) {
                Log.e("FavoritesViewModel", "Error adding recipe", e)
            }
        }
    }

    fun deleteSavedRecipe(recipe: NetworkRecipe){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSaved(recipe)
        }
    }

    fun isRecipeSaved(id: Int): Flow<Boolean> = flow {
        emit(repository.getSavedRecipeById(id) != null)
    }.flowOn(Dispatchers.IO)

    // TODO: Методы используются вне класса и не имеют отношение к сохраненным и собственным
    //  рецептам (для кэшированных); целесообразно вынести их из класса FavoritesViewModel.

    // region <Repository operations with cached Recipes>
    // Get all recipes from local database (Cached_recipes) as Flow
    val cashedRecipes: Flow<List<NetworkRecipe>> = repository.getAllCachedRecipes()

    fun deleteCashRecipes(){
        repository.deleteCached()
    }

    fun addCashedRecipe(recipe: NetworkRecipe?){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insertCached(recipe!!)
            } catch (e: Exception) {
                Log.e("FavoritesViewModel", "Error adding cached recipe", e)
            }
        }
    }
    // endregion
}
