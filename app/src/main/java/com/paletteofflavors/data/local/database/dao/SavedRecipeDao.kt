package com.paletteofflavors.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.paletteofflavors.data.local.database.model.SavedRecipeEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface SavedRecipeDao {
    @Insert
    suspend fun insert(recipe: SavedRecipeEntity)

    @Delete
    suspend fun delete(recipe: SavedRecipeEntity)

    @Query("SELECT * FROM savedRecipes")
    fun getAllRecipes(): Flow<List<SavedRecipeEntity>> // Flow для автоматических обновлений

    @Query("SELECT * FROM savedRecipes WHERE recipeId = :id")
    suspend fun getRecipeById(id: Int): SavedRecipeEntity?
}