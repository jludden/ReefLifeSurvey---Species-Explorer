package me.jludden.reeflifesurvey

import android.app.FragmentManager
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v7.app.AppCompatActivity
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.jludden.reeflifesurvey.customviews.ElasticDragDismissFrameLayout
import kotlinx.android.synthetic.main.activity_about.*

import com.mikepenz.aboutlibraries.LibsBuilder


/**
 * Created by Jason on 11/30/2017.
 *
 * Species thanks to the Plaid app
 *  - the UI depends heavily on Nick Butcher's ElasticDragDismissFrameLayout and InkPageIndicator classes
 */
class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        draggable_frame.addListener(
                object : ElasticDragDismissFrameLayout.SystemChromeFader(this) {
                    override fun onDragDismissed() {
                        // if we drag dismiss downward then the default reversal of the enter
                        // transition would slide content upward which looks weird. So reverse it.
                        if (draggable_frame.translationY > 0) {
                            window.returnTransition = TransitionInflater.from(this@AboutActivity)
                                    .inflateTransition(R.transition.about_return_downward)
                        }
                        finishAfterTransition()
                    }
                })

        pager.adapter = AboutPagerAdapter(fragmentManager)
        indicator.setViewPager(pager)
    }


    class AboutPagerAdapter(private val fragmentManager: FragmentManager) : PagerAdapter() {

        override fun getCount(): Int {
            return 2;
        }

        override fun isViewFromObject(view: View?, obj: Any?): Boolean {
            return view == obj
        }

        /**
         * Inflate the layout for each screen of the About activity
         *  First screen is a normal layout with an image and some static text
         *  Second screen is about the libraries used.
         *      I'm using the AboutLibraries library to generate a fragment that already has all this info auto-generated
         */
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val inflater = LayoutInflater.from(container.context)
            val layout : View

            if(position == 0 ) {
                layout = inflater.inflate(R.layout.about_reeflifesurvey, container, false)
            }
            else{
                layout = inflater.inflate(R.layout.about_libraries, container, false)

                val aboutLibsFragment = LibsBuilder().fragment()
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.about_libs_content_frame, aboutLibsFragment, librariesFragmentTAG)
                        .commit()
            }

            container.addView(layout);
            return layout
        }

    }

    companion object {
        const val TAG: String = "AboutActivity"
        const val librariesFragmentTAG: String = "AboutLibrariesFragment"
    }
}