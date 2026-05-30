package com.paletteofflavors.data.remote.api.turso

import android.util.Log
import com.paletteofflavors.BuildConfig
import com.paletteofflavors.data.remote.api.turso.queries.RecipeQueries
import com.paletteofflavors.data.remote.api.turso.queries.UserQueries
import com.paletteofflavors.domain.model.Comment
import com.paletteofflavors.domain.model.NetworkRecipe
import com.paletteofflavors.domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import tech.turso.libsql.Libsql
import java.net.HttpURLConnection
import java.net.URL

class Turso(
    private val dbUrl: String = BuildConfig.TURSO_DATABASE_URL,
    private val dbAuthToken: String = BuildConfig.TURSO_AUTH_TOKEN
) {
    // Авторизация пользователя
    suspend fun loginUser(username: String, passwordHash: Int): User? = withContext(Dispatchers.IO) {
        try {
            Libsql.openRemote(dbUrl, dbAuthToken).use { db ->
                db.connect().use { conn ->
                    val query = "SELECT * FROM users WHERE username = '$username' AND password = '$passwordHash'"

                    conn.query(query).use { rows ->
                        val nextRow = rows.nextRow()
                        if (nextRow != null) {
                            return@withContext User(
                                id = nextRow[0]?.toString()?.toIntOrNull(),
                                fullName = nextRow[3].toString(),
                                username = username,
                                email = nextRow[4].toString(),
                                phoneNumber = nextRow[5].toString(),
                                passwordHash = passwordHash,
                                avatarUrl = nextRow[7]?.toString()
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Turso", "Error during login", e)
        }
        null
    }

    // Проверка почты и имени на уникальность
    suspend fun checkUniqueUsernameAndEmail(username: String, email: String): Pair<Boolean, String?> =
        withContext(Dispatchers.IO) {
            try {
                Libsql.openRemote(dbUrl, dbAuthToken).use { db ->
                    db.connect().use { conn ->
                        conn.query(UserQueries.checkUniqueUserQuery(username, email))
                            .use { rows ->
                                var row = rows.nextRow()
                                while (row != null) {
                                    if (row[0].toString() == username) {
                                        return@withContext Pair(false, "Username already exists")
                                    }
                                    if (row[1].toString() == email) {
                                        return@withContext Pair(false, "Email already exists")
                                    }
                                    row = rows.nextRow()
                                }
                                return@withContext Pair(true, null)
                            }
                    }
                }
            } catch (e: Exception) {
                Log.e("Turso", "Error checking uniqueness", e)
                Pair(false, e.localizedMessage)
            }
        }

    // Регистрация пользователя
    suspend fun registerUser(user: User): Boolean = withContext(Dispatchers.IO) {
        try {
            Libsql.openRemote(dbUrl, dbAuthToken).use { db ->
                db.connect().use { conn ->
                    conn.execute(
                        UserQueries.registerUser(
                            user.fullName,
                            user.username,
                            user.phoneNumber,
                            user.email,
                            "" // Пароль не используется в registerUser напрямую, там hashCode()
                        ).replace("'${"".hashCode()}'", "'${user.passwordHash}'")
                    )
                    true
                }
            }
        } catch (e: Exception) {
            Log.e("Turso", "Error during registration", e)
            false
        }
    }

    suspend fun findUserByEmail(email: String): String? = withContext(Dispatchers.IO) {
        try {
            Libsql.openRemote(dbUrl, dbAuthToken).use { db ->
                db.connect().use { conn ->
                    val query = UserQueries.findUserByEmailQuery(email)
                    conn.query(query).use { rows ->
                        val nextRow = rows.nextRow()
                        nextRow?.get(5)?.toString()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Turso", "Error finding user", e)
            null
        }
    }

    suspend fun getUserById(userId: Int): User? = withContext(Dispatchers.IO) {
        try {
            Libsql.openRemote(dbUrl, dbAuthToken).use { db ->
                db.connect().use { conn ->
                    val query = "SELECT * FROM users WHERE id = $userId"
                    conn.query(query).use { rows ->
                        val row = rows.nextRow()
                        if (row != null) {
                            return@withContext User(
                                id = row[0]?.toString()?.toIntOrNull(),
                                fullName = row[3].toString(),
                                username = row[1].toString(),
                                email = row[4].toString(),
                                phoneNumber = row[5].toString(),
                                passwordHash = row[2]?.toString()?.toIntOrNull() ?: 0,
                                avatarUrl = row[7]?.toString()
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Turso", "Error getting user by id", e)
        }
        null
    }

    suspend fun updatePassword(email: String, phone: String, passwordHash: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            Libsql.openRemote(dbUrl, dbAuthToken).use { db ->
                db.connect().use { conn ->
                    conn.execute("BEGIN TRANSACTION")
                    try {
                        conn.execute(UserQueries.updatePasswordQuery(email, phone, passwordHash))
                        val isUpdated = conn.query(UserQueries.checkPasswordUpdatedQuery(email, phone, passwordHash)).use { rows ->
                            rows.nextRow() != null
                        }
                        conn.execute("COMMIT")
                        isUpdated
                    } catch (e: Exception) {
                        conn.execute("ROLLBACK")
                        throw e
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Turso", "Error updating password", e)
            false
        }
    }

    // Получение сетевых рецептов
    suspend fun getAllNetworkRecipes(sqlQuery: String? = null): List<NetworkRecipe> {
        return withContext(Dispatchers.IO) {
            val result = mutableListOf<NetworkRecipe>()
            try {
                val urlStr = dbUrl.replace("libsql://", "https://") + "/v2/pipeline"
                val url = URL(urlStr)

                val requestJson = JSONObject().apply {
                    put("requests", JSONArray().apply {
                        put(JSONObject().apply {
                            put("type", "execute")
                            put("stmt", JSONObject().apply {
                                put("sql", sqlQuery ?: RecipeQueries.ALL_PUBLIC_RECIPES)
                            })
                        })
                        put(JSONObject().apply {
                            put("type", "close")
                        })
                    })
                }

                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Authorization", "Bearer $dbAuthToken")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                connection.outputStream.use { os ->
                    os.write(requestJson.toString().toByteArray(Charsets.UTF_8))
                }

                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    return@withContext result
                }

                val responseBody = connection.inputStream.bufferedReader().readText()
                val jsonResponse = JSONObject(responseBody)
                val resultsArray = jsonResponse.getJSONArray("results")
                if (resultsArray.length() == 0) return@withContext result

                val okResult = resultsArray.getJSONObject(0)
                if (okResult.getString("type") != "ok") return@withContext result

                val responseObj = okResult.getJSONObject("response")
                val resultObj = responseObj.getJSONObject("result")
                val columns = resultObj.getJSONArray("cols")
                val rows = resultObj.getJSONArray("rows")

                // Строим карту «имя колонки -> индекс»
                val colIndex = mutableMapOf<String, Int>()
                for (i in 0 until columns.length()) {
                    val col = columns.getJSONObject(i)
                    colIndex[col.getString("name")] = i
                }

                // Функция для получения значения ячейки
                fun Any?.cellValue(): String? {
                    if (this == null || this == JSONObject.NULL) return null
                    val cell = this as? JSONObject ?: return this.toString()
                    return cell.optString("value", null) ?: cell.optString("value")
                }

                fun JSONArray.getColValue(key: String): String? {
                    val idx = colIndex[key] ?: return null
                    return get(idx).cellValue()
                }

                fun JSONArray.getColInt(key: String, default: Int = 0): Int {
                    val str = getColValue(key) ?: return default
                    return str.toIntOrNull() ?: default
                }

                // Парсим строки
                for (i in 0 until rows.length()) {
                    val row = rows.getJSONArray(i)

                    val likedList = parseJsonIntList(row.getColValue("liked_list"))

                    val recipe = NetworkRecipe(
                        recipeId = row.getColInt("recipe_id"),
                        title = row.getColValue("title") ?: "",
                        instruction = row.getColValue("instructions") ?: "",
                        cookTime = row.getColInt("cookTime"),
                        complexity = row.getColInt("complexity"),
                        commentsCount = row.getColInt("comments_count"),
                        likesCount = likedList.size,
                        imageUrl = row.getColValue("image_url") ?: "",
                        dateTime = row.getColValue("publish_dateTime") ?: "",
                        ownerId = row.getColValue("owner_id")?.toIntOrNull(),
                        mainCategory = row.getColValue("main_category") ?: "",
                        secondaryCategory = row.getColValue("secondary_category") ?: "",
                        ingredients = emptyList(),
                        isPublic = row.getColInt("isPublic") == 1,
                        likedListOfUsers = likedList,
                        savedListOfUsers = parseJsonIntList(row.getColValue("saved_list"))
                    )
                    result.add(recipe)
                }
            } catch (e: Exception) {
                Log.e("Turso", "Error fetching recipes", e)
            }
            result
        }
    }

    suspend fun saveRecipe(recipe: NetworkRecipe, ownerId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            Libsql.openRemote(dbUrl, dbAuthToken).use { db ->
                db.connect().use { conn ->
                    conn.execute(RecipeQueries.saveRecipe(recipe, ownerId))
                    true
                }
            }
        } catch (e: Exception) {
            Log.e("Turso", "Error saving recipe", e)
            false
        }
    }

    suspend fun deleteRecipe(recipeId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            Libsql.openRemote(dbUrl, dbAuthToken).use { db ->
                db.connect().use { conn ->
                    conn.execute(RecipeQueries.deleteRecipe(recipeId))
                    true
                }
            }
        } catch (e: Exception) {
            Log.e("Turso", "Error deleting recipe", e)
            false
        }
    }

    suspend fun addComment(recipeId: Int, userId: Int, text: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Libsql.openRemote(dbUrl, dbAuthToken).use { db ->
                db.connect().use { conn ->
                    conn.execute(RecipeQueries.addComment(recipeId, userId, text))
                    true
                }
            }
        } catch (e: Exception) {
            Log.e("Turso", "Error adding comment", e)
            false
        }
    }

    suspend fun getComments(recipeId: Int): List<Comment> = withContext(Dispatchers.IO) {
        val comments = mutableListOf<Comment>()
        try {
            Libsql.openRemote(dbUrl, dbAuthToken).use { db ->
                db.connect().use { conn ->
                    conn.query(RecipeQueries.getComments(recipeId)).use { rows ->
                        var row = rows.nextRow()
                        while (row != null) {
                            comments.add(
                                Comment(
                                    id = row[0]?.toString()?.toIntOrNull(),
                                    recipeId = row[1]?.toString()?.toIntOrNull() ?: recipeId,
                                    userId = row[2]?.toString()?.toIntOrNull() ?: 0,
                                    text = row[3].toString(),
                                    dateTime = row[4].toString(),
                                    username = row[5].toString()
                                )
                            )
                            row = rows.nextRow()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Turso", "Error getting comments", e)
        }
        comments
    }

    suspend fun updateLikes(recipeId: Int, likedList: List<Int>): Boolean = withContext(Dispatchers.IO) {
        try {
            Libsql.openRemote(dbUrl, dbAuthToken).use { db ->
                db.connect().use { conn ->
                    val json = JSONArray(likedList).toString()
                    conn.execute(RecipeQueries.updateLikes(recipeId, json))
                    true
                }
            }
        } catch (e: Exception) {
            Log.e("Turso", "Error updating likes", e)
            false
        }
    }

    suspend fun updateSaved(recipeId: Int, savedList: List<Int>): Boolean = withContext(Dispatchers.IO) {
        try {
            Libsql.openRemote(dbUrl, dbAuthToken).use { db ->
                db.connect().use { conn ->
                    val json = JSONArray(savedList).toString()
                    conn.execute(RecipeQueries.updateSaved(recipeId, json))
                    true
                }
            }
        } catch (e: Exception) {
            Log.e("Turso", "Error updating saved", e)
            false
        }
    }

    suspend fun followUser(followerId: Int, followedId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            Libsql.openRemote(dbUrl, dbAuthToken).use { db ->
                db.connect().use { conn ->
                    conn.execute(UserQueries.followUser(followerId, followedId))
                    true
                }
            }
        } catch (e: Exception) {
            Log.e("Turso", "Error following user", e)
            false
        }
    }

    suspend fun unfollowUser(followerId: Int, followedId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            Libsql.openRemote(dbUrl, dbAuthToken).use { db ->
                db.connect().use { conn ->
                    conn.execute(UserQueries.unfollowUser(followerId, followedId))
                    true
                }
            }
        } catch (e: Exception) {
            Log.e("Turso", "Error unfollowing user", e)
            false
        }
    }

    suspend fun getFollowStats(userId: Int): Pair<Int, Int> = withContext(Dispatchers.IO) {
        var followers = 0
        var following = 0
        try {
            Libsql.openRemote(dbUrl, dbAuthToken).use { db ->
                db.connect().use { conn ->
                    conn.query(UserQueries.getFollowersCount(userId)).use { rows ->
                        followers = rows.nextRow()?.get(0)?.toString()?.toIntOrNull() ?: 0
                    }
                    conn.query(UserQueries.getFollowingCount(userId)).use { rows ->
                        following = rows.nextRow()?.get(0)?.toString()?.toIntOrNull() ?: 0
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Turso", "Error getting follow stats", e)
        }
        Pair(followers, following)
    }

    suspend fun isFollowing(followerId: Int, followedId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            Libsql.openRemote(dbUrl, dbAuthToken).use { db ->
                db.connect().use { conn ->
                    conn.query(UserQueries.isFollowing(followerId, followedId)).use { rows ->
                        rows.nextRow() != null
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Turso", "Error checking if following", e)
            false
        }
    }

    suspend fun getUserRecipesCount(userId: Int): Int = withContext(Dispatchers.IO) {
        try {
            Libsql.openRemote(dbUrl, dbAuthToken).use { db ->
                db.connect().use { conn ->
                    conn.query(RecipeQueries.getUserRecipesCount(userId)).use { rows ->
                        rows.nextRow()?.get(0)?.toString()?.toIntOrNull() ?: 0
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Turso", "Error getting user recipes count", e)
            0
        }
    }

    suspend fun getIngredients(recipeId: Int): List<String> = withContext(Dispatchers.IO) {
        val ingredients = mutableListOf<String>()
        try {
            Libsql.openRemote(dbUrl, dbAuthToken).use { db ->
                db.connect().use { conn ->
                    conn.query(RecipeQueries.byIdIngredient(recipeId)).use { rows ->
                        var row = rows.nextRow()
                        while (row != null) {
                            ingredients.add(row[0].toString())
                            row = rows.nextRow()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Turso", "Error getting ingredients", e)
        }
        ingredients
    }

    suspend fun getFollowers(userId: Int): List<User> = getUsersList(UserQueries.getFollowersList(userId))
    suspend fun getFollowing(userId: Int): List<User> = getUsersList(UserQueries.getFollowingList(userId))

    suspend fun updateAvatar(userId: Int, avatarUrl: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Libsql.openRemote(dbUrl, dbAuthToken).use { db ->
                db.connect().use { conn ->
                    conn.execute(UserQueries.updateAvatarQuery(userId, avatarUrl))
                    true
                }
            }
        } catch (e: Exception) {
            Log.e("Turso", "Error updating avatar", e)
            false
        }
    }

    private suspend fun getUsersList(query: String): List<User> = withContext(Dispatchers.IO) {
        val users = mutableListOf<User>()
        try {
            Libsql.openRemote(dbUrl, dbAuthToken).use { db ->
                db.connect().use { conn ->
                    conn.query(query).use { rows ->
                        var row = rows.nextRow()
                        while (row != null) {
                            users.add(
                                User(
                                    id = row[0]?.toString()?.toIntOrNull(),
                                    fullName = row[3].toString(),
                                    username = row[1].toString(),
                                    email = row[4].toString(),
                                    phoneNumber = row[5].toString(),
                                    passwordHash = row[2]?.toString()?.toIntOrNull() ?: 0,
                                    avatarUrl = row[7]?.toString()
                                )
                            )
                            row = rows.nextRow()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Turso", "Error getting users list", e)
        }
        users
    }

    // Вспомогательная функция для парсинга строки вида "[1,2,3]" в List<Int>
    private fun parseJsonIntList(raw: String?): List<Int> {
        if (raw.isNullOrBlank() || raw == "[]") return emptyList()
        return try {
            JSONArray(raw).let { arr ->
                (0 until arr.length()).map { arr.getInt(it) }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
