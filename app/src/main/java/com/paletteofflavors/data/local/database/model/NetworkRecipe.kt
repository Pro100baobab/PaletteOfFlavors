package com.paletteofflavors.data.local.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "cashRecipes")
data class NetworkRecipe(
    //Base margins
    @PrimaryKey val recipeId: Int,
    val title: String,
    val ingredients: List<String>, // Получается комбинированным запросом с JOIN и преобразованием к списку
    val instruction: String,
    val cookTime: Int,

    //Only networks margins
    val complexity: Int,
    val commentsCount: Int,
    val likesCount: Int,
    val imageUrl: String? = null,
    val dateTime: String,
    val ownerId: Int? = null,

    val mainCategory: String,
    val secondaryCategory: String
    )
