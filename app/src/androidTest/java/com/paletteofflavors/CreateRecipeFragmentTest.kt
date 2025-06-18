package com.paletteofflavors

import android.view.View
import android.widget.RatingBar
import androidx.core.view.isVisible
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.paletteofflavors.presentation.feature.recipes.view.CreateRecipeFragment
import com.paletteofflavors.presentation.main.MainActivity
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateRecipeFragmentTest {

    @Before
    fun launchFragment() {
        ActivityScenario.launch(MainActivity::class.java).onActivity { activity ->
            activity.binding.fragmentContainerView.visibility = View.GONE
            activity.binding.appContent.isVisible = true
            activity.replaceMainFragment(CreateRecipeFragment())
        }
    }

    @Test
    fun testRecipeFormDisplayed() {

        onView(withId(R.id.back_to_favorites_button)).check(matches(isDisplayed()))
        onView(withId(R.id.recipe_title_label)).check(matches(isDisplayed()))

        onView(withId(R.id.recipe_name_input)).check(matches(isDisplayed()))
        onView(withId(R.id.recipe_name_edit)).check(matches(isDisplayed()))

        onView(withId(R.id.ingredients_input)).check(matches(isDisplayed()))
        onView(withId(R.id.ingredients_edit)).check(matches(isDisplayed()))

        onView(withId(R.id.instructions_input)).check(matches(isDisplayed()))
        onView(withId(R.id.instructions_edit)).check(matches(isDisplayed()))

        onView(withId(R.id.recipe_cooking_time_label)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withId(R.id.recipe_time_container)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withId(R.id.recipe_cooking_time_edit)).perform(scrollTo()).check(matches(isDisplayed()))

        onView(withId(R.id.complexity_linear_layout)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withId(R.id.setComplexityRatingBar)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withId(R.id.spinner1)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withId(R.id.spinner2)).perform(scrollTo()).check(matches(isDisplayed()))

        onView(withId(R.id.save_button)).perform(scrollTo()).check(matches(isDisplayed()))
    }

    @Test
    fun testValidRecipeCanBeSaved() {

        onView(withId(R.id.recipe_name_edit))
            .perform(typeText("Test Recipe"))
            .perform(closeSoftKeyboard())

        onView(withId(R.id.ingredients_edit))
            .perform(typeText("Ingredient 1, Ingredient 2"))
            .perform(closeSoftKeyboard())

        onView(withId(R.id.instructions_edit))
            .perform(typeText("Step 1, Step 2"))
            .perform(closeSoftKeyboard())


        onView(withId(R.id.recipe_cooking_time_edit))
            .perform(scrollTo(), typeText("30"))
            .perform(closeSoftKeyboard())

        onView(withId(R.id.setComplexityRatingBar))
            .perform(scrollTo(), setRating(3f))

        onView(withId(R.id.save_button))
            .perform(scrollTo(), click())

        onView(withText("Сохранить рецепт?")).check(matches(isDisplayed()))
    }


    // Кастомное действие для установки рейтинга
    private fun setRating(rating: Float): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return isAssignableFrom(RatingBar::class.java)
            }

            override fun getDescription(): String {
                return "Set rating on RatingBar"
            }

            override fun perform(uiController: UiController?, view: View) {
                val ratingBar = view as RatingBar
                ratingBar.rating = rating
            }
        }
    }
}