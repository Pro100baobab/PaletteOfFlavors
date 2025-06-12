package DataSource.Local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import domain.savedRecipe
import kotlinx.coroutines.flow.Flow


@Dao
interface SavedRecipeDao {
    @Insert
    suspend fun insert(recipe: savedRecipe)

    @Delete
    suspend fun delete(recipe: savedRecipe)

    @Query("SELECT * FROM savedRecipes")
    fun getAllRecipes(): Flow<List<savedRecipe>> // Flow для автоматических обновлений

    @Query("SELECT * FROM savedRecipes WHERE id = :id")
    suspend fun getRecipeById(id: Int): savedRecipe?
}