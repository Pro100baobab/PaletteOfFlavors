package com.paletteofflavors

import android.widget.EditText
import androidx.test.core.app.ApplicationProvider
import com.hbb20.CountryCodePicker
import com.paletteofflavors.logIn.RegistrationFragment
import com.paletteofflavors.logIn.isValidEmail
import com.paletteofflavors.logIn.isValidFullName
import com.paletteofflavors.logIn.isValidPassword
import com.paletteofflavors.logIn.isValidPhone
import com.paletteofflavors.logIn.isValidUsername
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class RegistrationValidationTest {

    private lateinit var fragment: RegistrationFragment

    private lateinit var fullName: EditText
    private lateinit var username: EditText
    private lateinit var email: EditText
    private lateinit var phoneNumber: EditText
    private lateinit var password: EditText

    private lateinit var ccp: CountryCodePicker

    @Before
    fun setUp() {
        fragment = RegistrationFragment()
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        fullName = EditText(context)
        username = EditText(context)
        email = EditText(context)
        phoneNumber = EditText(context)
        password = EditText(context)

        ccp = mock(CountryCodePicker::class.java).apply {
            whenever(selectedCountryCode).thenReturn("7")
            whenever(selectedCountryCodeWithPlus).thenReturn("+7")
        }
    }

    @Test
    fun `valid inputs should pass validation`() {

        fullName.setText("Иван Иванов")
        username.setText("Ivanov")
        email.setText("ivan@example.com")
        phoneNumber.setText("9123456789")
        password.setText("SecurePass123")

        println("FullName valid: ${isValidFullName(fullName)}")
        println("Username valid: ${isValidUsername(username)}")
        println("Email valid: ${isValidEmail(email)}")
        println("Phone valid: ${isValidPhone(phoneNumber, ccp)}\n ${phoneNumber.text}\n ${ccp.selectedCountryCodeWithPlus}")
        println("Password valid: ${isValidPassword(password)}")

        assertTrue(
            fragment.isFillsValid(fullName, username, email, phoneNumber, password, ccp)
        )
    }

    @Test
    fun `empty full name should fail validation`() {
        fullName.setText("")
        username.setText("ivanov")
        email.setText("ivan@example.com")
        phoneNumber.setText("9123456789")
        password.setText("SecurePass123!")

        assertFalse(
            fragment.isFillsValid(fullName, username, email, phoneNumber, password, ccp)
        )
    }

    @Test
    fun `invalid email should fail validation`() {

        fullName.setText("Иван Иванов")
        username.setText("ivanov")
        email.setText("invalid-email")
        phoneNumber.setText("9123456789")
        password.setText("SecurePass123!")

        assertFalse(
            fragment.isFillsValid(fullName, username, email, phoneNumber, password, ccp)
        )
    }

    @Test
    fun `short phone number should fail validation`() {

        fullName.setText("Иван Иванов")
        username.setText("ivanov")
        email.setText("ivan@example.com")
        phoneNumber.setText("123")
        password.setText("SecurePass123!")

        assertFalse(
            fragment.isFillsValid(fullName, username, email, phoneNumber, password, ccp)
        )
    }


    @Test
    fun `weak password should fail validation`() {

        fullName.setText("Иван Иванов")
        username.setText("ivanov")
        email.setText("ivan@example.com")
        phoneNumber.setText("9123456789")
        password.setText("123")

        assertFalse(
            fragment.isFillsValid(fullName, username, email, phoneNumber, password, ccp)
        )
    }
}
