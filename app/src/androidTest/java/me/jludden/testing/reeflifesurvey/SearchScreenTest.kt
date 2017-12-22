package me.jludden.testing.reeflifesurvey

import android.os.AsyncTask
import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onData
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.action.ViewActions.typeText
import android.support.test.espresso.core.deps.guava.base.Preconditions.checkArgument
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.contrib.RecyclerViewActions.scrollTo
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.squareup.rx2.idler.Rx2Idler
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import me.jludden.reeflifesurvey.R
import me.jludden.reeflifesurvey.search.SearchActivity
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.Matchers.anything
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.StringContains.containsString
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.model.Statement

/**
 * Created by Jason on 12/21/2017.
 */
@RunWith(AndroidJUnit4::class) @LargeTest class SearchScreenTest {

//    @Rule
//    var searchScreenIntentsTestRule: IntentsTestRule<SearchActivity> = IntentsTestRule(SearchActivity::class.java)
//
    /**
     * Prepare your test fixture for this test. In this case we register an IdlingResources with
     * Espresso. IdlingResource resource is a great way to tell Espresso when your app is in an
     * idle state. This helps Espresso to synchronize your test actions, which makes tests significantly
     * more reliable.
     */
//    @Before
//    fun registerIdlingResource() {
//        Espresso.registerIdlingResources(
//                searchActivityTestRule.activity.getCountingIdlingResource())
//    }


    /*public class AsyncTaskSchedulerRule implements TestRule {
        final Scheduler asyncTaskScheduler = Schedulers.from(AsyncTask.THREAD_POOL_EXECUTOR);

        @Override
        public Statement apply(Statement base, Description description) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    RxJavaHooks.setOnIOScheduler(scheduler -> asyncTaskScheduler);
                    RxJavaHooks.setOnComputationScheduler(scheduler -> asyncTaskScheduler);
                    RxJavaHooks.setOnNewThreadScheduler(scheduler -> asyncTaskScheduler);
                    try {
                        base.evaluate();
                    } finally {
                        RxJavaHooks.reset();
                    }
                }
            };
        }*/


    /**
     * TestRule for RxAndroid
     */
    @Rule @JvmField var asyncTaskSchedulerTestRule = object : TestRule {

        /**
         * try to get rx observables to work with espresso similar to how EspressoIdlingResource is used
         */

        val asyncTaskScheduler = Schedulers.from(AsyncTask.THREAD_POOL_EXECUTOR);

        override fun apply(base: Statement?, description: org.junit.runner.Description?): Statement? {
            return object : Statement() {
                override fun evaluate() {
                    /*RxJavaPlugins.onIoScheduler(asyncTaskScheduler)
                    RxJavaPlugins.onComputationScheduler(asyncTaskScheduler)
                    RxJavaPlugins.onNewThreadScheduler(asyncTaskScheduler)
                    RxJavaPlugins.setInitComputationSchedulerHandler { scheduler -> asyncTaskScheduler }*/

                    RxJavaPlugins.setInitComputationSchedulerHandler(
                            Rx2Idler.create("RxJava 2.x Computation Scheduler"));

                    try {
                        base?.evaluate();
                    } finally {
                        RxJavaPlugins.reset()
                    }
                }
            }
        }
    }



    /**
     * [ActivityTestRule] is a JUnit [@Rule][Rule] to launch your activity under test.
     *
     *
     * Rules are interceptors which are executed for each test method and are important building
     * blocks of Junit tests.
     */
    @Rule @JvmField var searchActivityTestRule = object :
            ActivityTestRule<SearchActivity>(SearchActivity::class.java) {

        /**
         * To avoid a long list of search results and the need to scroll through the list to find a
         * search result, we call [DataSource.deleteAllSearchResults] before each test.
         */
        /*override fun beforeActivityLaunched() {
            super.beforeActivityLaunched()
            // Doing this in @Before generates a race condition.
            Injection.provideDataRepository(InstrumentationRegistry.getTargetContext())
                    .deleteAllSearchResults()
        }*/
    }

    /**
     * A custom [Matcher] which matches an item in a [RecyclerView] by its text.
     *
     *
     * View constraints:
     *
     *  * View must be a child of a [RecyclerView]
     *
     * @param itemText the text to match
     *
     * @return Matcher that matches text in the given view
     */
    private fun withItemText(itemText: String): Matcher<View> {
        checkArgument(itemText.isNotEmpty(), "itemText cannot be null or empty")
        return object : TypeSafeMatcher<View>() {
            public override fun matchesSafely(item: View): Boolean {
                return Matchers.allOf(
                        ViewMatchers.isDescendantOfA(ViewMatchers.isAssignableFrom(TextView::class.java)),
                       // ViewMatchers.isDescendantOfA(ViewMatchers.isAssignableFrom(RecyclerView::class.java)),
                        ViewMatchers.withText(containsString(itemText))).matches(item)
            }

            override fun describeTo(description: Description) {
                description.appendText("is isDescendantOfA RV with text " + itemText)
            }
        }
    }

    @Test fun searchView_matchesTypedText(){
        val SEARCH_TEXT = "Wrasse"

        onView(withId(R.id.search_view)).perform(ViewActions.click())
        onView(withId(R.id.search_view)).perform(typeText(SEARCH_TEXT));
        onView(withText(SEARCH_TEXT)).check(matches(isDisplayed())) //search box matches Wrasse

        Thread.sleep(1500)


        RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(1)


        //onView(withText(containsString("Wrasse"))).check(matches(isDisplayed()))


        //onView(withId(R.id.search_view)).check(matches(withText("Wrasse")));

        //onData(anything()).atPosition(0).check(matches(hasDescendant(withItemText(("Wrasse")))))

      //  onView(withId(R.id.search_results_cards)).check(matches(hasDescendant(withText("Wrasse"))))
       // onView(withId(R.id.search_results_cards)).check(matches(hasDescendant(withItemText("Wrasse"))))

   //     onView(withItemText("Wrasse")).check(matches(isDisplayed()))

        //onView()


        // Scroll notes list to added note, by finding its description

//        onView(withId(R.id.search_results_cards)).perform(
//                scrollTo<RecyclerView.ViewHolder>(hasDescendant(withText("Wrasse"))))


        // Verify note is displayed on screen
       // onView(withItemText("Wrasse")).check(matches(isDisplayed()))
    }

}