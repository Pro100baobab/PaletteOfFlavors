package com.paletteofflavors.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.paletteofflavors.data.local.database.model.CachedRecipeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedRecipeDao {
    @Insert
    suspend fun insert(recipe: CachedRecipeEntity)

    @Delete
    suspend fun delete(recipe: CachedRecipeEntity)

    @Query("DELETE FROM cachedRecipes")
    fun cleanCachedRecipesTable()

    @Query("SELECT * FROM cachedRecipes")
    fun getAllCachedRecipes(): Flow<List<CachedRecipeEntity>>

    @Query("SELECT * FROM cachedRecipes WHERE recipeId = :id")
    suspend fun getRecipeById(id: Int): CachedRecipeEntity?
}