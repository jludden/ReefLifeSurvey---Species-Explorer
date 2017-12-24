package me.jludden.reeflifesurvey.about

import android.app.FragmentManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v7.app.AppCompatActivity
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import me.jludden.reeflifesurvey.customviews.ElasticDragDismissFrameLayout
import kotlinx.android.synthetic.main.activity_about.*

import com.mikepenz.aboutlibraries.LibsBuilder
import me.jludden.reeflifesurvey.R
import me.jludden.reeflifesurvey.R.id.*


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

        pager.adapter = AboutPagerAdapter(fragmentManager, View.OnClickListener { v ->
            when(v.id){
                about_button_launch_website_support -> launchBrowser(getString(R.string.website_launch_support_url))
                about_button_launch_website -> launchBrowser(getString(R.string.website_launch_url))
                about_button_launch_github -> launchBrowser(getString(R.string.github_launch_url))
            }
        })
        indicator.setViewPager(pager)
    }

    fun launchBrowser(url: String){
        if (url.startsWith("http://") || url.startsWith("https://")) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
    }


    class AboutPagerAdapter(private val fragmentManager: FragmentManager, private val onClickListener: View.OnClickListener) : PagerAdapter() {

        override fun getCount(): Int {
            return 3;
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

            when(position) {
                0 -> {
                    layout = inflater.inflate(R.layout.about_reeflifesurvey, container, false)
                    layout.findViewById<Button>(about_button_launch_website_support).setOnClickListener(onClickListener)
                    layout.findViewById<Button>(about_button_launch_github).setOnClickListener(onClickListener)
                }
                1 -> {
                    layout = inflater.inflate(R.layout.about_reeflifesurvey_methodology, container, false)
                    layout.findViewById<Button>(about_button_launch_website).setOnClickListener(onClickListener)
                }
                else -> {
                    layout = inflater.inflate(R.layout.about_libraries, container, false)

                    val aboutLibsFragment = LibsBuilder().fragment()
                    fragmentManager
                            .beginTransaction()
                            .replace(R.id.about_libs_content_frame, aboutLibsFragment, librariesFragmentTAG)
                            .commit()
                }
            }

            container.addView(layout);
            return layout
        }

        override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
            container.removeView(view as View)
        }

    }

    companion object {
        const val TAG: String = "AboutActivity"
        const val librariesFragmentTAG: String = "AboutLibrariesFragment"
    }
}