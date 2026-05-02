package com.paletteofflavors.domain.model

// Единая модель рецепта для использования по всему приложению.
// Не является Room-сущностью, конвертируется в/из CachedRecipeEntity и SavedRecipeEntity.

data class NetworkRecipe(
    val recipeId: Int,
    val title: String,
    val ingredients: List<String>, // Получается комбинированным запросом с JOIN и преобразованием к списку
    val instruction: String,       // Возможно, стоит получать при открытии конкретного рецепта
    val cookTime: Int,
    val complexity: Int,
    val commentsCount: Int,        // Убрать лишнее поле и добавить commentsList
    val likesCount: Int,           // Убрать лишнее поле
    val imageUrl: String? = null,
    val dateTime: String,
    val ownerId: Int? = null,
    val mainCategory: String,
    val secondaryCategory: String,
    val isPublic: Boolean,
    val likedListOfUsers: List<Int> = emptyList(),
    val savedListOfUsers: List<Int> = emptyList(),
    )

// TODO: перерассмотреть сущности серверной БД для нормализации и оптимизации запросов