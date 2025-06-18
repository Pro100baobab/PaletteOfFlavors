package com.paletteofflavors

import androidx.navigation.findNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegistrationFragmentTest {

    @Before
    fun launchFragment() {
        ActivityScenario.launch(MainActivity::class.java).onActivity { activity ->
            activity.findNavController(R.id.fragmentContainerView)
                .navigate(R.id.registrationFragment)
        }
    }

    @Test
    fun testRegistrationFormDisplayed() {
        onView(withId(R.id.etFullname)).check(matches(isDisplayed()))
        onView(withId(R.id.etUsername)).check(matches(isDisplayed()))
        onView(withId(R.id.etEmail)).check(matches(isDisplayed()))
        onView(withId(R.id.etPhoneNumber)).check(matches(isDisplayed()))
        onView(withId(R.id.etPassword)).check(matches(isDisplayed()))
        onView(withId(R.id.btnRegister)).check(matches(isDisplayed()))
    }

    @Test
    fun testInvalidEmailShowsError() {
        onView(withId(R.id.etFullname)).perform(typeText("Own Fullname"), closeSoftKeyboard())
        onView(withId(R.id.etUsername)).perform(typeText("Username"), closeSoftKeyboard())
        onView(withId(R.id.etEmail)).perform(typeText("invalid-email"), closeSoftKeyboard())
        onView(withId(R.id.btnRegister)).perform(click())
    }
}