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

    fun followUser(followerId: Int, followedId: Int): String =
        "INSERT OR IGNORE INTO Followers (follower_id, followed_id) VALUES ($followerId, $followedId)"

    fun unfollowUser(followerId: Int, followedId: Int): String =
        "DELETE FROM Followers WHERE follower_id = $followerId AND followed_id = $followedId"

    fun getFollowersCount(userId: Int): String =
        "SELECT COUNT(*) FROM Followers WHERE followed_id = $userId"

    fun getFollowingCount(userId: Int): String =
        "SELECT COUNT(*) FROM Followers WHERE follower_id = $userId"

    fun isFollowing(followerId: Int, followedId: Int): String =
        "SELECT 1 FROM Followers WHERE follower_id = $followerId AND followed_id = $followedId"

    fun getFollowersList(userId: Int): String = """
        SELECT u.* 
        FROM users u 
        JOIN Followers f ON u.id = f.follower_id 
        WHERE f.followed_id = $userId
    """.trimIndent()

    fun getFollowingList(userId: Int): String = """
        SELECT u.* 
        FROM users u 
        JOIN Followers f ON u.id = f.followed_id 
        WHERE f.follower_id = $userId
    """.trimIndent()
}