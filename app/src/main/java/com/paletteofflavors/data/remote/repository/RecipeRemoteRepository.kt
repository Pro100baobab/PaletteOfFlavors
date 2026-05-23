package com.paletteofflavors.data.remote.repository

import com.paletteofflavors.data.remote.api.turso.Turso
import com.paletteofflavors.data.remote.api.turso.queries.RecipeQueries
import com.paletteofflavors.domain.exception.NoInternetException
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
        ifConnected { turso.getAllNetworkRecipes(RecipeQueries.ALL_RECIPES) }

    suspend fun searchByMainCategory(category: String): List<NetworkRecipe> =
        ifConnected { turso.getAllNetworkRecipes(RecipeQueries.byMainCategory(category)) }

    suspend fun searchBySecondaryCategory(category: String): List<NetworkRecipe> =
        ifConnected { turso.getAllNetworkRecipes(RecipeQueries.bySecondaryCategory(category)) }

    suspend fun searchByIngredients(ingredients: List<String>): List<NetworkRecipe> =
        ifConnected { turso.getAllNetworkRecipes(RecipeQueries.byIngredients(ingredients)) }

    suspend fun searchByTitleOrIngredient(words: List<String>): List<NetworkRecipe> =
        ifConnected { turso.getAllNetworkRecipes(RecipeQueries.searchByTitleOrIngredient(words)) }
}
