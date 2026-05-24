package com.paletteofflavors.presentation.feature.recipes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paletteofflavors.data.remote.repository.RecipeRemoteRepository
import com.paletteofflavors.data.remote.repository.UserRemoteRepository
import com.paletteofflavors.domain.model.Comment
import com.paletteofflavors.domain.model.NetworkRecipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecipeDetailsViewModel(
    private val recipeRepository: RecipeRemoteRepository,
    private val userRepository: UserRemoteRepository
) : ViewModel() {

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> = _isFollowing

    private val _likesCount = MutableStateFlow(0)
    val likesCount: StateFlow<Int> = _likesCount

    private val _isLiked = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> = _isLiked

    private var currentRecipe: NetworkRecipe? = null

    fun setRecipe(recipe: NetworkRecipe, currentUserId: Int) {
        currentRecipe = recipe
        _likesCount.value = recipe.likesCount
        _isLiked.value = recipe.likedListOfUsers.contains(currentUserId)
        fetchComments()
        checkFollowStatus(currentUserId)
    }

    fun fetchComments() {
        val recipeId = currentRecipe?.recipeId ?: return
        viewModelScope.launch {
            try {
                _comments.value = recipeRepository.getComments(recipeId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun likeRecipe(userId: Int) {
        val recipe = currentRecipe ?: return
        viewModelScope.launch {
            try {
                val newList = recipe.likedListOfUsers.toMutableList()
                if (newList.contains(userId)) {
                    newList.remove(userId)
                    _isLiked.value = false
                    _likesCount.value -= 1
                } else {
                    newList.add(userId)
                    _isLiked.value = true
                    _likesCount.value += 1
                }
                
                recipeRepository.updateLikes(recipe.recipeId, newList)
                currentRecipe = recipe.copy(likedListOfUsers = newList, likesCount = _likesCount.value)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun addComment(userId: Int, text: String) {
        val recipeId = currentRecipe?.recipeId ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            try {
                val success = recipeRepository.addComment(recipeId, userId, text)
                if (success) {
                    fetchComments()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun checkFollowStatus(followerId: Int) {
        val followedId = currentRecipe?.ownerId ?: return
        if (followerId == followedId) return
        viewModelScope.launch {
            try {
                _isFollowing.value = userRepository.isFollowing(followerId, followedId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun toggleFollow(followerId: Int) {
        val followedId = currentRecipe?.ownerId ?: return
        if (followerId == followedId) return
        viewModelScope.launch {
            try {
                if (_isFollowing.value) {
                    userRepository.unfollowUser(followerId, followedId)
                    _isFollowing.value = false
                } else {
                    userRepository.followUser(followerId, followedId)
                    _isFollowing.value = true
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
