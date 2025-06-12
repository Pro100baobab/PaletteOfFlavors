package DataSource.Network

import android.icu.util.MeasureUnit.Complexity
import androidx.room.Entity
import androidx.room.PrimaryKey
import domain.Recipe
import kotlinx.serialization.Serializable

data class NetworkRecipe(
    //Base margins
    val recipeId: Int,
    val title: String,
    val ingredients: List<String>, // Будет получено путем комбинированного запроса с JOIN
    val instruction: String,
    val cookTime: Int,

    //Only networks margins
    val complexity: Int,
    val commentsCount: Int,
    val likesCount: Int,
    val imageUrl: String? = null,
    val dateTime: String,
    val ownerId: Int? = null
    ){

    /*
    fun NetworkRecipe.toRecipe(): Recipe {
        return Recipe(
            id = recipeId,
            title = recipeName,
            ingredients = ingredientsList,
            instruction = recipeInstructions,
            imageUrl = recipeImageUrl,
            cookTime = cookingTime,
            //comments = recipecomments,
            //likes = recipelikes
        )
    }*/
}
