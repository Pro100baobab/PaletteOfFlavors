package domain

data class Recipe(
    val id: Int,
    val title: String,
    val ingredients: List<String>,
    val instruction: String,
    val imageUrl: String?,
    val cookTime: Int
)
