package Repositories

import DataSource.Local.RecipeDao
import DataSource.Local.SavedRecipeDao
import DataSource.Network.NetworkRecipe
import android.util.Log
import domain.Recipe
import domain.SavedRecipe
import kotlinx.coroutines.flow.Flow

class RecipeRepository(
    private val userRecipeDao: RecipeDao,
    private val savedRecipeDao: SavedRecipeDao,
    //private val turso: Turso
) {
    // Собственные рецепты
    suspend fun insertOwn(recipe: Recipe) = userRecipeDao.insert(recipe)
    suspend fun deleteOwn(recipe: Recipe) = userRecipeDao.delete(recipe)
    fun getAllUsersRecipes(): Flow<List<Recipe>> = userRecipeDao.getAllRecipes()

    // Сохраненные рецепты
    //suspend fun insertSaved(recipe: SavedRecipe) = savedRecipeDao.insert(recipe)
    suspend fun insertSaved(recipe: SavedRecipe) {
        try {
            savedRecipeDao.insert(recipe)
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error saving recipe", e)
            throw e // или обработайте ошибку по-другому
        }
    }


    suspend fun deleteSaved(recipe: SavedRecipe) = savedRecipeDao.delete(recipe)
    fun getAllSavedRecipes(): Flow<List<SavedRecipe>> = savedRecipeDao.getAllRecipes()

   /*
    // Специфичные операции для Turso
    suspend fun fetchFromTurso(): List<NetworkRecipe> {
        val recipes = turso.getAllNetworkRecipes()
        // Сохраняем в локальную базу
        recipes.forEach { recipe ->
            networkRecipeDao.insert(recipe.toEntity())
        }
        return recipes
    }
    */

}


// Конвертер из NetworkRecipe в SavedRecipe
fun NetworkRecipe.toSavedRecipe() = SavedRecipe(
    recipeId = this.recipeId,
    title = this.title,
    ingredients = this.ingredients,
    instruction = this.instruction,
    cookTime = this.cookTime,
    complexity = this.complexity,
    commentsCount = this.commentsCount,
    likesCount = this.likesCount,
    imageUrl = this.imageUrl,
    dateTime = this.dateTime,
    ownerId = this.ownerId
)

fun SavedRecipe.toNetworkRecipe() = NetworkRecipe(
    recipeId = this.recipeId,
    title = this.title,
    ingredients = this.ingredients,
    instruction = this.instruction,
    cookTime = this.cookTime,
    complexity = this.complexity,
    commentsCount = this.commentsCount,
    likesCount = this.likesCount,
    imageUrl = this.imageUrl,
    dateTime = this.dateTime,
    ownerId = this.ownerId
)

