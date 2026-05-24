package com.paletteofflavors.presentation.feature.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paletteofflavors.data.remote.repository.RecipeRemoteRepository
import com.paletteofflavors.data.remote.repository.UserRemoteRepository
import com.paletteofflavors.domain.model.NetworkRecipe
import com.paletteofflavors.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val recipeRepository: RecipeRemoteRepository,
    private val userRepository: UserRemoteRepository
) : ViewModel() {

    private val _userRecipes = MutableStateFlow<List<NetworkRecipe>>(emptyList())
    val userRecipes: StateFlow<List<NetworkRecipe>> = _userRecipes

    private val _recipeCount = MutableStateFlow(0)
    val recipeCount: StateFlow<Int> = _recipeCount

    private val _followersCount = MutableStateFlow(0)
    val followersCount: StateFlow<Int> = _followersCount

    private val _followingCount = MutableStateFlow(0)
    val followingCount: StateFlow<Int> = _followingCount

    private val _followersList = MutableStateFlow<List<User>>(emptyList())
    val followersList: StateFlow<List<User>> = _followersList

    private val _followingList = MutableStateFlow<List<User>>(emptyList())
    val followingList: StateFlow<List<User>> = _followingList

    private val _targetUser = MutableStateFlow<User?>(null)
    val targetUser: StateFlow<User?> = _targetUser

    fun fetchUserData(userId: Int, isOwnProfile: Boolean = true) {
        viewModelScope.launch {
            try {
                if (!isOwnProfile) {
                    _targetUser.value = userRepository.getUserById(userId)
                }

                _recipeCount.value = userRepository.getUserRecipesCount(userId)
                val stats = userRepository.getFollowStats(userId)
                _followersCount.value = stats.first
                _followingCount.value = stats.second
                _userRecipes.value = recipeRepository.getUserRecipes(userId, !isOwnProfile)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun fetchFollowers(userId: Int) {
        viewModelScope.launch {
            try {
                _followersList.value = userRepository.getFollowers(userId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun fetchFollowing(userId: Int) {
        viewModelScope.launch {
            try {
                _followingList.value = userRepository.getFollowing(userId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
