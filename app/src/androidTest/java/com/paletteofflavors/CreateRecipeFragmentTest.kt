package com.paletteofflavors

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.paletteofflavors.MainActivity
import com.paletteofflavors.R
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateRecipeFragmentTest {

    @Before
    fun launchFragment() {
        ActivityScenario.launch(MainActivity::class.java).onActivity { activity ->
            activity.replaceMainFragment(CreateRecipeFragment())
        }
    }

    @Test
    fun testRecipeFormDisplayed() {
        onView(withId(R.id.recipe_name_edit)).check(matches(isDisplayed()))
        onView(withId(R.id.ingredients_edit)).check(matches(isDisplayed()))
        onView(withId(R.id.instructions_edit)).check(matches(isDisplayed()))
        onView(withId(R.id.recipe_cooking_time_edit)).check(matches(isDisplayed()))
        onView(withId(R.id.save_button)).check(matches(isDisplayed()))
    }

    @Test
    fun testEmptyFieldsShowError() {
        onView(withId(R.id.save_button)).perform(click())
        onView(withText("Заполните все поля")).check(matches(isDisplayed()))
    }

    @Test
    fun testValidRecipeCanBeSaved() {
        onView(withId(R.id.recipe_name_edit)).perform(typeText("Test Recipe"))
        onView(withId(R.id.ingredients_edit)).perform(typeText("Ingredient 1, Ingredient 2"))
        onView(withId(R.id.instructions_edit)).perform(typeText("Step 1, Step 2"))
        onView(withId(R.id.recipe_cooking_time_edit)).perform(typeText("30"))

        onView(withId(R.id.save_button)).perform(click())
        onView(withText("Сохранить рецепт?")).check(matches(isDisplayed()))
    }
}