package com.paletteofflavors.data.local.repository

import com.paletteofflavors.data.local.database.dao.CashDao
import com.paletteofflavors.data.local.database.dao.RecipeDao
import com.paletteofflavors.data.local.database.dao.SavedRecipeDao
import com.paletteofflavors.data.local.database.model.NetworkRecipe
import android.util.Log
import com.paletteofflavors.data.local.database.model.Recipe
import com.paletteofflavors.data.local.database.model.SavedRecipe
import kotlinx.coroutines.flow.Flow

class RecipeRepository(
    private val userRecipeDao: RecipeDao,
    private val savedRecipeDao: SavedRecipeDao,
    private val cashRecipeDao: CashDao
) {
    // Собственные рецепты
    suspend fun insertOwn(recipe: Recipe) = userRecipeDao.insert(recipe)
    suspend fun deleteOwn(recipe: Recipe) = userRecipeDao.delete(recipe)
    fun getAllUsersRecipes(): Flow<List<Recipe>> = userRecipeDao.getAllRecipes()

    // Сохраненные рецепты
    suspend fun insertSaved(recipe: SavedRecipe) {
        try {
            savedRecipeDao.insert(recipe)
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error saving recipe", e)
            throw e
        }
    }
    suspend fun deleteSaved(recipe: SavedRecipe) = savedRecipeDao.delete(recipe)
    fun getAllSavedRecipes(): Flow<List<SavedRecipe>> = savedRecipeDao.getAllRecipes()
    suspend fun getSavedRecipeById(id: Int) = savedRecipeDao.getRecipeById(id)


    //  Кешированные рецепты
    fun deleteCash() = cashRecipeDao.cleanCashTable()
    fun getAllCashedRecipes(): Flow<List<NetworkRecipe>> = cashRecipeDao.getAllCashRecipes()
    suspend fun insertCashed(recipe: NetworkRecipe) {
        try {
            cashRecipeDao.insert(recipe)
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error saving recipe", e)
            throw e
        }
    }
}