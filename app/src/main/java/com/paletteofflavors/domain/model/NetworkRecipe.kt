package com.paletteofflavors.domain.model


data class NetworkRecipe(
    val recipeId: Int,
    val title: String,
    val ingredients: List<String>, // Получается комбинированным запросом с JOIN и преобразованием к списку
    val instruction: String,
    val cookTime: Int,
    val complexity: Int,
    val commentsCount: Int,
    val likesCount: Int,
    val imageUrl: String? = null,
    val dateTime: String,
    val ownerId: Int? = null,
    val mainCategory: String,
    val secondaryCategory: String,
    val isPublic: Boolean,
    val likedListOfUsers: List<Int> = emptyList(),
    val savedListOfUsers: List<Int> = emptyList(),
)
