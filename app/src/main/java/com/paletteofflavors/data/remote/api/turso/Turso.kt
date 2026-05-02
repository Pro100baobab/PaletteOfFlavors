package com.paletteofflavors.data.remote.api.turso

import com.paletteofflavors.data.local.SessionManager
import com.paletteofflavors.domain.model.NetworkRecipe
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.paletteofflavors.BuildConfig
import com.paletteofflavors.presentation.main.MainActivity
import com.paletteofflavors.R
import com.paletteofflavors.presentation.feature.main.view.SearchFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tech.turso.libsql.Connection
import tech.turso.libsql.Libsql
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class Turso(
    private val activity: MainActivity,
    private val context: Context,
    private val rememberMe: CheckBox? = null,
    private val dbUrl: String = BuildConfig.TURSO_DATABASE_URL,
    private val dbAuthToken: String = BuildConfig.TURSO_AUTH_TOKEN
) {
    // Авторизация пользователя
    fun loginUser(username: String, password: String, isRememberMePressed: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Libsql.openRemote(dbUrl, dbAuthToken).use { db ->
                    db.connect().use { conn ->
                        val query = """
                            SELECT * FROM users 
                            WHERE username = '$username' 
                            AND password = '${password.hashCode()}'
                        """.trimIndent()

                        conn.query(query).use { rows ->
                            val nextRow = rows.nextRow()
                            if (nextRow != null) {

                                //val _id = nextRow[0].toString()
                                //val _username = nextRow[1].toString()
                                //val _password = nextRow[2].toString() // вернет хершированный пароль
                                val _fullName = nextRow[3].toString()
                                val _email = nextRow[4].toString()
                                val _phoneNumber = nextRow[5].toString()

                                activity.runOnUiThread {

                                    // LogIn Session
                                    activity.sessionManager =
                                        SessionManager(context, SessionManager.SESSION_USERSESSION)
                                    activity.sessionManager.createLoginSession(
                                        fullName = _fullName,
                                        username = username,
                                        email = _email,
                                        phoneNumber = _phoneNumber,
                                        password = password
                                    ) //password, а не _password, потому что в бд хранится хешированный пароль

                                    Toast.makeText(
                                        context,
                                        "Login successful: $username $_fullName $_email",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // Save/del LogIn Settings if checked (RememberMe Session)
                                    rememberMe(username, password)

                                    activity.replaceMainFragment(SearchFragment())
                                    activity.binding.fragmentContainerView.visibility = View.VISIBLE
                                    activity.returnNavigation()
                                }
                            } else {
                                activity.runOnUiThread {
                                    Toast.makeText(
                                        context,
                                        "Invalid credentials",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("Login", "Error during login", e)
                activity.runOnUiThread {
                    Toast.makeText(
                        context,
                        "Login failed: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // Управление сессией Запомнить
    private fun rememberMe(_username: String, _password: String) {

        val mainActivity = activity
        if (rememberMe!!.isChecked) {
            mainActivity.sessionManagerRememberMe =
                SessionManager(context, SessionManager.SESSION_REMEMBERME)
            mainActivity.sessionManagerRememberMe.createRememberMeSession(
                username = _username,
                password = _password
            )
        } else {
            if (!mainActivity.sessionManagerRememberMe.checkRememberMe()) {
                mainActivity.sessionManagerRememberMe.logoutUserSession()
            }
        }
    }

    // Проверка почты и имени на уникальность
    suspend fun checkUniqueUsernameAndEmail(username: String, email: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                Libsql.openRemote(dbUrl, dbAuthToken).use { db ->
                    db.connect().use { conn ->

                        var result = false
                        // Проверяем, существует ли пользователь
                        conn.query("SELECT username, email FROM users WHERE username = '$username' OR email = '$email'")
                            .use { rows ->
                                var nameFlag: Boolean = false
                                var emailFlag: Boolean = false
                                var row = rows.nextRow()

                                while (row != null) {

                                    if (nameFlag || emailFlag)
                                        return@use

                                    if (!nameFlag && row[0].toString() == username) {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(
                                                context,
                                                "Username already exists",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            nameFlag = true
                                        }
                                    }

                                    if (!emailFlag && row[1].toString() == email) {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(
                                                context,
                                                "Email already exists",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            emailFlag = true
                                        }
                                    }


                                    row = rows.nextRow()
                                }

                                result = !nameFlag && !emailFlag
                            }
                        result
                    }
                }
            } catch (e: Exception) {
                Log.e("Registration", "Error checking uniqueness", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Error checking uniqueness: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                false
            }
        }

    // Регистрация пользователя
    fun registerUser(
        fullname: String,
        username: String,
        phone_number: String,
        email: String,
        password: String,
        navController: NavController
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Libsql.openRemote(dbUrl, dbAuthToken).use { db ->
                    db.connect().use { conn ->
                        // Проверяем, существует ли пользователь осуществляется до вызова этой функции

                        // Регистрируем нового пользователя
                        conn.query(
                            "INSERT INTO users (fullname, username, email, phone_number, password, created_at) VALUES('$fullname','$username', '$email', '$phone_number', '${password.hashCode()}', CURRENT_TIMESTAMP)"
                        )


                        //TODO: Add progress bar for connection time.
                        activity.runOnUiThread {

                            if (activity.sessionManager.checkLogin()) {
                                activity.findNavController(R.id.fragmentContainerView)
                                    .navigate(R.id.action_verifyOTP_to_loginFragment)
                                activity.binding.appContent.visibility = View.VISIBLE
                                activity.navBottomViewModel.setIsContentVisible(true)
                                Toast.makeText(
                                    context,
                                    "New account successful registered",
                                    Toast.LENGTH_SHORT
                                ).show()

                            } else {
                                Toast.makeText(
                                    context,
                                    "Registration successful",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.navigate(R.id.action_verifyOTP_to_loginFragment)
                            }
                        }
                    }

                }
            } catch (e: Exception) {
                Log.e("Registration", "Error during registration", e)
                activity.runOnUiThread {
                    Toast.makeText(context, "Registration failed: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }


    // По умолчанию без фильтра, но можно использовать готовый запрос с фильтрацией
    // Получение сетевых рецептов
    // Вместо Flow<NetworkRecipe> будет suspend-функция, возвращающая List<NetworkRecipe>
    suspend fun getAllNetworkRecipes(sqlQuery: String? = null): List<NetworkRecipe> {

        Log.d("TursoHTTP", "Вызываем диспетчер")

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
                    Log.e("TursoHTTP", "Bad response: $responseCode")
                    return@withContext result
                }

                val responseBody = connection.inputStream.bufferedReader().readText()
                Log.d("TursoHTTP", "Response: $responseBody")
                val jsonResponse = JSONObject(responseBody)
                val resultsArray = jsonResponse.getJSONArray("results")
                if (resultsArray.length() == 0) return@withContext result

                // Первый (и единственный) результат – тип "ok"
                val okResult = resultsArray.getJSONObject(0)
                if (okResult.getString("type") != "ok") {
                    Log.e("TursoHTTP", "Unexpected top-level result type: ${okResult.getString("type")}")
                    return@withContext result
                }

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
                Log.e("TursoHTTP", "Error: ${e.message}", e)
            }
            Log.d("Result", "Всего рецептов: ${result.size}")
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

    // Получение списка ингредиентов по ID рецепта
    private fun getIngredientsForRecipe(conn: Connection, recipeId: Int): List<String> {
        val ingredientsList = mutableListOf<String>()

        conn.query(
            """
        SELECT IngredientDictionary.name 
        FROM RecipeIngredients
        JOIN IngredientDictionary ON RecipeIngredients.ingredient_id = IngredientDictionary.ingredient_id
        WHERE RecipeIngredients.recipe_id = $recipeId
    """
        ).use { rows ->
            var row = rows.nextRow()
            while (row != null) {
                row[0].toString().let { ingredientsList.add(it) }
                row = rows.nextRow()
            }
        }

        return ingredientsList
    }


    suspend fun FindUserByEmail(email: String, callback: (String) -> Unit) {

        try {

            var phoneNumber = ""

            withContext(Dispatchers.IO) {
                Libsql.openRemote(dbUrl, dbAuthToken).use { db ->
                    db.connect().use { conn ->
                        val query = """
                                SELECT * FROM users 
                                WHERE email = '$email'
                            """.trimIndent()

                        conn.query(query).use { rows ->
                            val nextRow = rows.nextRow()
                            if (nextRow != null) {
                                phoneNumber = nextRow[5].toString()

                                activity.runOnUiThread {
                                    Toast.makeText(
                                        context,
                                        "phone: $phoneNumber",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                activity.runOnUiThread {
                                    Toast.makeText(
                                        context,
                                        "Invalid credentials",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }


                    }
                }
            }

            callback(phoneNumber) // return

        } catch (e: Exception) {
            Log.e("Login", "Error during sending email", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Login failed: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
            callback("")
        }

    }


   /* fun checkInternetConnection(requireContext: Context): Boolean {
        return if (isInternetAvailable(requireContext)) {
            //Toast.makeText(requireContext, "Internet is available", Toast.LENGTH_SHORT).show() --Can't toast on a thread that has not called Looper.prepare()
            true
        } else {
            try {
                Toast.makeText(requireContext, "No internet connection", Toast.LENGTH_SHORT)
                    .show() // Can't toast on a thread that has not called Looper.prepare()
            } catch (_: Exception) {
            }
            false
        }
    }*/

   fun isConnected(): Boolean = isInternetAvailable(context)

    // Функции для проверки подключения к интернету
    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        val currentNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(currentNetwork)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            ?: false
    }
}