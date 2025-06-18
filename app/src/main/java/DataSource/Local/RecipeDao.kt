package DataSource.Local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import domain.Recipe
import kotlinx.coroutines.flow.Flow


@Dao
interface RecipeDao {
    @Insert()
    suspend fun insert(recipe: Recipe)

    @Delete
    suspend fun delete(recipe: Recipe)

    @Query("SELECT * FROM resipes")
    fun getAllRecipes(): Flow<List<Recipe>> // Flow для автоматических обновлений

    @Query("SELECT * FROM resipes WHERE id = :id")
    suspend fun getRecipeById(id: Int): Recipe?
}


