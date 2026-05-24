/** turso/queries/ – вспомогательный пакет для SQL-строк,
 * которые сейчас выполняются через Turso.
 * В будущем будет заменён на api/endpoints/ */

package com.paletteofflavors.data.remote.api.turso.queries

import com.paletteofflavors.domain.model.NetworkRecipe

object RecipeQueries {
    // Базовый запрос с подсчетом комментариев
    private const val BASE_RECIPE_QUERY = """
        SELECT r.*, 
        (SELECT COUNT(*) FROM Comments c WHERE c.recipe_id = r.recipe_id) as comments_count 
        FROM Recipes r
    """

    const val ALL_PUBLIC_RECIPES = "$BASE_RECIPE_QUERY WHERE r.isPublic = 1"

    fun byMainCategory(category: String): String =
        "$BASE_RECIPE_QUERY WHERE r.isPublic = 1 AND r.main_category = '${category.replace("'", "''")}'"

    fun bySecondaryCategory(category: String): String =
        "$BASE_RECIPE_QUERY WHERE r.isPublic = 1 AND r.secondary_category = '${category.replace("'", "''")}'"

    fun byIngredients(ingredients: List<String>): String {
        val safeValues = ingredients.joinToString(",") { "'${it.replace("'", "''")}'" }
        return """
            $BASE_RECIPE_QUERY
            WHERE r.isPublic = 1 AND r.recipe_id IN (
                SELECT ri.recipe_id FROM RecipeIngredients ri
                JOIN IngredientDictionary id ON ri.ingredient_id = id.ingredient_id
                WHERE id.name IN ($safeValues)
            )
        """.trimIndent()
    }

    fun searchByTitleOrIngredient(words: List<String>): String {
        val safeWords = words.joinToString(",") { "'${it.replace("'", "''")}'" }
        return """
            SELECT r.*, (SELECT COUNT(*) FROM Comments c WHERE c.recipe_id = r.recipe_id) as comments_count 
            FROM Recipes r WHERE r.isPublic = 1 AND r.title IN ($safeWords)
            UNION ALL
            SELECT r.*, (SELECT COUNT(*) FROM Comments c WHERE c.recipe_id = r.recipe_id) as comments_count
            FROM Recipes r
            WHERE r.isPublic = 1 AND EXISTS (
                SELECT 1
                FROM RecipeIngredients ri
                JOIN IngredientDictionary id ON ri.ingredient_id = id.ingredient_id
                WHERE ri.recipe_id = r.recipe_id AND id.name IN ($safeWords)
            )
        """.trimIndent()
    }

    fun getUserRecipes(userId: Int, onlyPublic: Boolean = false): String {
        val base = "$BASE_RECIPE_QUERY WHERE r.owner_id = $userId"
        return if (onlyPublic) "$base AND r.isPublic = 1" else base
    }

    fun getUserRecipesCount(userId: Int): String =
        "SELECT COUNT(*) FROM Recipes WHERE owner_id = $userId"

    fun saveRecipe(recipe: NetworkRecipe, ownerId: Int): String {
        return """
            INSERT INTO Recipes (title, instructions, cookTime, complexity, image_url, owner_id, main_category, secondary_category, isPublic, liked_list, saved_list)
            VALUES (
                '${recipe.title.replace("'", "''")}',
                '${recipe.instruction.replace("'", "''")}',
                ${recipe.cookTime},
                ${recipe.complexity},
                ${if (recipe.imageUrl == null) "NULL" else "'${recipe.imageUrl}'"},
                $ownerId,
                '${recipe.mainCategory.replace("'", "''")}',
                '${recipe.secondaryCategory.replace("'", "''")}',
                ${if (recipe.isPublic) 1 else 0},
                '[]',
                '[]'
            )
        """.trimIndent()
    }

    fun deleteRecipe(recipeId: Int): String =
        "DELETE FROM Recipes WHERE recipe_id = $recipeId"

    fun addComment(recipeId: Int, userId: Int, text: String): String =
        "INSERT INTO Comments (recipe_id, user_id, text) VALUES ($recipeId, $userId, '${text.replace("'", "''")}')"

    fun getComments(recipeId: Int): String = """
        SELECT c.*, u.username 
        FROM Comments c 
        JOIN users u ON c.user_id = u.id 
        WHERE c.recipe_id = $recipeId 
        ORDER BY c.created_at DESC
    """.trimIndent()

    fun updateLikes(recipeId: Int, likedListJson: String): String =
        "UPDATE Recipes SET liked_list = '$likedListJson' WHERE recipe_id = $recipeId"

    fun updateSaved(recipeId: Int, savedListJson: String): String =
        "UPDATE Recipes SET saved_list = '$savedListJson' WHERE recipe_id = $recipeId"

    fun byIdIngredient(recipeId: Int): String =
        """
        SELECT IngredientDictionary.name 
        FROM RecipeIngredients
        JOIN IngredientDictionary ON RecipeIngredients.ingredient_id = IngredientDictionary.ingredient_id
        WHERE RecipeIngredients.recipe_id = $recipeId
        """.trimIndent()
}
