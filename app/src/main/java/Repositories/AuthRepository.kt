package Repositories

import DataSource.model.AuthResponse
import domain.ProfileResponse

interface AuthRepository {
    suspend fun register(username: String, password: String): AuthResponse?
    suspend fun login(username: String, password: String): AuthResponse?
    suspend fun getProfile(token: String): ProfileResponse?
}