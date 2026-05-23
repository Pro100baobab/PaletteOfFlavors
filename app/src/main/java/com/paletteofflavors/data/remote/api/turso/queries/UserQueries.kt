package com.paletteofflavors.data.remote.api.turso.queries

object UserQueries{
    fun loginUserQuery(username: String, password: String): String =
        "SELECT * FROM users WHERE username = '$username' AND password = '${password.hashCode()}'"

    fun checkUniqueUserQuery(username: String, email: String): String =
        "SELECT username, email FROM users WHERE username = '$username' OR email = '$email'"

    fun registerUser(fullname: String, username: String, phone_number: String, email: String, password: String): String =
        "INSERT INTO users (fullname, username, email, phone_number, password, created_at) VALUES('$fullname','$username', '$email', '$phone_number', '${password.hashCode()}', CURRENT_TIMESTAMP)"

    fun findUserByEmailQuery(email: String): String =
        "SELECT * FROM users WHERE email = '$email'"

    fun updatePasswordQuery(email: String, phone: String, passwordHash: Int): String =
        "UPDATE users SET password = '$passwordHash' WHERE email = '$email' AND phone_number = '$phone'"

    fun checkPasswordUpdatedQuery(email: String, phone: String, passwordHash: Int): String =
        "SELECT 1 FROM users WHERE password = '$passwordHash' AND email = '$email' AND phone_number = '$phone' LIMIT 1"
}