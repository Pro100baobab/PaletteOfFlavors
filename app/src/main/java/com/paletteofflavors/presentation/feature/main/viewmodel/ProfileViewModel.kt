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

    private val _avatarUrl = MutableStateFlow<String?>(null)
    val avatarUrl: StateFlow<String?> = _avatarUrl

    private val _uploadStatus = MutableStateFlow<Result<String>?>(null)
    val uploadStatus: StateFlow<Result<String>?> = _uploadStatus

    fun fetchUserData(userId: Int, isOwnProfile: Boolean = true) {
        viewModelScope.launch {
            try {
                val user = userRepository.getUserById(userId)
                
                _targetUser.value = user
                _avatarUrl.value = user?.avatarUrl
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

    fun uploadAndSetAvatar(userId: Int, imageBase64: String) {
        viewModelScope.launch {
            try {
                val url = userRepository.uploadImageToImgBB(imageBase64)
                if (url != null) {
                    val success = userRepository.updateAvatar(userId, url)
                    if (success) {
                        _avatarUrl.value = url
                        _uploadStatus.value = Result.success(url)
                    } else {
                        _uploadStatus.value = Result.failure(Exception("Failed to update database"))
                    }
                } else {
                    _uploadStatus.value = Result.failure(Exception("Failed to upload to ImgBB"))
                }
            } catch (e: Exception) {
                _uploadStatus.value = Result.failure(e)
            }
        }
    }

    fun clearUploadStatus() {
        _uploadStatus.value = null
    }
}
