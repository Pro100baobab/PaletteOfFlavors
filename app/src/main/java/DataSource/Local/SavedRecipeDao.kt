package DataSource.Local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import domain.SavedRecipe
import kotlinx.coroutines.flow.Flow


@Dao
interface SavedRecipeDao {
    @Insert
    suspend fun insert(recipe: SavedRecipe)

    @Delete
    suspend fun delete(recipe: SavedRecipe)

    @Query("SELECT * FROM savedRecipes")
    fun getAllRecipes(): Flow<List<SavedRecipe>> // Flow для автоматических обновлений

    @Query("SELECT * FROM savedRecipes WHERE recipeId = :id")
    suspend fun getRecipeById(id: Int): SavedRecipe?
}