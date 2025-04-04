package DataSource.Local

import domain.Recipe

//import androidx.room.PrimaryKey

data class DatabaseRecipe(
    //@PrimaryKey val id: Int,
    val name: String,
    val ingredients: String,
    val recipeinstructions: String,
    val imageUrl: String?,
    val cookTime: Int,
    val recipecomments: Int,
    val recipelikes: Int
)

// Data Layer (преобразование из DatabaseRecipe в Recipe)
fun DatabaseRecipe.toRecipe(): Recipe {
    return Recipe(
        //id = id,
        id = 1,
        title = name,
        ingredients = ingredients.split(","), // Разделение строки на список ингредиентов
        instruction = recipeinstructions,
        imageUrl = imageUrl,
        cookTime = cookTime,
        comments = recipecomments,
        likes = recipelikes
    )
}