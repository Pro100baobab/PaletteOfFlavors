package DataSource.Network

import DataSource.Local.SessionManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.room.Query
import com.paletteofflavors.HomeFragment
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

    private val dbUrl = (activity as MainActivity).TURSO_DATABASE_URL
    private val dbAuthToken = (activity as MainActivity).TURSO_AUTH_TOKEN

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

                                activity?.runOnUiThread {

                                    // Create a Session by SessionManager
                                    (activity as MainActivity).sessionManager =
                                        SessionManager(context, SessionManager.SESSION_USERSESSION)
                                    (activity as MainActivity).sessionManager.createLoginSession(
                                        fullName = _fullName,
                                        username = username,
                                        email = _email,
                                        phoneNumber = _phoneNumber,
                                        password = password
                                    ) //password, а не _password, потому что в бд хранится хешированный пароль

                                    Toast.makeText(
                                        context,
                                        "Login successful: $username $_fullName $_phoneNumber",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // Save LogIn Settings if checked
                                    when(isRememberMePressed){
                                        true -> rememberMe(username, password)
                                        else -> activity.sessionManagerRememberMe.logoutUserSession()
                                    }

                                    activity.replaceMainFragment(SearchFragment())
                                    activity.binding.fragmentContainerView.visibility = View.VISIBLE
                                    activity.returnNavigation()
                                }
                            } else {
                                activity?.runOnUiThread {
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
                activity?.runOnUiThread {
                    Toast.makeText(
                        context,
                        "Login failed: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun rememberMe(_username: String, _password: String) {

        val mainActivity = activity as MainActivity
        if (rememberMe!!.isChecked) {
            mainActivity.sessionManagerRememberMe =
                SessionManager(context, SessionManager.SESSION_REMEMBERME)
            mainActivity.sessionManagerRememberMe.createRememberMeSession(
                username = _username,
                password = _password
            )
        } else {
            if (mainActivity.sessionManagerRememberMe.checkRememberMe()) {
                mainActivity.sessionManagerRememberMe.logoutUserSession()
            }
        }
    }


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
                        // Проверяем, существует ли пользователь
                        conn.query("SELECT username FROM users WHERE username = '$username'")
                            .use { rows ->
                                if (rows.nextRow() != null) {
                                    activity?.runOnUiThread {
                                        Toast.makeText(
                                            context,
                                            "Username already exists",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    return@use
                                }
                            }

                        // TODO: Alter table users in turso for default CURRENT_TIMESTAMP
                        // Регистрируем нового пользователя
                        conn.query(
                            "INSERT INTO users (fullname, username, email, phone_number, password, created_at) VALUES('$fullname','$username', '$email', '$phone_number', '${password.hashCode()}', CURRENT_TIMESTAMP)"
                        )


                        //TODO: Add progress bar for connection time.
                        activity?.runOnUiThread {

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
                activity?.runOnUiThread {
                    Toast.makeText(context, "Registration failed: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }


    //по умолчанию без фильтра, но можно использовать готовый запрос для получения только нужных рецептов.
    fun getAllNetworkRecipesFlow(sqlQuery: String? = null): Flow<NetworkRecipe> = flow {

        if (!checkInternetConnection(context)) {
            return@flow
        }

        try {
            Libsql.openRemote(dbUrl, dbAuthToken).use { db ->
                db.connect().use { conn ->

                    val query = when(sqlQuery){
                        null -> "SELECT * FROM Recipes "
                        else -> sqlQuery
                    }

                    conn.query(query).use { rows ->

                        var recipeRow = rows.nextRow()

                        while (recipeRow != null) {

                            //Log.d("NetworkListSize", "+1")

                            val recipeId = recipeRow[0].toString().toInt()
                            val recipeIngredients = getIngredientsForRecipe(conn, recipeId)

                            val networkRecipe = NetworkRecipe(
                                recipeId = recipeRow[0].toString().toInt(),
                                title = recipeRow[1].toString(),
                                instruction = recipeRow[2].toString(),
                                cookTime = recipeRow[3].toString().toInt(),
                                complexity = recipeRow[4].toString().toInt(),
                                commentsCount = recipeRow[5].toString().toInt(),
                                likesCount = recipeRow[6].toString().toInt(),
                                imageUrl = recipeRow[7]?.toString(),
                                dateTime = recipeRow[8].toString(),
                                ownerId = recipeRow[9]?.toString()!!.toInt(),

                                mainCategory = recipeRow[10].toString(),
                                secondaryCategory = recipeRow[11].toString(),

                                // Получаем с помощью отдельного запроса
                                ingredients = recipeIngredients
                            )

                            emit(networkRecipe)
                            recipeRow = rows.nextRow()
                        }

                    }
                }
            }

        } catch (e: Exception) {
            Log.e("GetRecipeTable", "Error when try to get Recipes table", e)
            /*activity.runOnUiThread {
                Toast.makeText(
                    context,
                    "Getting rows from Recipes table failed: ${e.message}",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }*/
        }

    }.flowOn(Dispatchers.IO)



    private suspend fun getIngredientsForRecipe(conn: Connection, recipeId: Int): List<String> {
        val ingredientsList = mutableListOf<String>()

        conn.query("""
        SELECT IngredientDictionary.name 
        FROM RecipeIngredients
        JOIN IngredientDictionary ON RecipeIngredients.ingredient_id = IngredientDictionary.ingredient_id
        WHERE RecipeIngredients.recipe_id = $recipeId
    """).use { rows ->
            var row = rows.nextRow()
            while (row != null) {
                row[0].toString().let { ingredientsList.add(it) }
                row = rows.nextRow()
            }
        }

        return ingredientsList
    }



    private fun checkInternetConnection(requireContext: Context): Boolean {
        if (isInternetAvailable(requireContext)) {
            //Toast.makeText(requireContext, "Internet is available", Toast.LENGTH_SHORT).show() --Can't toast on a thread that has not called Looper.prepare()
            return true
        } else {
            //Toast.makeText(requireContext, "No internet connection", Toast.LENGTH_SHORT).show() -- Can't toast on a thread that has not called Looper.prepare()
            return false
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