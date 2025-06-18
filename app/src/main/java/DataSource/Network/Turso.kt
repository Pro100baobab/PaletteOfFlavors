package DataSource.Network

import DataSource.Local.SessionManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.paletteofflavors.MainActivity
import com.paletteofflavors.R
import com.paletteofflavors.SearchFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tech.turso.libsql.Connection
import tech.turso.libsql.Libsql


class Turso(
    private val activity: MainActivity,
    private val context: Context,
    private val rememberMe: CheckBox? = null
) {

    private val dbUrl = activity.TURSO_DATABASE_URL
    private val dbAuthToken = activity.TURSO_AUTH_TOKEN

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
    fun getAllNetworkRecipesFlow(sqlQuery: String? = null): Flow<NetworkRecipe> = flow {
        if (!checkInternetConnection(context)) {
            Log.d("NetworkCheck", "No internet connection")
            return@flow
        }

        val db = Libsql.openRemote(dbUrl, dbAuthToken)
        val conn = db.connect()

        try {
            val query = sqlQuery ?: "SELECT * FROM Recipes"
            val rows = conn.query(query)

            while (true) {
                try {
                    val recipeRow =
                        rows.nextRow() ?: break // Выходим из цикла, если нет больше строк

                    val recipeId = recipeRow[0].toString().toInt()
                    val recipeIngredients = getIngredientsForRecipe(conn, recipeId)

                    // Получение значений из строки
                    val networkRecipe = NetworkRecipe(
                        recipeId = recipeId,
                        title = recipeRow[1].toString(),
                        instruction = recipeRow[2].toString(),
                        cookTime = recipeRow[3].toString().toInt(),
                        complexity = recipeRow[4].toString().toInt(),
                        commentsCount = recipeRow[5].toString().toInt(),
                        likesCount = recipeRow[6].toString().toInt(),
                        imageUrl = recipeRow[7]?.toString() ?: "",
                        dateTime = recipeRow[8].toString(),
                        ownerId = recipeRow[9]?.toString()!!.toInt(),
                        mainCategory = recipeRow[10].toString(),
                        secondaryCategory = recipeRow[11].toString(),
                        ingredients = recipeIngredients
                    )

                    emit(networkRecipe)
                } catch (e: Exception) {
                    Log.e("RecipeError", "Failed to process recipe row", e)
                    continue
                }
            }
        } catch (e: Exception) {
            Log.e("GetRecipeTable", "Error accessing database", e)
        } finally {
            conn?.close()
            db?.close()
        }
    }.flowOn(Dispatchers.IO)


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
            val dbUrl = activity.TURSO_DATABASE_URL
            val dbAuthToken = activity.TURSO_AUTH_TOKEN

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


    // Функции для проверки подключения к интернету
    fun checkInternetConnection(requireContext: Context): Boolean {
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
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        val currentNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(currentNetwork)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            ?: false
    }
}