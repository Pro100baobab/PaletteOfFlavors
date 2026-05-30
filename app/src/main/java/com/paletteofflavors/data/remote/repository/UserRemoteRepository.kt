package com.paletteofflavors.data.remote.repository

import com.paletteofflavors.data.remote.api.imgBB.ImgBBService
import com.paletteofflavors.data.remote.api.turso.Turso
import com.paletteofflavors.domain.exception.NoInternetException
import com.paletteofflavors.domain.model.User
import com.paletteofflavors.domain.utils.InternetChecker

class UserRemoteRepository(
    private val turso: Turso,
    private val internetChecker: InternetChecker,
    private val imgBBService: ImgBBService? = null
) {
    private fun checkConnection() {
        if (!internetChecker.isConnected()) {
            throw NoInternetException()
        }
    }

    suspend fun loginUser(username: String, passwordHash: Int): User? {
        checkConnection()
        return turso.loginUser(username, passwordHash)
    }

    suspend fun checkUniqueUsernameAndEmail(username: String, email: String): Pair<Boolean, String?> {
        checkConnection()
        return turso.checkUniqueUsernameAndEmail(username, email)
    }

    suspend fun registerUser(user: User): Boolean {
        checkConnection()
        return turso.registerUser(user)
    }

    suspend fun findUserByEmail(email: String): String? {
        checkConnection()
        return turso.findUserByEmail(email)
    }

    suspend fun updatePassword(email: String, phone: String, passwordHash: Int): Boolean {
        checkConnection()
        return turso.updatePassword(email, phone, passwordHash)
    }

    suspend fun getUserById(userId: Int): User? {
        checkConnection()
        return turso.getUserById(userId)
    }

    suspend fun followUser(followerId: Int, followedId: Int): Boolean {
        checkConnection()
        return turso.followUser(followerId, followedId)
    }

    suspend fun unfollowUser(followerId: Int, followedId: Int): Boolean {
        checkConnection()
        return turso.unfollowUser(followerId, followedId)
    }

    suspend fun getFollowStats(userId: Int): Pair<Int, Int> {
        checkConnection()
        return turso.getFollowStats(userId)
    }

    suspend fun isFollowing(followerId: Int, followedId: Int): Boolean {
        checkConnection()
        return turso.isFollowing(followerId, followedId)
    }

    suspend fun getUserRecipesCount(userId: Int): Int {
        checkConnection()
        return turso.getUserRecipesCount(userId)
    }

    suspend fun getFollowers(userId: Int): List<User> {
        checkConnection()
        return turso.getFollowers(userId)
    }

    suspend fun getFollowing(userId: Int): List<User> {
        checkConnection()
        return turso.getFollowing(userId)
    }

    suspend fun uploadImageToImgBB(imageBase64: String): String? {
        checkConnection()
        val response = imgBBService?.uploadImage(imageBase64 = imageBase64)
        return if (response?.isSuccessful == true) {
            response.body()?.data?.url
        } else {
            null
        }
    }

    suspend fun updateAvatar(userId: Int, avatarUrl: String): Boolean {
        checkConnection()
        return turso.updateAvatar(userId, avatarUrl)
    }
}
