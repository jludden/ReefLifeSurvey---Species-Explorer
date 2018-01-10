package me.jludden.testing.reeflifesurvey

import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import me.jludden.reeflifesurvey.Injection
import me.jludden.reeflifesurvey.MainActivity
import me.jludden.reeflifesurvey.R
import me.jludden.reeflifesurvey.data.FakeDataRepository
import me.jludden.reeflifesurvey.fishcards.CardViewAdapter
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by Jason on 1/6/2018.
 */
@RunWith(AndroidJUnit4::class) @LargeTest
class CardViewScreenTest {


    @Rule
    @JvmField var mainActivityTestRule = ActivityTestRule<MainActivity>(MainActivity::class.java)


//todo test - clicking on card goes to details activity no crashey

    @Test fun fromHome_navCardView_navDetails(){
        val dataRepo = FakeDataRepository.getInstance()
        dataRepo.addFavoriteSites(InstrumentationRegistry.getContext(), dataRepo.createSimpleTestSite1())
        dataRepo.addFishSpecies(dataRepo.createSimpleFishSpecies1())

//        Espresso.onView(ViewMatchers.withId(R.id.button_launch_select_sites)).perform(ViewActions.click())
        onView(ViewMatchers.withId(R.id.button_launch_browse_species)).perform(ViewActions.click())

        Thread.sleep(1500)

        //cardview
        onView(withId(R.id.recycler_view_card_list))
                .perform(RecyclerViewActions.actionOnItemAtPosition<CardViewAdapter.FishCardViewHolder>(1, click()));

        //details
        onView(withId(R.id.details_image_main)).check(matches(isDisplayed()))

//        onView(withText("Fake")).check(matches(isDisplayed())) //details matches fish speces 1
        //todo check actionbar title, other views displayed, etc
    }

}