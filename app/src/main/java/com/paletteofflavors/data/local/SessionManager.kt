package com.paletteofflavors.data.local

import android.content.Context
import android.content.SharedPreferences

// Shared Preferences
class SessionManager(
    _context: Context,
    sessionName: String
){
    var usersSession: SharedPreferences = _context.getSharedPreferences(sessionName, Context.MODE_PRIVATE);
    var editor: SharedPreferences.Editor = usersSession.edit();    // разрешает редактировать сеанс внутри пользовательского сеанса
    var context: Context = _context;


    companion object {

        //Session names
        const val SESSION_USERSESSION = "userLoginSession"
        const val SESSION_REMEMBERME = "rememberMe"
        const val SESSION_CODE = "verificationSession"
        const val SESSION_BASESETTINGS = "baseSettingsSession"

        //LogIn variables
        const val IS_LOGIN = "IsLoggedIn"
        const val KEY_FULLNAME = "fullName"
        const val KEY_USERNAME = "username"
        const val KEY_EMAIL = "email"
        const val KEY_PHONE_NUMBER = "phoneNumber"
        const val KEY_PASSWORD = "password"


        //RememberMe variables
        const val IS_REMEMBERME = "IsRememberMe"
        const val KEY_SESSION_USERNAME = "username"
        const val KEY_SESSION_PASSWORD = "password"

        //Verification variables
        const val IS_VERIFICATION_CODE = "IsVerificationCode"
        const val KEY_CODE = "verificationCode"
        const val KEY_TIME = "timerEndTime"
        const val TIMER_DURATION: Long = 60000 // 1 минута

        //BaseSettings variables
        const val IS_BASESETINGS = "IsBaseSettings"
        const val KEY_CASH = "cash"
    }

    // Functions for BaseSettings Session
    fun createBaseSettingSession(cashFlag: Boolean){
        editor.putBoolean(IS_BASESETINGS, true)
        editor.putBoolean(KEY_CASH, cashFlag)
        editor.commit()
    }
    fun checkBaseSettings(): Boolean{
        return usersSession.getBoolean(IS_BASESETINGS, false)
    }



    // Functions for LogIn Session
    fun createLoginSession(fullName: String, username: String, email: String, phoneNumber: String, password: String){

        editor.putBoolean(IS_LOGIN, true)

        editor.putString(KEY_FULLNAME, fullName)
        editor.putString(KEY_USERNAME, username)
        editor.putString(KEY_EMAIL, email)
        editor.putString(KEY_PHONE_NUMBER, phoneNumber)
        editor.putString(KEY_PASSWORD, password)

        editor.commit()
    }
    fun getUsersDetailFromSession(): HashMap<String, String?>{
        val userData = HashMap<String, String?>()

        userData[KEY_FULLNAME] = usersSession.getString(KEY_FULLNAME, null)
        userData[KEY_USERNAME] = usersSession.getString(KEY_USERNAME, null)
        userData[KEY_EMAIL] = usersSession.getString(KEY_EMAIL, null)
        userData[KEY_PHONE_NUMBER] = usersSession.getString(KEY_PHONE_NUMBER, null)
        userData.put(KEY_PASSWORD, usersSession.getString(KEY_PASSWORD, null))

        return userData;
    }
    fun checkLogin(): Boolean{
        return usersSession.getBoolean(IS_LOGIN, false)
    }




    // function for RememberMe Session
    fun createRememberMeSession(username: String, password: String){

        editor.putBoolean(IS_REMEMBERME, true)

        editor.putString(KEY_SESSION_USERNAME, username)
        editor.putString(KEY_SESSION_PASSWORD, password)

        editor.commit()
    }
    fun getRememberMeDetailsFromSession(): HashMap<String, String?>{
        val userData = HashMap<String, String?>()

        userData[KEY_SESSION_USERNAME] = usersSession.getString(KEY_SESSION_USERNAME, null)
        userData.put(KEY_SESSION_PASSWORD, usersSession.getString(KEY_SESSION_PASSWORD, null))

        return userData;
    }
    fun checkRememberMe(): Boolean{
        return usersSession.getBoolean(IS_REMEMBERME, false)
    }




    // function for VerificationCode Session
    fun createCodeVerificationSession(verificationCode: String){

        editor.putBoolean(IS_VERIFICATION_CODE, true)
        editor.putLong(KEY_TIME, System.currentTimeMillis()+ TIMER_DURATION)
        editor.putString(KEY_CODE, verificationCode)

        editor.apply()
    }
    fun checkVerificationCode(): Boolean = usersSession.getBoolean(IS_VERIFICATION_CODE, false)

    fun getVerificationCodeSessionDetails(): String? = usersSession.getString(KEY_CODE, null)

    fun getVerificationCodeTimer(): Long = usersSession.getLong(KEY_TIME, 0)

    fun isVerificationCodeTimerRunning(): Boolean = usersSession.getLong(KEY_TIME, 0) > System.currentTimeMillis()

    fun clearVerificationCodeTimer() {
        editor.remove(KEY_TIME)
        editor.apply()
    }


    // Logout from current session (general)
    fun logoutUserSession(){
        editor.clear()
        editor.commit()
    }
}