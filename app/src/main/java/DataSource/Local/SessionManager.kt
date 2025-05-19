package DataSource.Local

import android.content.Context
import android.content.SharedPreferences

// Класс менеджера сессий (общие найстройки)
class SessionManager(
    _context: Context
){
    var usersSession: SharedPreferences = _context.getSharedPreferences("userLoginSession", Context.MODE_PRIVATE);
    var editor: SharedPreferences.Editor = usersSession.edit();    // разрешает редактировать сеанс внутри пользовательского сеанса
    var context: Context = _context;


    companion object {
        const val IS_LOGIN = "IsLoggedIn"
        const val KEY_FULLNAME = "fullName"
        const val KEY_USERNAME = "username"
        const val KEY_EMAIL = "email"
        const val KEY_PHONE_NUMBER = "phoneNumber"
        const val KEY_PASSWORD = "password"
        //private const val KEY_DATE = "date"
        //private const val KEY_GENDER = "gender"
    }

    public fun createLoginSession(fullName: String, username: String, email: String, phoneNumber: String, password: String){

        editor.putBoolean(IS_LOGIN, true)

        editor.putString(KEY_FULLNAME, fullName)
        editor.putString(KEY_USERNAME, username)
        editor.putString(KEY_EMAIL, email)
        editor.putString(KEY_PHONE_NUMBER, phoneNumber)
        editor.putString(KEY_PASSWORD, password)

        editor.commit()
    }

    public fun getUsersDetailFromSession(): HashMap<String, String?>{
        val userData = HashMap<String, String?>()

        userData[KEY_FULLNAME] = usersSession.getString(KEY_FULLNAME, null)
        userData[KEY_USERNAME] = usersSession.getString(KEY_USERNAME, null)
        userData[KEY_EMAIL] = usersSession.getString(KEY_EMAIL, null)
        userData[KEY_PHONE_NUMBER] = usersSession.getString(KEY_PHONE_NUMBER, null)
        userData.put(KEY_PASSWORD, usersSession.getString(KEY_PASSWORD, null))

        return userData;
    }

    public fun checkLogin(): Boolean{
        return usersSession.getBoolean(IS_LOGIN, false)
    }

    public fun logoutUserSession(){
        editor.clear()
        editor.commit()
    }
}