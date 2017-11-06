package me.jludden.reeflifesurvey.FullScreenImageActivity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.json.JSONObject;

import me.jludden.reeflifesurvey.BrowseFish.CardViewFragment;
import me.jludden.reeflifesurvey.BrowseFish.InfoCardLoader;
import me.jludden.reeflifesurvey.BrowseFish.model.InfoCard;
import me.jludden.reeflifesurvey.R;
import me.jludden.reeflifesurvey.ReefLifeDataFragment;
import me.jludden.reeflifesurvey.model.SurveySiteList;

import java.util.List;

/**
 * Created by Jason on 6/11/2017.
 */

public class FullScreenImageActivity extends FragmentActivity
        implements ReefLifeDataFragment.ReefLifeDataRetrievalCallback,
        ReefLifeDataFragment.ReefLifeDataUpdateCallback,
        LoaderManager.LoaderCallbacks<List<InfoCard.CardDetails>> {

    final static String DEBUG_TAG = "me.jludden.reeflifesurvey" ;

    ImageView mImageView;
    FullScreenImageAdapter mViewAdapter;
    ViewPager mViewPager;
    private GestureDetectorCompat mDetector;
    private static final float MIN_SWIPE_DISTANCE = 250;
    private ReefLifeDataFragment mDataFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_view_activity); //TODO layout for view_activity

        Log.d("jludden.reeflifesurvey"  , "FullScreenImageActivity onCreate");

        /* todo

            So the problem with this full screen image activity is the fact that it is an activity
            the data fragment will need to be recreated every time. this is obviously not ideal.
            basically, there is no workaround for having the loader here. either we load it here
            or we create a new instance of the data fragment which has the loader

         */
        //Start the retained data fragment TODO would really rather use the existing one
        FragmentManager fragmentManager = getSupportFragmentManager();
        mDataFragment = (ReefLifeDataFragment) fragmentManager.findFragmentByTag(ReefLifeDataFragment.TAG);
        if( mDataFragment == null) {
            mDataFragment = new ReefLifeDataFragment();
            fragmentManager.beginTransaction()
                    .add(mDataFragment, ReefLifeDataFragment.TAG)
                    .commit();
        } /*else { TODO not sure how this fragment behaves during a config change
            mDataFragment.setTargetFragment(this, mDataFragment.getTargetRequestCode());
        }*/


        mViewAdapter = new FullScreenImageAdapter(this);

        mViewAdapter.setRootLayout((RelativeLayout) findViewById(R.id.root_layout)); //todo probly doesnt do anything

        mViewPager = (ViewPager) findViewById(R.id.fullscreen_activity_pager);
        mViewPager.setAdapter(mViewAdapter);
        //mViewPager.setRotationY(180); //todo testing
        //mViewPager.setRotation(90);

        //Set the On Touch Listener for this view. When a swipe up is detected, show some details about the image
        // mViewPager.requestDisallowInterceptTouchEvent(true); //prevent the pager from intercepting touch events
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            float y2, y1 = -1;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Log.d("jludden.reeflifesurvey"  , "FullScreenImageACTIVITY on touch :"+event);

                int action = MotionEventCompat.getActionMasked(event);
                if (action == MotionEvent.ACTION_DOWN) y1 = event.getY();
                else if (action == MotionEvent.ACTION_MOVE) {
                    y2 = event.getY();
                    float deltaY = y1 - y2;
                    if ((y1 > -1) && (Math.abs(deltaY) > MIN_SWIPE_DISTANCE)) {
                        Log.d("jludden.reeflifesurvey"  , "FullScreenImageACTIVITY SWIPE UP: " + deltaY);
                        mViewAdapter.showDetails(); //have the view adapter actually notify the user
                    }
                }
                return false; //return false to allow the onPageChange for the view to still fire
            }
        });


        //TODO Consider adding a page change listener to add more items at the end. Or maybe not, maybe that is not what the fish quiz requires
        //Add a page change listener. This will notify the adapter to load more data when we are close to the end
        /*mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                Log.d("jludden.reeflifesurvey"  , "FullScreenImageACTIVITY onPageSelected: " + position);
                //detect when we're close to the end
                if ((mViewAdapter.getCount() - position) < 3){

                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //Log.d("jludden.reeflifesurvey"  , "FullScreenImageACTIVITY onPageScrolled: " + position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //Log.d("jludden.reeflifesurvey"  , "FullScreenImageACTIVITY onPageScrollStateChanged: " + state);
            }
        });*/

        //TODO - get data passed in from main activity
//        Intent i = getIntent();
//        int position = i.getIntExtra("position", 0);
        // mViewPager.setCurrentItem(0); //set the viewpager to the passed in index

        //start loader
        getSupportLoaderManager().initLoader(0, null, this); //todo need to be calling initloader for incremental as well

        // TODO we can add our own page change listener implementation here,
        // or we could just extend ViewPager class instead
        // mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

        // Close activity on button click
        Button closeButton = (Button) findViewById(R.id.button_close);
        closeButton.bringToFront();
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("jludden.reeflifesurvey"  , "FullScreenImageActivity Close Button Clicked");
                finish();
            }
        });
    }


    /**
     * Set the View to Sticky Immersive Mode
     * IMMERSIVE_STICKY flag, and the user swipes to display the system bars. Semi-transparent bars temporarily appear and then hide again.
     * The act of swiping doesn't clear any flags, nor does it trigger your system UI visibility change listeners, because the transient appearance of the system bars isn't considered a UI visibility change.
     * Note: Remember that the "immersive" flags only take effect if you use them in conjunction with SYSTEM_UI_FLAG_HIDE_NAVIGATION, SYSTEM_UI_FLAG_FULLSCREEN,
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            this.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public Loader<List<InfoCard.CardDetails>> onCreateLoader(int id, Bundle args) {
        Log.d("jludden.reeflifesurvey"  , "FullScreenImageActivity OnCreateLoader");
        return new InfoCardLoader(this, CardViewFragment.CardType.Fish, "");
    }

    @Override
    public void onLoadFinished(Loader<List<InfoCard.CardDetails>> loader, List<InfoCard.CardDetails> data) {
        Log.d("jludden.reeflifesurvey"  , "FullScreenImageActivity onLoadFinished loaderid: " + loader.getId() + " data length: " + data.size());

        mViewAdapter.updateItems(data); //update items in the adapter

    }

    @Override
    public void onLoaderReset(Loader<List<InfoCard.CardDetails>> loader) {

    }

    @Override
    public SurveySiteList retrieveSurveySiteList(){
        return mDataFragment.getSurveySites();
    }

    @Override
    public JSONObject retrieveFishSpecies() {
        return mDataFragment.getFishSpecies();
    }

    @Override
    public void onDataFragmentLoadFinished() {   }

}