package com.paletteofflavors.data.remote.repository

import com.paletteofflavors.data.remote.api.turso.Turso

class UserRemoteRepository(val turso: Turso) {

    suspend fun checkUniqueUsernameAndEmail(username: String, email: String): Boolean =
        turso.checkUniqueUsernameAndEmail(username, email)

    /*
    suspend fun registerUser(fullname: String, username: String, phone_number: String, email: String, password: String) =
        turso.registerUser(fullname, username, phone_number, email, password)*/
}