package com.paletteofflavors.domain.model

data class Comment(
    val id: Int? = null,
    val recipeId: Int,
    val userId: Int,
    val username: String,
    val text: String,
    val dateTime: String
)
