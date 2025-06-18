package com.paletteofflavors.data.local.database.dao

import com.paletteofflavors.data.local.database.model.NetworkRecipe
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CashDao {
    @Insert
    suspend fun insert(recipe: NetworkRecipe)

    @Delete
    suspend fun delete(recipe: NetworkRecipe)

    @Query("DELETE FROM cashRecipes")
    fun cleanCashTable()

    @Query("SELECT * FROM cashRecipes")
    fun getAllCashRecipes(): Flow<List<NetworkRecipe>>

    @Query("SELECT * FROM cashRecipes WHERE recipeId = :id")
    suspend fun getRecipeById(id: Int): NetworkRecipe?
}