package com.paletteofflavors

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.paletteofflavors.MainActivity
import com.paletteofflavors.R
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginFragmentTest {
    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setup() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @Test
    fun testLoginScreenDisplayed() {
        onView(withId(R.id.etLoginUsername)).check(matches(isDisplayed()))
        onView(withId(R.id.etLoginPassword)).check(matches(isDisplayed()))
        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()))
    }

    @Test
    fun testLoginWithEmptyFieldsShowsError() {
        onView(withId(R.id.btnLogin)).perform(click())
        onView(withText("Please fill all fields"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testNavigationToRegistration() {
        onView(withId(R.id.tvRegistration)).perform(click())
        onView(withId(R.id.etFullname)).check(matches(isDisplayed()))
    }

    @After
    fun cleanup() {
        scenario.close()
    }
}