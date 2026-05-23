package com.paletteofflavors.data.remote.api.turso

import android.util.Log
import com.paletteofflavors.BuildConfig
import com.paletteofflavors.data.remote.api.turso.queries.UserQueries
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
                                fullName = nextRow[3].toString(),
                                username = username,
                                email = nextRow[4].toString(),
                                phoneNumber = nextRow[5].toString(),
                                passwordHash = passwordHash
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

    // По умолчанию без фильтра, но можно использовать готовый запрос с фильтрацией
    // Получение сетевых рецептов
    suspend fun getAllNetworkRecipes(sqlQuery: String? = null): List<NetworkRecipe> {
        return withContext(Dispatchers.IO) {
            val result = mutableListOf<NetworkRecipe>()
            try {
                val urlStr = dbUrl.replace("libsql://", "https://") + "/v2/pipeline" // TODO: вынести в BuildConfig
                val url = URL(urlStr)

                val requestJson = JSONObject().apply {
                    put("requests", JSONArray().apply {
                        put(JSONObject().apply {
                            put("type", "execute")
                            put("stmt", JSONObject().apply {
                                put("sql", sqlQuery ?: "SELECT * FROM Recipes")
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

                // Функция для получения значения ячейки (объекта {type, value})
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

                    val recipe = NetworkRecipe(
                        recipeId = row.getColInt("recipe_id"),
                        title = row.getColValue("title") ?: "",
                        instruction = row.getColValue("instructions") ?: "",
                        cookTime = row.getColInt("cookTime"),
                        complexity = row.getColInt("complexity"),
                        commentsCount = row.getColInt("comments_count"),
                        likesCount = row.getColInt("likes_count"),
                        imageUrl = row.getColValue("image_url") ?: "",
                        dateTime = row.getColValue("publish_dateTime") ?: "",
                        ownerId = row.getColValue("owner_id")?.toIntOrNull(),
                        mainCategory = row.getColValue("main_category") ?: "",
                        secondaryCategory = row.getColValue("secondary_category") ?: "",
                        ingredients = emptyList(),
                        isPublic = row.getColInt("isPublic") == 1,
                        likedListOfUsers = parseJsonIntList(row.getColValue("liked_list")),
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
