package domain

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "resipes")
@Serializable // Для JSON-конвертера
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,   // автоинкремент
    val title: String,
    val ingredients: List<String>, // Конвертируется в JSON
    val instruction: String,
    val cookTime: Int,  // в минутах
    val imageUrl: String? = null
)

/*
data class Recipe(
    val id: Int,
    val title: String,
    val ingredients: List<String>,
    val instruction: String,
    val imageUrl: String?,
    val cookTime: Int,

    // Для облачных рецептов
    val comments: Int,
    val likes: Int
    // val stars: Int
)*/


