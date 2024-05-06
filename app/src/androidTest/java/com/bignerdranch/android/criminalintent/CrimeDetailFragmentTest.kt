package com.bignerdranch.android.criminalintent

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.FragmentScenario.Companion.launch
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.regex.Pattern.matches

class CrimeDetailFragmentTest {

    @Test
    fun testCrimeDetailFragment() {
        // Launch the CrimeDetailFragment
        val scenario: FragmentScenario<CrimeDetailFragment> = launchFragmentInContainer()

        // Verify that the views are displayed
        onView(withId(R.id.crime_title)).check(matches(isDisplayed()))
        onView(withId(R.id.crime_date)).check(matches(isDisplayed()))
        onView(withId(R.id.crime_solved)).check(matches(isDisplayed()))

        // Perform actions on the views
        onView(withId(R.id.crime_title)).perform(replaceText("New Crime Title"))
        onView(withId(R.id.crime_date)).perform(click())

        // Verify that the actions have the intended effect
        onView(withId(R.id.crime_title)).check(matches(withText("New Crime Title")))
    }
}