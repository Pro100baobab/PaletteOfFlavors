package DataSource.Network

import DataSource.Local.SessionManager
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.paletteofflavors.HomeFragment
import com.paletteofflavors.MainActivity
import com.paletteofflavors.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tech.turso.libsql.Libsql


class Turso(private val activity: MainActivity, private val context: Context, private val rememberMe: CheckBox? = null){
    fun loginUser(username: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dbUrl = (activity as MainActivity).TURSO_DATABASE_URL
                val dbAuthToken = (activity as MainActivity).TURSO_AUTH_TOKEN

                Libsql.openRemote(dbUrl, dbAuthToken).use { db ->
                    db.connect().use { conn ->
                        val query = """
                            SELECT * FROM users 
                            WHERE username = '$username' 
                            AND password = '${password.hashCode()}'
                        """.trimIndent()

                        conn.query(query).use { rows ->
                            val nextRow = rows.nextRow()
                            if ( nextRow != null) {

                                //val _id = nextRow[0].toString()
                                //val _username = nextRow[1].toString()
                                //val _password = nextRow[2].toString() // вернет хершированный пароль
                                val _fullName = nextRow[3].toString()
                                val _email = nextRow[4].toString()
                                val _phoneNumber = nextRow[5].toString()

                                activity?.runOnUiThread {

                                    // Create a Session by SessionManager
                                    (activity as MainActivity).sessionManager = SessionManager(context, SessionManager.SESSION_USERSESSION)
                                    (activity as MainActivity).sessionManager.createLoginSession(fullName = _fullName, username = username, email = _email, phoneNumber = _phoneNumber, password = password) //password, а не _password, потому что в бд хранится хешированный пароль

                                    Toast.makeText(context,
                                        "Login successful: $username $_fullName $_phoneNumber", Toast.LENGTH_SHORT).show()

                                    // Save LogIn Settings if checked
                                    rememberMe(username, password)

                                    (activity as? MainActivity)?.binding?.appContent?.isVisible = true
                                    (activity as? MainActivity)?.showNormalFragment(HomeFragment())
                                }
                            } else {
                                activity?.runOnUiThread {
                                    Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
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


    private fun rememberMe(_username: String, _password: String){

        val mainActivity  = activity as MainActivity
        if(rememberMe!!.isChecked){
            mainActivity.sessionManagerRememberMe = SessionManager(context, SessionManager.SESSION_REMEMBERME)
            mainActivity.sessionManagerRememberMe.createRememberMeSession(username =  _username, password =  _password)
        }
        else {
            if(mainActivity .sessionManagerRememberMe.checkRememberMe()){
                mainActivity .sessionManagerRememberMe.logoutUserSession()
            }
        }
    }


    fun registerUser(fullname: String, username: String, phone_number: String, email: String, password: String, navController: NavController) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dbUrl = (activity as MainActivity).TURSO_DATABASE_URL
                val dbAuthToken = (activity as MainActivity).TURSO_AUTH_TOKEN

                Libsql.openRemote(dbUrl, dbAuthToken).use { db ->
                    db.connect().use { conn ->
                        // Проверяем, существует ли пользователь
                        conn.query("SELECT username FROM users WHERE username = '$username'").use { rows ->
                            if (rows.nextRow() != null) {
                                activity?.runOnUiThread {
                                    Toast.makeText(context, "Username already exists", Toast.LENGTH_SHORT).show()
                                }
                                return@use
                            }
                        }

                        // TODO: Alter table users in turso for default CURRENT_TIMESTAMP
                        // Регистрируем нового пользователя
                        conn.query(
                            "INSERT INTO users (fullname, username, email, phone_number, password, created_at) VALUES('$fullname','$username', '$email', '$phone_number', '${password.hashCode()}', CURRENT_TIMESTAMP)")


                        //TODO: Add progress bar for connection time.
                        activity?.runOnUiThread {

                            if(activity.sessionManager.checkLogin()){
                                activity.findNavController(R.id.fragmentContainerView).navigate(R.id.action_verifyOTP_to_loginFragment)
                                activity.binding.appContent.visibility = View.VISIBLE
                                activity.navBottomViewModel.setIsContentVisible(true)
                                Toast.makeText(context, "New account successful registered", Toast.LENGTH_SHORT).show()

                            }   else {
                                Toast.makeText(context, "Registration successful", Toast.LENGTH_SHORT).show()
                                navController.navigate(R.id.action_verifyOTP_to_loginFragment)
                            }
                        }
                    }

                }
            } catch (e: Exception) {
                Log.e("Registration", "Error during registration", e)
                activity?.runOnUiThread {
                    Toast.makeText(context, "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


}