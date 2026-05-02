package com.paletteofflavors.data.local.repository

import com.paletteofflavors.data.local.database.dao.SavedRecipeDao
import com.paletteofflavors.domain.model.NetworkRecipe
import android.util.Log
import com.paletteofflavors.data.local.database.dao.CachedRecipeDao
import com.paletteofflavors.data.local.database.model.CachedRecipeEntity
import com.paletteofflavors.data.local.database.model.SavedRecipeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// TODO: репозиторий --> база данных --> дао
class RecipeRepository(
    private val savedRecipeDao: SavedRecipeDao,
    private val cachedRecipeDao: CachedRecipeDao
) {
    // region <Saved Recipes functions>

    suspend fun insertSaved(recipe: NetworkRecipe) {
        try {
            savedRecipeDao.insert(recipe.toSavedRecipeEntity())
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error saving recipe", e)
            throw e
        }
    }
    suspend fun deleteSaved(recipe: NetworkRecipe){
        savedRecipeDao.delete(recipe.toSavedRecipeEntity())
    }

    fun getAllSavedRecipes(): Flow<List<NetworkRecipe>> = savedRecipeDao.getAllRecipes().map {
        it.map { savedRecipe -> savedRecipe.toNetworkRecipe() }
    }

    suspend fun getSavedRecipeById(id: Int): NetworkRecipe? =
        savedRecipeDao.getRecipeById(id)?.toNetworkRecipe()

    // endregion

    //  region <Caches Recipes functions>

    fun deleteCached() = cachedRecipeDao.cleanCachedRecipesTable()

    fun getAllCachedRecipes(): Flow<List<NetworkRecipe>> = cachedRecipeDao.getAllCachedRecipes().
    map {
        it.map { cachedRecipe -> cachedRecipe.toNetworkRecipe() }
    }

    suspend fun insertCached(recipe: NetworkRecipe) {
        try {
            cachedRecipeDao.insert(recipe.toCachedRecipeEntity())
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error saving recipe", e)
            throw e
        }
    }

    // endregion

    // region <Mappers>

    private fun NetworkRecipe.toSavedRecipeEntity() = SavedRecipeEntity(
        recipeId = recipeId, title = title, ingredients = ingredients,
        instruction = instruction, cookTime = cookTime, complexity = complexity,
        commentsCount = commentsCount, likesCount = likesCount, imageUrl = imageUrl,
        dateTime = dateTime, ownerId = ownerId, mainCategory = mainCategory,
        secondaryCategory = secondaryCategory, isPublic = isPublic,
        likedListOfUsers = likedListOfUsers, savedListOfUsers = savedListOfUsers
    )

    private fun NetworkRecipe.toCachedRecipeEntity() = CachedRecipeEntity(
        recipeId = recipeId, title = title, ingredients = ingredients,
        instruction = instruction, cookTime = cookTime, complexity = complexity,
        commentsCount = commentsCount, likesCount = likesCount, imageUrl = imageUrl,
        dateTime = dateTime, ownerId = ownerId, mainCategory = mainCategory,
        secondaryCategory = secondaryCategory, isPublic = isPublic,
        likedListOfUsers = likedListOfUsers, savedListOfUsers = savedListOfUsers
    )

    private fun SavedRecipeEntity.toNetworkRecipe() = NetworkRecipe(
        recipeId = recipeId, title = title, ingredients = ingredients,
        instruction = instruction, cookTime = cookTime, complexity = complexity,
        commentsCount = commentsCount, likesCount = likesCount, imageUrl = imageUrl,
        dateTime = dateTime, ownerId = ownerId, mainCategory = mainCategory,
        secondaryCategory = secondaryCategory, isPublic = isPublic,
        likedListOfUsers = likedListOfUsers, savedListOfUsers = savedListOfUsers
    )

    private fun CachedRecipeEntity.toNetworkRecipe() = NetworkRecipe(
        recipeId = recipeId, title = title, ingredients = ingredients,
        instruction = instruction, cookTime = cookTime, complexity = complexity,
        commentsCount = commentsCount, likesCount = likesCount, imageUrl = imageUrl,
        dateTime = dateTime, ownerId = ownerId, mainCategory = mainCategory,
        secondaryCategory = secondaryCategory, isPublic = isPublic,
        likedListOfUsers = likedListOfUsers, savedListOfUsers = savedListOfUsers
    )

    // endregion
}