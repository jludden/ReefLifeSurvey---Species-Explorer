package me.jludden.reeflifesurvey.fullscreenquiz

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v4.view.ViewPager
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.util.Pair
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import kotlinx.android.synthetic.main.activity_fullscreen.*
import kotlinx.android.synthetic.main.card_view_item.*
import kotlinx.android.synthetic.main.fullscreen_view_pager_item.*
import me.jludden.reeflifesurvey.R
import me.jludden.reeflifesurvey.customviews.BaseDisplayableImage
import me.jludden.reeflifesurvey.customviews.BottomDrawerBehavior
import me.jludden.reeflifesurvey.customviews.ImageDrawer
import me.jludden.reeflifesurvey.customviews.ImageDrawer.*

import me.jludden.reeflifesurvey.data.model.FishSpecies
import me.jludden.reeflifesurvey.data.InfoCardLoader
import me.jludden.reeflifesurvey.detailed.DetailsActivity
import me.jludden.reeflifesurvey.fishcards.CardViewFragment

/**
 * Created by Jason on 6/11/2017.
 */

class FullScreenImageActivity : FragmentActivity(), FullScreenImageListener, OnImageDrawerInteractionListener, LoaderManager.LoaderCallbacks<List<FishSpecies>> {
    internal lateinit var mViewAdapter: FullScreenImageAdapter
    internal lateinit var mViewPager: ViewPager
    internal var isLoading: Boolean = true
    internal var itemsLoaded = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        setContentView(R.layout.activity_fullscreen)

        Log.d(TAG, "FullScreenImageActivity onCreate")

        mViewAdapter = FullScreenImageAdapter(this, this)
        mViewPager = findViewById<View>(R.id.fullscreen_activity_pager) as ViewPager
        mViewPager.adapter = mViewAdapter
        BottomDrawerBehavior.from<View>(image_drawer).state = BottomDrawerBehavior.STATE_HIDDEN

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
                BottomDrawerBehavior.from<View>(image_drawer).state = BottomDrawerBehavior.STATE_COLLAPSED
            } else {
                details_text.visibility = View.GONE
                BottomDrawerBehavior.from<View>(image_drawer).state = BottomDrawerBehavior.STATE_HIDDEN
            }
        })
        findViewById<ImageButton>(R.id.button_close).setOnClickListener {
            Log.d(TAG, "FullScreenImageActivity Close Button Clicked")
            finish()
        }
        details_text.movementMethod = ScrollingMovementMethod()
        details_text.setOnClickListener {
            val options = ActivityOptions.makeSceneTransitionAnimation(this,
                    Pair(full_screen_image_view, getString(R.string.transition_launch_details)))
            val intent = Intent(this@FullScreenImageActivity,
                    DetailsActivity::class.java)
            intent.putExtra(FishSpecies.INTENT_EXTRA, mViewAdapter.findItemForPage(mViewPager.currentItem))
//            intent.putExtra(DetailsActivity.ARGS_NO_POSTPONE, true)
            startActivityForResult(intent, DetailsActivity.REQUEST_CODE, options.toBundle())
        }

        //TODO - get data passed in from main_toolbar activity
        //        Intent i = getIntent();
        //        int position = i.getIntExtra("position", 0);
        // mViewPager.setCurrentItem(0); //set the viewpager to the passed in index

        //start loader
        supportLoaderManager.initLoader(LOADER_ID, null, this) //need to be calling initloader for incremental as well
    }

    /**
     * update text box
     * todo update drawer selected
     */
    fun updateItemDescription(fish: FishSpecies) {
        Log.d(TAG, "FullScreenImageListener update item description ")
        details_text.text = "${fish.scientificName} \n ${fish.commonNames}"
        details_text.scrollTo(0,0)
    }

    /**
     * on image drawer item clicked
     */
    override fun onImageClicked(item: BaseDisplayableImage, sharedElement: View) {
        Log.d(TAG, "Bottom sheet image clicked ${item.identifier}")

        val page = mViewAdapter.findPageForItem(item.identifier as FishSpecies)
        mViewPager.currentItem = page

        val behavior = BottomDrawerBehavior.from<View>(image_drawer)
        if(behavior.state == BottomDrawerBehavior.STATE_EXPANDED){
            behavior.state = BottomDrawerBehavior.STATE_COLLAPSED
        }
    }

    override fun onDrawerStateChanged(newState: Int) {
        when(newState){
            BottomDrawerBehavior.STATE_EXPANDED -> {
                details_text.visibility = View.GONE
            }
            BottomDrawerBehavior.STATE_COLLAPSED -> {
                findViewById<ImageDrawer<BaseDisplayableImage>>(R.id.image_drawer).scrollTo(mViewPager.currentItem)
            }
        }
    }

    //incrementally load more -- todo also load more after adapter gets to end
    override fun onLoadMoreRequested() {
        if(!isLoading){
            isLoading = true
            Log.d(TAG, "load more requested")
            supportLoaderManager.getLoader<Any>(LOADER_ID).onContentChanged()
        }
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
        return InfoCardLoader(this, CardViewFragment.CardType.Fish, "")
    }

    override fun onLoadFinished(loader: Loader<List<FishSpecies>>, data: List<FishSpecies>) {
        Log.d(TAG, "FullScreenImageActivity onLoadFinished loaderid: " + loader.id + " data length: " + data.size)

        mViewAdapter.updateItems(data) //update items in the adapter
        updateItemDescription(data[mViewPager.currentItem])

        addDrawerItems(data, itemsLoaded)
        isLoading = false
        itemsLoaded = data.size
    }

    private fun addDrawerItems(data: List<FishSpecies>, startIndex: Int) {
        val imageDrawer = findViewById<ImageDrawer<BaseDisplayableImage>>(R.id.image_drawer)
        var count = 0
//        val itemList =  ArrayList<BaseDisplayableImage>()
        for(fish in data) {
//            itemList.add(BaseDisplayableImage(fish.primaryImageURL, fish))
            if(count++ >= startIndex) {
                    imageDrawer.addItem(BaseDisplayableImage(fish.primaryImageURL, fish))
//                Log.d(TAG, "FullScreenImageActivity Already added 80 items to drawer, won't add any more")
//                break
            } //todo.. load incrementally
        }

//        imageDrawer.setList(itemList)
    }

    override fun onLoaderReset(loader: Loader<List<FishSpecies>>) {}

    companion object {
        const val TAG = "FullScreenImageActivity"
        private val MIN_SWIPE_DISTANCE = 250f
        private const val LOADER_ID = 0
    }
}

interface FullScreenImageListener {
    fun onLoadMoreRequested()
}