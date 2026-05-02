/** turso/queries/ – вспомогательный пакет для SQL-строк,
 * которые сейчас выполняются через Turso.
 * В будущем будет заменён на api/endpoints/ */

package com.paletteofflavors.data.remote.api.turso.queries

// TODO: параметризовать после миграции на сервер.
object RecipeQueries {
    const val ALL_RECIPES = "SELECT * FROM Recipes"

    fun byMainCategory(category: String): String =
        "SELECT r.* FROM Recipes r WHERE r.main_category = '${category.replace("'", "''")}'"

    fun bySecondaryCategory(category: String): String =
        "SELECT r.* FROM Recipes r WHERE r.secondary_category = '${category.replace("'", "''")}'"

    fun byIngredients(ingredients: List<String>): String {
        val safeValues = ingredients.joinToString(",") { "'${it.replace("'", "''")}'" }
        return """
            SELECT r.* FROM Recipes r
            WHERE r.recipe_id IN (
                SELECT ri.recipe_id FROM RecipeIngredients ri
                JOIN IngredientDictionary id ON ri.ingredient_id = id.ingredient_id
                WHERE id.name IN ($safeValues)
            )
        """.trimIndent()
    }

    fun searchByTitleOrIngredient(words: List<String>): String {
        val safeWords = words.joinToString(",") { "'${it.replace("'", "''")}'" }
        return """
            SELECT * FROM Recipes WHERE title IN ($safeWords)
            UNION ALL
            SELECT r.*
            FROM Recipes r
            WHERE EXISTS (
                SELECT 1
                FROM RecipeIngredients ri
                JOIN IngredientDictionary id ON ri.ingredient_id = id.ingredient_id
                WHERE ri.recipe_id = r.recipe_id AND id.name IN ($safeWords)
            )
        """.trimIndent()
    }
}