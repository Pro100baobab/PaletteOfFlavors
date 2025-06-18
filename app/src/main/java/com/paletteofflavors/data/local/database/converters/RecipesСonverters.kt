package com.paletteofflavors.data.local.database.converters

import com.paletteofflavors.data.local.database.model.NetworkRecipe
import com.paletteofflavors.data.local.database.model.SavedRecipe


// Конвертер из NetworkRecipe в SavedRecipe TODO: привести к единому интерфейсу
fun NetworkRecipe.toSavedRecipe() = SavedRecipe(
    recipeId = this.recipeId,
    title = this.title,
    ingredients = this.ingredients,
    instruction = this.instruction,
    cookTime = this.cookTime,
    complexity = this.complexity,
    commentsCount = this.commentsCount,
    likesCount = this.likesCount,
    imageUrl = this.imageUrl,
    dateTime = this.dateTime,
    ownerId = this.ownerId,
    mainCategory = this.mainCategory,
    secondaryCategory = this.secondaryCategory
)

fun SavedRecipe.toNetworkRecipe() = NetworkRecipe(
    recipeId = this.recipeId,
    title = this.title,
    ingredients = this.ingredients,
    instruction = this.instruction,
    cookTime = this.cookTime,
    complexity = this.complexity,
    commentsCount = this.commentsCount,
    likesCount = this.likesCount,
    imageUrl = this.imageUrl,
    dateTime = this.dateTime,
    ownerId = this.ownerId,
    mainCategory = this.mainCategory,
    secondaryCategory = this.secondaryCategory
)

