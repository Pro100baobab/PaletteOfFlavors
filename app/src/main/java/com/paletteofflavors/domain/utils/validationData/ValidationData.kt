package com.paletteofflavors.domain.utils.validationData

import android.util.Patterns
import android.widget.EditText
import com.hbb20.CountryCodePicker

// Validation functions
fun isValidFullName(fullnameEditText: EditText): Boolean {
    val fullName = fullnameEditText.text.toString().trim()
    val pattern = Regex("^\\p{L}{2,15}(\\s\\p{L}{2,15})+$") // Минимум 2 слова по 2-15 символов

    return when {
        fullName.isEmpty() -> {
            fullnameEditText.error = "Введите имя и фамилию"
            false
        }
        !pattern.matches(fullName) -> {
            fullnameEditText.error = "Введите имя и фамилию (только буквы)"
            false
        }
        else -> true
    }
}

fun isValidUsername(usernameEditText: EditText): Boolean {
    val username = usernameEditText.text.toString().trim()
    val pattern = Regex("^[a-zA-Z0-9._-]{5,20}$") // 5-20 символов: буквы, цифры, ., _, -

    return when {
        username.isEmpty() -> {
            usernameEditText.error = "Введите имя пользователя"
            false
        }
        username.length < 5 -> {
            usernameEditText.error = "Слишком короткое имя (мин. 3 символа)"
            false
        }
        !pattern.matches(username) -> {
            usernameEditText.error = "Только буквы, цифры и символы ._-"
            false
        }
        else -> true
    }
}

fun isValidPassword(passwordEditText: EditText): Boolean {
    val password = passwordEditText.text.toString().trim()
    val pattern = Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$") // 8+ символов, цифры, верхний и нижний регистр

    return when {
        password.isEmpty() -> {
            passwordEditText.error = "Введите пароль"
            false
        }
        password.length < 8 -> {
            passwordEditText.error = "Пароль должен содержать минимум 8 символов"
            false
        }
        !pattern.matches(password) -> {
            passwordEditText.error = "Пароль должен содержать цифры, заглавные и строчные буквы"
            false
        }
        else -> true
    }
}

fun isValidEmail(emailEditText: EditText): Boolean {
    val result = Patterns.EMAIL_ADDRESS.matcher(emailEditText.text.toString().trim()).matches()

    if (!result)
        emailEditText.error = "invalid email address"

    return result
}

fun isValidPhone(phoneEditText: EditText, ccp: CountryCodePicker): Boolean {

    val phone = ccp.selectedCountryCodeWithPlus + phoneEditText.text.toString().trim()
    val pattern = Regex("""^\+?[0-9]{10,15}$""") // Между 10-15 цифрами, + опционален

    return when {
        phone.isEmpty() -> {
            phoneEditText.error = "Введите номер телефона"
            false
        }
        !pattern.matches(phone) -> {
            phoneEditText.error = "Неверный формат номера"
            false
        }
        else -> true
    }
}