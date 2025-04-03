package DataSource.Network

import domain.Recipe

data class NetworkRecipe(
    val recipeId: Int,
    val recipeName: String,
    val ingredientsList: List<String>,
    val recipeInstructions: String,
    val recipeImageUrl: String?,
    val cookingTime: Int,
)

fun NetworkRecipe.toRecipe(): Recipe {
    return Recipe(
        id = recipeId,
        title = recipeName,
        ingredients = ingredientsList,
        instruction = recipeInstructions,
        imageUrl = recipeImageUrl,
        cookTime = cookingTime,
    )
}