package com.paletteofflavors.data.remote.repository

import com.paletteofflavors.data.remote.api.turso.Turso
import com.paletteofflavors.data.remote.api.turso.queries.RecipeQueries
import com.paletteofflavors.domain.exception.NoInternetException
import com.paletteofflavors.domain.model.Comment
import com.paletteofflavors.domain.model.NetworkRecipe
import com.paletteofflavors.domain.utils.InternetChecker

class RecipeRemoteRepository(
    private val turso: Turso,
    private val internetChecker: InternetChecker
) {
    private suspend fun <T> ifConnected(block: suspend () -> T): T {
        if (!internetChecker.isConnected()) {
            throw NoInternetException()
        }
        return block()
    }

    suspend fun getAllRecipes(): List<NetworkRecipe> =
        ifConnected { turso.getAllNetworkRecipes(RecipeQueries.ALL_PUBLIC_RECIPES) }

    suspend fun searchByMainCategory(category: String): List<NetworkRecipe> =
        ifConnected { turso.getAllNetworkRecipes(RecipeQueries.byMainCategory(category)) }

    suspend fun searchBySecondaryCategory(category: String): List<NetworkRecipe> =
        ifConnected { turso.getAllNetworkRecipes(RecipeQueries.bySecondaryCategory(category)) }

    suspend fun searchByIngredients(ingredients: List<String>): List<NetworkRecipe> =
        ifConnected { turso.getAllNetworkRecipes(RecipeQueries.byIngredients(ingredients)) }

    suspend fun searchByTitleOrIngredient(words: List<String>): List<NetworkRecipe> =
        ifConnected { turso.getAllNetworkRecipes(RecipeQueries.searchByTitleOrIngredient(words)) }

    suspend fun getUserRecipes(userId: Int, onlyPublic: Boolean = false): List<NetworkRecipe> =
        ifConnected { turso.getAllNetworkRecipes(RecipeQueries.getUserRecipes(userId, onlyPublic)) }

    suspend fun saveRecipe(recipe: NetworkRecipe, ownerId: Int): Boolean =
        ifConnected { turso.saveRecipe(recipe, ownerId) }

    suspend fun deleteRecipe(recipeId: Int): Boolean =
        ifConnected { turso.deleteRecipe(recipeId) }

    suspend fun addComment(recipeId: Int, userId: Int, text: String): Boolean =
        ifConnected { turso.addComment(recipeId, userId, text) }

    suspend fun getComments(recipeId: Int): List<Comment> =
        ifConnected { turso.getComments(recipeId) }

    suspend fun updateLikes(recipeId: Int, likedList: List<Int>): Boolean =
        ifConnected { turso.updateLikes(recipeId, likedList) }

    suspend fun updateSaved(recipeId: Int, savedList: List<Int>): Boolean =
        ifConnected { turso.updateSaved(recipeId, savedList) }
}
