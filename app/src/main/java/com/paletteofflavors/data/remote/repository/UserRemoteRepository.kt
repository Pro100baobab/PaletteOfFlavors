package com.paletteofflavors.data.remote.repository

import com.paletteofflavors.data.remote.api.turso.Turso
import com.paletteofflavors.domain.exception.NoInternetException
import com.paletteofflavors.domain.model.User
import com.paletteofflavors.domain.utils.InternetChecker

class UserRemoteRepository(
    private val turso: Turso,
    private val internetChecker: InternetChecker
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
}
