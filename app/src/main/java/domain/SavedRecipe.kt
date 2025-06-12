package domain

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "savedRecipes")
data class SavedRecipe(
    @PrimaryKey val recipeId: Int,
    val title: String,
    val ingredients: List<String>,
    val instruction: String,
    val cookTime: Int,

    //Only networks margins
    val complexity: Int,
    val commentsCount: Int,
    val likesCount: Int,
    val imageUrl: String? = null,
    val dateTime: String,
    val ownerId: Int? = null
)

