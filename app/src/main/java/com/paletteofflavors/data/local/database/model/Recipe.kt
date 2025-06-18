package com.paletteofflavors.data.local.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "resipes")
@Serializable // Для JSON-конвертера
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val ingredients: List<String>, // Конвертируется в JSON
    val instruction: String,
    val cookTime: Int,  // в минутах
    val imageUrl: String? = null,

    val complexity: Int,

    val mainCategory: String,
    val secondaryCategory: String
)


