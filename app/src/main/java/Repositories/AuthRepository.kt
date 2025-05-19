package Repositories

import DataSource.model.AuthResponse
import domain.ProfileResponse

interface AuthRepository {
    suspend fun register(username: String, password: String): AuthResponse?
    suspend fun login(username: String, password: String): AuthResponse?
    suspend fun getProfile(token: String): ProfileResponse?

    //suspend fun getUsersData(username: String, password: String): UsersData?
}

//TODO: Add overriding of suspend function from AuthRepository
/*
class TursoData(): AuthRepository{
    override suspend fun getUsersData(username: String, password: String): UsersData? {
        val dbUrl = (activity as MainActivity).TURSO_DATABASE_URL
        val dbAuthToken = (activity as MainActivity).TURSO_AUTH_TOKEN

        Libsql.openRemote(dbUrl, dbAuthToken).use { db ->
            db.connect().use { conn ->
                val query = """
                            SELECT id FROM users
                            WHERE username = '$username'
                            AND password = '${password.hashCode()}'
                        """.trimIndent()
    }
}*/