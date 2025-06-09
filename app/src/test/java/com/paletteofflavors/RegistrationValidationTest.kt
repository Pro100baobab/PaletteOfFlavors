package com.paletteofflavors

import android.widget.EditText
import androidx.test.core.app.ApplicationProvider
import com.hbb20.CountryCodePicker
import com.paletteofflavors.logIn.RegistrationFragment
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RegistrationValidationTest {
    private lateinit var fragment: RegistrationFragment
    private lateinit var fullName: EditText
    private lateinit var username: EditText
    private lateinit var email: EditText
    private lateinit var phoneNumber: EditText
    private lateinit var password: EditText
    private lateinit var ccp: CountryCodePicker

    @Before
    fun setup() {
        fragment = RegistrationFragment()
        fullName = EditText(ApplicationProvider.getApplicationContext())
        username = EditText(ApplicationProvider.getApplicationContext())
        email = EditText(ApplicationProvider.getApplicationContext())
        phoneNumber = EditText(ApplicationProvider.getApplicationContext())
        password = EditText(ApplicationProvider.getApplicationContext())
        ccp = CountryCodePicker(ApplicationProvider.getApplicationContext())
        ccp.setCountryForNameCode("RU") // Set default country
    }

    @Test
    fun `isFillsValid returns true for valid inputs`() {
        fullName.setText("John Doe")
        username.setText("johndoe")
        email.setText("john@example.com")
        phoneNumber.setText("1234567890")
        password.setText("Password123")

        assertTrue(fragment.isFillsValid(fullName, username, email, phoneNumber, password, ccp))
    }

    @Test()
    fun `isFillsValid returns false for empty full name`() {
        fullName.setText("")
        username.setText("johndoe")
        email.setText("john@example.com")
        phoneNumber.setText("1234567890")
        password.setText("Password123")

        assertFalse(fragment.isFillsValid(fullName, username, email, phoneNumber, password, ccp))
    }


}