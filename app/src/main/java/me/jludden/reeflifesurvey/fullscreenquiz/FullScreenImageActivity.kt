package me.jludden.reeflifesurvey.fullscreenquiz

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.FragmentActivity
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v4.view.ViewPager
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import kotlinx.android.synthetic.main.activity_fullscreen.*
import me.jludden.reeflifesurvey.R
import me.jludden.reeflifesurvey.customviews.BaseDisplayableImage
import me.jludden.reeflifesurvey.customviews.ImageDrawer
import me.jludden.reeflifesurvey.customviews.ImageDrawer.*

import me.jludden.reeflifesurvey.data.model.FishSpecies
import me.jludden.reeflifesurvey.data.InfoCardLoader
import me.jludden.reeflifesurvey.fishcards.CardViewFragment

/**
 * Created by Jason on 6/11/2017.
 */

class FullScreenImageActivity : FragmentActivity(), FullScreenImageListener, OnImageDrawerInteractionListener, LoaderManager.LoaderCallbacks<List<FishSpecies>> {
    internal lateinit var mViewAdapter: FullScreenImageAdapter
    internal lateinit var mViewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        setContentView(R.layout.activity_fullscreen)

        Log.d(TAG, "FullScreenImageActivity onCreate")

        mViewAdapter = FullScreenImageAdapter(this, this)
        mViewPager = findViewById<View>(R.id.fullscreen_activity_pager) as ViewPager
        mViewPager.adapter = mViewAdapter

        //Set the On Touch Listener for this view. When a swipe up is detected, show some details about the image
        // mViewPager.requestDisallowInterceptTouchEvent(true); //prevent the pager from intercepting touch events
     /*   mViewPager.setOnTouchListener(object : View.OnTouchListener {
            internal var y2: Float = 0.toFloat()
            internal var y1 = -1f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                //Log.d(TAG  , "FullScreenImageACTIVITY on touch :"+event);

                val action = MotionEventCompat.getActionMasked(event)
                if (action == MotionEvent.ACTION_DOWN)
                    y1 = event.y
                else if (action == MotionEvent.ACTION_MOVE) {
                    y2 = event.y
                    val deltaY = y1 - y2
                    if (y1 > -1 && Math.abs(deltaY) > MIN_SWIPE_DISTANCE) {
                        Log.d(TAG, "FullScreenImageACTIVITY SWIPE UP: " + deltaY)
                        mViewAdapter.showDetails() //have the view adapter actually notify the user
                    }
                }
                return false //return false to allow the onPageChange for the view to still fire
            }
        })*/


        //page change listener. update
        mViewPager.addOnPageChangeListener(object: ViewPager.SimpleOnPageChangeListener(){
            override fun onPageSelected(position: Int) {
                updateItemDescription(mViewAdapter.findItemForPage(position))
                //todo update drawer position
                //todo update zoom level
            }
        })

        //button click listeners
        findViewById<ImageButton>(R.id.button_show_hud).setOnClickListener({
            Log.d(TAG, "FullScreenImageActivity hud button pressed")
            if(details_text.visibility == View.GONE) {
                details_text.visibility = View.VISIBLE
                BottomSheetBehavior.from<View>(image_drawer).setState(BottomSheetBehavior.STATE_COLLAPSED)
            } else {
                details_text.visibility = View.GONE
                BottomSheetBehavior.from<View>(image_drawer).setState(BottomSheetBehavior.STATE_HIDDEN)
            }
        })
        findViewById<ImageButton>(R.id.button_close).setOnClickListener {
            Log.d(TAG, "FullScreenImageActivity Close Button Clicked")
            finish()
        }
        details_text.movementMethod = ScrollingMovementMethod()

        //TODO - get data passed in from main_toolbar activity
        //        Intent i = getIntent();
        //        int position = i.getIntExtra("position", 0);
        // mViewPager.setCurrentItem(0); //set the viewpager to the passed in index

        //start loader
        supportLoaderManager.initLoader(0, null, this) //need to be calling initloader for incremental as well
    }

    /**
     * update text box
     * todo update drawer selected
     */
    override fun updateItemDescription(fish: FishSpecies) {
        Log.d(TAG, "FullScreenImageListener update item description ")
        details_text.text = "${fish.scientificName} \n ${fish.commonNames}"
        details_text.scrollTo(0,0)
    }

    /**
     * on image drawer item clicked
     */
    override fun onImageClicked(item: BaseDisplayableImage, sharedElement: View) {
        Log.d(TAG, "Bottom sheet image clicked ${item.identifier}")

        //todo go to image
        val page = mViewAdapter.findPageForItem(item.identifier as FishSpecies)
        mViewPager.currentItem = page
    }

    override fun onDrawerStateChanged(newState: Int) {
        //todo load more!!



    }


    /**
     * Set the View to Sticky Immersive Mode
     * IMMERSIVE_STICKY flag, and the user swipes to display the system bars. Semi-transparent bars temporarily appear and then hide again.
     * The act of swiping doesn't clear any flags, nor does it trigger your system UI visibility change listeners, because the transient appearance of the system bars isn't considered a UI visibility change.
     * Note: Remember that the "immersive" flags only take effect if you use them in conjunction with SYSTEM_UI_FLAG_HIDE_NAVIGATION, SYSTEM_UI_FLAG_FULLSCREEN,
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            this.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<FishSpecies>> {
        Log.d(TAG, "FullScreenImageActivity OnCreateLoader")
        return InfoCardLoader(this, CardViewFragment.CardType.Fish, "")    }


    override fun onLoadFinished(loader: Loader<List<FishSpecies>>, data: List<FishSpecies>) {
        Log.d(TAG, "FullScreenImageActivity onLoadFinished loaderid: " + loader.id + " data length: " + data.size)

        mViewAdapter.updateItems(data) //update items in the adapter
        updateItemDescription(data[mViewPager.currentItem])

        addDrawerItems(data)
    }

    private fun addDrawerItems(data: List<FishSpecies>) {
        val imageDrawer = findViewById<ImageDrawer<BaseDisplayableImage>>(R.id.image_drawer)

        var count = 0
        for(fish in data) {
            imageDrawer.addItem(BaseDisplayableImage(fish.primaryImageURL, fish))
            if(count++ > 80) break //todo
        }

    }

    override fun onLoaderReset(loader: Loader<List<FishSpecies>>) {}

    companion object {
        const val TAG = "FullScreenImageActivity"
        private val MIN_SWIPE_DISTANCE = 250f
    }


}

interface FullScreenImageListener {
    fun updateItemDescription(fish : FishSpecies)
}