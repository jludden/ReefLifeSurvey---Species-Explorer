package me.jludden.reeflifesurvey;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import me.jludden.reeflifesurvey.BrowseFish.CardViewFragment;
import me.jludden.reeflifesurvey.BrowseFish.CardViewFragment.CardViewSettings;

import me.jludden.reeflifesurvey.BrowseFish.DetailsViewFragment;
import me.jludden.reeflifesurvey.FullScreenImageActivity.FullScreenImageActivity;
import me.jludden.reeflifesurvey.BrowseFish.InfoCardLoader;
import me.jludden.reeflifesurvey.BrowseFish.model.InfoCard;
import me.jludden.reeflifesurvey.CountryList.CountryListFragment;

import me.jludden.reeflifesurvey.Intro.IntroViewPagerFragment;
import me.jludden.reeflifesurvey.model.DummyContent;
import me.jludden.reeflifesurvey.model.SurveySiteList;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.SupportMapFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static com.daimajia.androidanimations.library.Techniques.SlideInUp;
import static com.daimajia.androidanimations.library.Techniques.SlideOutDown;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        CompoundButton.OnCheckedChangeListener,
        CountryListFragment.OnListFragmentInteractionListener,
        CardViewFragment.OnCardViewFragmentInteractionListener,
        MapViewFragment.MapViewFragmentInteractionListener,
        ReefLifeDataFragment.ReefLifeDataRetrievalCallback,
        ReefLifeDataFragment.ReefLifeDataUpdateCallback, PopupMenu.OnMenuItemClickListener {


    private GoogleMap mMap;
    private FloatingActionButton mFAB, mBottomSheetButton;
    private FloatingActionButton[] mFABmenu;
    private boolean mFabMenuVisible = false;
    public static final int FAB_ONCLICK_ANIMATION_DURATION = 150; //snappy fab animations
    private ReefLifeDataFragment mDataFragment;
    private SurveySiteList mSurveySiteList;
    private SliderLayout mBottomSheetImageCarousel;

    //   private FloatingActionsMenu mFabMenu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); //todo could this be the collapsing toolbar?

        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
//This will set Expanded text to transparent so it wount overlap the content of the toolbar
        collapsingToolbar.setExpandedTitleColor(Color.parseColor("#00FF0000"));//ContextCompat.getColor(this, R.color.transparent));
        //  collapsingToolbar.setExpandedTitleColor(ContextCompat.getColor(this, R.color.black_semi_transparent));

////        CoordinatorLayout.LayoutParams params =(CoordinatorLayout.LayoutParams) collapsingToolbar.getLayoutParams();
//
       // AppBarLayout toolbar_layout = (AppBarLayout) findViewById(R.id.app_bar);
        //toolbar_layout.setMinimumHeight(400);
//        CoordinatorLayout.LayoutParams params =(CoordinatorLayout.LayoutParams) toolbar_layout.getLayoutParams();
//        params.height = 400;
//        toolbar_layout.setLayoutParams(params);

     //   toolbar_layout.setExpanded(false);
        // collapsingToolbar.setexpanded
        //collapsingToolbar.

        //set The custom text
     //   collapsingToolbar.setTitle("TEXTTTs");

//Set the color of collapsed toolbar text
     //   collapsingToolbar.setCollapsedTitleTextColor(ContextCompat.getColor(this, R.color.white));

        ToggleButton mToolbarButton_starred = (ToggleButton) findViewById(R.id.toolbar_button_starred);
        mToolbarButton_starred.setOnCheckedChangeListener(this);
        ((ToggleButton) findViewById(R.id.toolbar_button_loadAll)).setOnCheckedChangeListener(this);

        mBottomSheetButton = (FloatingActionButton) findViewById(R.id.bottom_sheet_fab);
        mBottomSheetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO consolidate fab logic, doesnt make sense to pass the event when they could just override on the onclicklistener
                //If mapfragment is shown, pass the click event to the fragment to
                MapViewFragment mapFrag = (MapViewFragment) getSupportFragmentManager().findFragmentByTag(MapViewFragment.TAG);
                if (mapFrag != null && mapFrag.isVisible()) {
                    mapFrag.onFABclick();
                }
                //else, show a fab menu definitely
                else {
                    if(mFabMenuVisible) hideFABmenu();
                    else showFABmenu();
                }
            }
        });

        mFAB = (FloatingActionButton) findViewById(R.id.fab_menu);
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO consolidate fab logic, doesnt make sense to pass the event when they could just override on the onclicklistener
                //If mapfragment is shown, pass the click event to the fragment to
                MapViewFragment mapFrag = (MapViewFragment) getSupportFragmentManager().findFragmentByTag(MapViewFragment.TAG);
                if (mapFrag != null && mapFrag.isVisible()) {
                    mapFrag.onFABclick();
                }
                //else, show a fab menu definitely
                else {
                    if(mFabMenuVisible) hideFABmenu();
                    else showFABmenu();
                }
            }
        });

        //Set up FAB Menu. make sure these are being cleaned up in onDestroy TODO
        FloatingActionButton mMiniFab1 = (FloatingActionButton) findViewById(R.id.fab_1);
        FloatingActionButton mMiniFab2 = (FloatingActionButton) findViewById(R.id.fab_2);
        FloatingActionButton mMiniFab3 = (FloatingActionButton) findViewById(R.id.fab_3);
        mFABmenu = new FloatingActionButton[]{mMiniFab1, mMiniFab2, mMiniFab3};
        mMiniFab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Snackbar.make(v, "FAB1 Launching Quiz Mode", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                launchFullScreenQuizModeActivity();
                hideFABmenu();
            }
        });
        mMiniFab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "FAB2 Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                expandBottomSheet(null);
                hideFABmenu();
            }
        });
        mMiniFab3.setOnClickListener(new View.OnClickListener() { //TODO mehhh web view tho
            @Override
            public void onClick(final View v) {
                Snackbar.make(v, "FAB3 Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
              /*  WebView mWebview  = new WebView(getApplication());

               // mWebview.getSettings().setJavaScriptEnabled(true); // enable javascript

                mWebview.setWebViewClient(new WebViewClient() {
                    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                        Snackbar.make(v, "FAB3 Webview test", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                });

                mWebview .loadUrl("http://www.reddit.com");
                setContentView(mWebview );*/
                hideFABmenu();
            }
        });

        //Set up the bottom sheet
        View bottomSheet = findViewById(R.id.bottom_sheet);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        //bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            /**
             * Called when the bottom sheet changes its state.
             *
             * @param bottomSheet The bottom sheet view.
             * @param newState    The new state
             */
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                Log.d("jludden.reeflifesurvey"  , "Bottom Sheet OnStateChanged: "+newState);
            }

            /**
             * Called when the bottom sheet is being dragged.
             *
             * @param bottomSheet The bottom sheet view.
             * @param slideOffset The new offset of this bottom sheet within [-1,1] range. Offset
             *                    increases as this bottom sheet is moving upward. From 0 to 1 the sheet
             *                    is between collapsed and expanded states and from -1 to 0 it is
             */
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//                Log.d("jludden.reeflifesurvey"  , "Bottom Sheet onSlide");
            }
        });
        //bottomSheet.addTouchables();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Start the retained data fragment
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

//
//        SupportMapFragment mapFragment  = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(
//                R.id.fragment_map));
//        mapFragment.getMapAsync(this);

        //start the IntroViewPagerFragment
        //hideClutter();
        launchUIFragment(new IntroViewPagerFragment(), IntroViewPagerFragment.TAG);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        hideFABmenu();
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(isFinishing()){ getSupportFragmentManager().beginTransaction()
                .remove(mDataFragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
//        final MenuItem settings_item = menu.findItem(R.id.action_settings);
//        settings_item.getac


        final MenuItem search_item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(search_item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                CardViewSettings.SEARCH_CONSTRAINT = newText.toLowerCase();
                CardViewFragment viewFragment = (CardViewFragment) getSupportFragmentManager().findFragmentByTag(CardViewFragment.TAG);
                if (viewFragment != null && viewFragment.isVisible()) {
                    viewFragment.onFilterApplied();
                }
                return true;
            }
        });
        //        searchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d("jludden.reeflifesurvey"  , "Searchview OnFocusChange. hasfocus: " +hasFocus);

                if(!hasFocus) hideKeyboard(v);
            }
        });
        return true;
    }

    public void hideKeyboard(View v){
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            PopupMenu popup = new PopupMenu(this, findViewById(R.id.collapsing_toolbar));
            popup.setOnMenuItemClickListener(this);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.settings_menu, popup.getMenu());
            popup.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Log.d("jludden.reeflifesurvey"  , "nav item selected: " + id);

        Fragment newFragment;
        String tag;
        String subtitle;
        mBottomSheetButton.setVisibility(View.GONE);
        View bottomSheet = findViewById(R.id.bottom_sheet);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

//        switch(id) {
//            case R.id
//        }

        if (id == R.id.nav_home){
            launchNewFragment(IntroViewPagerFragment.class);
        }
        else if (id == R.id.nav_card_view) {   // Handle the camera action
            //newFragment = new CardViewFragment();
            newFragment = CardViewFragment.newInstance(CardViewFragment.CardType.Fish, "");
            tag = CardViewFragment.TAG;

//            String title = "Title here";
//            subtitle = "hello world";
//            getSupportActionBar().setTitle(title);
//            getSupportActionBar().setSubtitle(subtitle);
            AppBarLayout toolbar = (AppBarLayout) findViewById(R.id.app_bar);
            toolbar.setExpanded(true,true);
            addSiteLocationsToToolbar();

            mFAB.show();
            launchUIFragment(newFragment, tag);

        } else if (id == R.id.nav_map_view) {
            newFragment = getSupportFragmentManager().findFragmentByTag(MapViewFragment.TAG);
            if (newFragment == null) {
                newFragment = MapViewFragment.newInstance();
            }
            tag = MapViewFragment.TAG;

            //hide a bunch of shit
            mFAB.hide();
            hideFABmenu();
            mBottomSheetButton.setVisibility(View.VISIBLE);
            AppBarLayout toolbar = (AppBarLayout) findViewById(R.id.app_bar);
            toolbar.setExpanded(false,true);
            //  SupportMapFragment mapFragment = (SupportMapFragment) newFragment;
            // mapFragment.getMapAsync(this);
            launchUIFragment(newFragment, tag);

        } else if (id == R.id.nav_send) {
            newFragment = CardViewFragment.newInstance(CardViewFragment.CardType.Countries, "");
            tag = "CountryFragment"; //TODO refactor these tags. They should be public static final vars on fragment classes
            mFAB.hide();
            launchUIFragment(newFragment, tag);

        } else {
            newFragment = new CountryListFragment();
            tag = "CountryListFragment"; //TODO refactor these tags. They should be public static final vars on fragment classes
            mFAB.hide();
            launchUIFragment(newFragment, tag);

        }
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    /**
     * public method to launch a new fragment
     * @param fragmentClass
     */
    public void launchNewFragment(Class fragmentClass){
        if(fragmentClass == IntroViewPagerFragment.class){
            String tag = IntroViewPagerFragment.TAG;
            Fragment newFragment = getSupportFragmentManager().findFragmentByTag(tag);
            if(newFragment == null) {
                newFragment = IntroViewPagerFragment.newInstance();
            }
            mFAB.hide();
            hideFABmenu();
            mBottomSheetButton.setVisibility(View.GONE);
            AppBarLayout toolbar = (AppBarLayout) findViewById(R.id.app_bar);
            toolbar.setExpanded(false,true);
            launchUIFragment(newFragment, tag);
        }
        else if(fragmentClass == CardViewFragment.class){
            launchNewCardViewFragment("");
        }
        else if(fragmentClass == MapViewFragment.class){
            String tag = MapViewFragment.TAG;
            Fragment newFragment = getSupportFragmentManager().findFragmentByTag(tag);
            if (newFragment == null) {
                newFragment = MapViewFragment.newInstance();
            }

            //hide a bunch of shit
            mFAB.hide();
            hideFABmenu();
            mBottomSheetButton.setVisibility(View.VISIBLE);
            AppBarLayout toolbar = (AppBarLayout) findViewById(R.id.app_bar);
            toolbar.setExpanded(false,true);
            launchUIFragment(newFragment, tag);
        }
    }

    //special method to launch this fragment because we are passing in a parameter
    private void launchNewCardViewFragment(@Nullable String code) {
        Fragment cardViewFrag = CardViewFragment.newInstance(CardViewFragment.CardType.Fish, code);
        launchUIFragment(cardViewFrag, CardViewFragment.TAG);
    }

    private void launchUIFragment(Fragment newFragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, newFragment, tag)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Set the expandable toolbar buttons to include the site locations
     * called when launching CardViewFragment
     */
    private void addSiteLocationsToToolbar() {
        LinearLayout toolbar_layout = (LinearLayout) findViewById(R.id.toolbar_layout);
        int offset=4;//TODO this is the number of buttons before the survey sites

        //remove the previously added site buttons
        int childCount = toolbar_layout.getChildCount()-offset;
        if(childCount > 0) {
            toolbar_layout.removeViews(offset, childCount);
        }

        int count = 0;
        for(String siteCode : retrieveSurveySiteList().getSelectedSiteCodes()) {
            ToggleButton siteButton = new ToggleButton(getBaseContext());
            siteButton.setId(offset + count++);
            siteButton.setTextOn(siteCode);
            siteButton.setChecked(true); //default state true
            siteButton.setTextOff("("+siteCode+")");
            siteButton.setTag(siteCode); //store the site associated with this button
            siteButton.setOnCheckedChangeListener(this);
            toolbar_layout.addView(siteButton);
        }
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
    public void onDataFragmentLoadFinished() {
        MapViewFragment mapFrag = (MapViewFragment) getSupportFragmentManager().findFragmentByTag(MapViewFragment.TAG);
        if (mapFrag != null && mapFrag.isVisible()) {
            mapFrag.onDataFragmentLoadFinished();
        }

        /*CardViewFragment viewFragment = (CardViewFragment) getSupportFragmentManager().findFragmentByTag(CardViewFragment.TAG);
        if (viewFragment != null && viewFragment.isVisible()) {
            viewFragment.();
        }*/
    }

    //todo - launch activity - right now just the full screen quiz mode
    public void launchFullScreenQuizModeActivity(){
        //get data from CardViewFragment TODO whole list of card data?
        Intent i = getIntent();
        InfoCard.CardDetails cardInfo = (InfoCard.CardDetails) i.getParcelableExtra("cardInfo");
        //
        //                if(cardInfo != null)   Log.d("jludden.reeflifesurvey"  ,"mFAB onclick. cardinfo name"+cardInfo.cardName);
        //
        //                //try passing some data to the new fragment
        //                ImageViewFragment imgFragment = new ImageViewFragment();
        //                Bundle b = new Bundle();
        //                b.putParcelable("cardInfo", cardInfo);
        //                imgFragment.setArguments(b);
        //                launchUIFragment(imgFragment);
        //launch full screen image activity on button click
        Intent intent = new Intent(MainActivity.this,
                FullScreenImageActivity.class);
        //intent.putExtra("cardInfo", cardInfo);
        startActivity(intent);
    }

    //region public accessor functions
    //public way for other fragments to get a reference to the floating action button
    public FloatingActionButton getFloatingActionButton(){
        //todo return different bottom sheet button depending on what fragment we're looking at
        //right now the mapfragment uses this. mapfrag != null but, during onCreate, is !isVisible()
//        MapViewFragment mapFrag = (MapViewFragment) getSupportFragmentManager().findFragmentByTag(MapViewFragment.TAG);
//        if (mapFrag != null && mapFrag.isVisible()) {
        return mBottomSheetButton;
//        }
//        else {
//            return mFAB;
//        }
    }

    /**
     *
     * @return true if the menu will be shown
     *      false if the menu is already shown
     */
    public boolean showFABmenu() {
        if(!mFabMenuVisible){
            mFabMenuVisible = true;
            animateFAB(mFABmenu, true);
            return true;
        }
        else return false;
    }

    /**
     *
     * @return true if the menu will be hidden
     *      false if the menu is already hidden
     */
    public boolean hideFABmenu() {
        Log.d("jludden.reeflifesurvey"  , "Main Activity hideFabMenu()");
        if(mFabMenuVisible){
            mFabMenuVisible = false;
            animateFAB(mFABmenu, false);
            return true;
        }
        else return false;
    }

    /**
     * Animate and show/hide the floating action button menu
     * @param fabArray array of floating action buttons
     * @param animateIn whether to display the menu and make items clickable,
     *                     or to animate out the items and hide them
     */
    private void animateFAB(final FloatingActionButton[] fabArray, final boolean animateIn){
        int index = 0;
        Techniques anim = animateIn ? SlideInUp : SlideOutDown;
        int rotation = animateIn ? 90 : 0;
        ObjectAnimator.ofFloat(mFAB, "rotation", rotation).setDuration(FAB_ONCLICK_ANIMATION_DURATION).start(); //rotate the main FAB
        for(FloatingActionButton fab : fabArray) {
            YoYo.with(anim)
                    .interpolate(new AnticipateOvershootInterpolator()) //overshoots and bounces back
                    .duration(FAB_ONCLICK_ANIMATION_DURATION)
                    .withListener(getAnimatorListenerForFAB(fabArray,++index,animateIn))
                    .playOn(fab);
        }
    }

    /**
     *  Set up an animator listener to set the menu button visibility
     *      when it animates in, we show the buttons immediately
     *      when it animates out, we hide the buttons at the end of the animation
     * @param fabArray array of floating action buttons
     * @param fabIndex the index of this button in the array
     * @param animateIn true - the buttons will be shown, false - they will be hidden
     * @return
     */
    private Animator.AnimatorListener getAnimatorListenerForFAB(final FloatingActionButton[] fabArray, final int fabIndex, final boolean animateIn) {
        return new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (animateIn) setFabMenuVisibleHelper(fabArray, fabIndex, true);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!animateIn) setFabMenuVisibleHelper(fabArray, fabIndex, false);
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        };
    }

    /**
     * Helper function to set the locations and visibility of each floating menu button
     * @param fabArray array of floating action buttons
     * @param fabIndex the index of this button in the array
     * @param animateIn true - the buttons will be shown, false - they will be hidden
     */
    private void setFabMenuVisibleHelper(final FloatingActionButton[] fabArray, final int fabIndex, final boolean animateIn){
        int direction = animateIn ? 1 : -1;

        //todo remove: testing to try to animate fab menu from wherever the bottom sheet floating action button is. seems viable but not fantastic
//        CoordinatorLayout.LayoutParams mainFABparams = (CoordinatorLayout.LayoutParams) mFAB.getLayoutParams();
//        Log.d("jludden.reeflifesurvey"  ,"mainFAB bottomMargin: "+ mainFABparams.bottomMargin + " keyline: "+mainFABparams.keyline
//            +"  height: "+mainFABparams.height + " topMargin: "+ mainFABparams.topMargin + " measuredheight: "+mFAB.getMeasuredHeight() );
//
//        View btmSheet = findViewById(R.id.bottom_sheet);
//        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(btmSheet);
//
//        CoordinatorLayout.LayoutParams btmSheetFABparams = (CoordinatorLayout.LayoutParams) btmSheet.getLayoutParams();
//        Log.d("jludden.reeflifesurvey"  ,"btmSheetFABparams bottomMargin: "+ btmSheetFABparams.bottomMargin + " keyline: "+btmSheetFABparams.keyline
//                +"  height: "+btmSheetFABparams.height + " baseline: " + btmSheet.getBaseline() + "topMargin: "+ mainFABparams.topMargin
//                +" state: "+bottomSheetBehavior.getState() + " measured height: " +btmSheet.getMeasuredHeight()
//        );
//
//        int offset = 110;//mFAB.getMeasuredHeight();
//        if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) offset += btmSheet.getMeasuredHeight();//340; //bottomSheetBehavior.getMeasuredHeight();
//        else if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) offset += 80; //bottomSheetBehavior.getPeekHeight();
//        Log.d("jludden.reeflifesurvey"  ,"btmSheetFABparams offset: "+offset);

        FloatingActionButton fab = fabArray[fabIndex-1];
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) fab.getLayoutParams();
//        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) fab.getLayoutParams();

        Log.d("jludden.reeflifesurvey"  ,"bottomMargin before: "+ layoutParams.bottomMargin);
        layoutParams.bottomMargin = (int) (layoutParams.bottomMargin + direction * fab.getHeight() * (1.7 * (fabIndex))); //add or subtract from the current bottom margin
        //layoutParams.bottomMargin = (int) (offset + direction * fab.getHeight() * (1.7 * (fabIndex))); //add or subtract from an offset - the estimated position of the main FAB

        //if(!animateIn) layoutParams.bottomMargin = 40;
        Log.d("jludden.reeflifesurvey"  ,"bottomMargin after: "+ layoutParams.bottomMargin);
        fab.setLayoutParams(layoutParams);
        fab.setClickable(animateIn);
        fab.setVisibility(animateIn ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * Show the initial survey site location in just the top part of the bottom sheet
     * TODO merge with expand bottom sheet? they should at the very least set the same info
     * @param siteInfo
     */
    public void peekBottomSheet(final SurveySiteList.SurveySite siteInfo) {
        //Set up the bottom sheet
        View bottomSheet = findViewById(R.id.bottom_sheet);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        if(bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        final TextView topText = (TextView) findViewById(R.id.bottom_sheet_top);
        topText.setText(siteInfo.getEcoRegion());
        topText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("jludden.reeflifesurvey"  , "Bottom Sheet Top TextView clicked.. hidden?: "+v.isShown());
                //todo refactor this
                //if the detailsviewfragment is showing, and they click the top of bottom sheet,
                //navigate them back to the mapview
                MapViewFragment mapFrag = (MapViewFragment) getSupportFragmentManager().findFragmentByTag(MapViewFragment.TAG);
                if (mapFrag != null && !mapFrag.isVisible()) {
                    Log.d("jludden.reeflifesurvey"  , "Bottom Sheet TEST1 PASSED");
                    mBottomSheetButton.setVisibility(View.VISIBLE);
                    launchUIFragment(mapFrag,MapViewFragment.TAG);
                }
                else if(mapFrag != null){ //launch browse fish details for this site
                    //AppBarLayout toolbar = (AppBarLayout) findViewById(R.id.app_bar);
                    //toolbar.setExpanded(true,true);
                    //addSiteLocationsToToolbar();
                    mFAB.show();
                    launchNewCardViewFragment(siteInfo.getCode());
                }
            }
        });
        //if added to favs, set the fab to remove

        //show some info like num sites etc
    }

    /**
     * Show the bottom sheet with full details about the survey site
     */
    public void expandBottomSheet(@Nullable SurveySiteList.SurveySite siteInfo) {
        //Set up the bottom sheet
        View bottomSheet = findViewById(R.id.bottom_sheet);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        if (siteInfo == null) return; //todo coming from mainactivity fab 2

        final TextView topText = (TextView) findViewById(R.id.bottom_sheet_top);
        final TextView bottomText = (TextView) findViewById(R.id.bottom_sheet_main_text);
        if (mSurveySiteList == null) mSurveySiteList = this.retrieveSurveySiteList();
        if ((bottomText == null) || (siteInfo == null) || (mSurveySiteList == null)) {
            Log.e("jludden.reeflifesurvey", "MainActivity^expandBottomSheet something null: " + (bottomText == null) + (siteInfo == null) + (mSurveySiteList == null));
            throw new NullPointerException("MainActivity^expandBottomSheet something null"); //todo error signature
        }

        //set up text fields
        topText.setText(siteInfo.getEcoRegion());
        StringBuilder details = new StringBuilder();
        details.append("Code (ID) : "+siteInfo.getCode()+" ("+siteInfo.getID()+")");
        details.append("\n SiteName "+siteInfo.getSiteName());
        details.append("\n EcoRegion "+siteInfo.getEcoRegion());
        details.append("\n Realm "+siteInfo.getRealm());
        details.append("\n Position: "+siteInfo.getPosition());
        details.append("\n Num Surveys "+siteInfo.getNumberOfSurveys());
        details.append("\n" + mSurveySiteList.codeList(siteInfo.getCode(), -1));
        bottomText.setText(details.toString()); //todo
        bottomText.setMovementMethod(new ScrollingMovementMethod());
        bottomText.scrollTo(0,0);

        //set up image carousel
        createImageCarousel(siteInfo);
    }

    //BottomSheet related:
    //helper function to load and display a fish carousel in the bottom sheet,
    //based on fish that can be found in the survey site
    //todo move to bottomsheet class
    private void createImageCarousel(@Nullable SurveySiteList.SurveySite siteInfo) {
        //todo show a fish carousel
        try{
            //Load fish cards
            List<InfoCard.CardDetails> fishCards = InfoCardLoader.loadSingleSite(siteInfo, this, 5);
            Log.d("jludden.reeflifesurvey", "BottomSheet fishCards loaded: "+fishCards.size());
            if(fishCards.size() > 0 ) Log.d("jludden.reeflifesurvey", "BottomSheet fishCards loaded: "+fishCards.get(0).commonNames);

            //add cards to carousel. consider doing this in a reactive way?
            mBottomSheetImageCarousel = (SliderLayout) findViewById(R.id.site_preview_carousel);
            mBottomSheetImageCarousel.removeAllSliders();

            for(InfoCard.CardDetails card : fishCards) {
                TextSliderView textSliderView = createCarouselEntry(card);
                mBottomSheetImageCarousel.addSlider(textSliderView);
            }
            //mDemoSlider.addOnPageChangeListener(this);

        } catch (JSONException e){
            Log.e("jludden.reefLifeSurvey" , "Failed to parse surveysitelist json. species found but corresponding sitings count could not be retrieved");
        }
    }

    //BottomSheet related:
    //creates a TextSliderView (a fish preview image, with description and onclick listener) that can be added to a SliderLayout (an image carousel of fish previews)
    private TextSliderView createCarouselEntry(final InfoCard.CardDetails card){
        String name = card.cardName;
        TextSliderView textSliderView = new TextSliderView(this);
        textSliderView
                .description(name)
                .image(card.getPrimaryImageURL())
                .setScaleType(BaseSliderView.ScaleType.Fit)
                .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                    @Override
                    public void onSliderClick(BaseSliderView slider) {
                        onFishDetailsRequested(card);
                    }
                });
        return textSliderView;
    }

    public FloatingActionButton[] getFABmenu(){
        return mFABmenu;
    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {
        //TODO delete (old stuff from countrylist)
    }

    /**
     * Called from CardViewFragmen
     * And from Carousel Entries
     * t to launch a details mode for the fish card
     * @param cardDetails
     */
    @Override
    public void onFishDetailsRequested(InfoCard.CardDetails cardDetails) {
        Log.d("jludden.reeflifesurvey", "MainActivity onFishDetailsRequested: "+cardDetails.toString());

        //hide a bunch of shit todo probably want at least a fab or a bottom bar, cant decide
        mFAB.hide();
        hideFABmenu();
        mBottomSheetButton.setVisibility(View.GONE);
        AppBarLayout toolbar = (AppBarLayout) findViewById(R.id.app_bar);
        toolbar.setExpanded(false,true);
        View bottomSheet = findViewById(R.id.bottom_sheet);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        if(bottomSheetBehavior.getState()==BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }


        Fragment newFragment = DetailsViewFragment.newInstance(cardDetails, "whatever");
        String tag = DetailsViewFragment.TAG;

        launchUIFragment(newFragment, tag);
    }

    //todo move to bottomsheet class
    @Override
    protected void onStop() {
        if(mBottomSheetImageCarousel != null) {
            mBottomSheetImageCarousel.stopAutoCycle(); //prevent a memory leak
        }
        super.onStop();
    }

    /**
     * Override the default dispatchTouchEvent to hide the keyboard from the search box, if it is up
     * todo there is another touch listener in this class that handles hiding the fab menu. reconcile?
     * @param ev motion event passed on to super
     * @return result from super.dispatch
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        SearchView searchView = (SearchView) findViewById(R.id.action_search);
        searchView.clearFocus();
        return super.dispatchTouchEvent(ev);
    }

    /**
     * Called when the checked state of a compound button has changed.
     *
     * @param buttonView The compound button view whose state has changed.
     * @param isChecked  The new checked state of buttonView.
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d("jludden.reeflifesurvey","button pressed: "+buttonView.getId()+" is checked: "+isChecked);
        CardViewFragment viewFragment = (CardViewFragment) getSupportFragmentManager().findFragmentByTag(CardViewFragment.TAG);

        switch(buttonView.getId()){
            case(R.id.toolbar_button_starred):
                CardViewSettings.FILTER_FAVORITES = isChecked;
                if (viewFragment != null && viewFragment.isVisible()) {
                    if(CardViewSettings.LOAD_ALL) viewFragment.onFilterApplied(); //already have everything loaded, just apply filters
                    else {
                        CardViewSettings.LOAD_ALL = true;
                        viewFragment.onLoadMore(false);
                    }
                }
                break;
            case(R.id.toolbar_button_loadAll):
                CardViewSettings.LOAD_ALL = isChecked;
                if (viewFragment != null && viewFragment.isVisible()) {
                    viewFragment.onLoadMore(false);
                }
                break;
            default: //handle survey site location button pressed
                Log.d("jludden.reeflifesurvey","unhandled toggle button (#"+buttonView.getId()+") pressed: "+buttonView.getText());
                if(buttonView.getTag() != null) {
                    //SurveySiteList.SurveySite site = (SurveySiteList.SurveySite) buttonView.getTag();
                    String siteCode = (String) buttonView.getTag();
                    Log.d("jludden.reeflifesurvey", "toggle button tag: " + siteCode + " is checked: " + buttonView.isChecked());
                    if (buttonView.isChecked()) retrieveSurveySiteList().saveFavoriteSite(siteCode, getBaseContext()); //update saved sites in datafragment
                    else retrieveSurveySiteList().removeFavoriteSite(siteCode, getBaseContext());
                    if (viewFragment != null && viewFragment.isVisible()) { //reload card view fragment
                        viewFragment.onLoadMore(true);
                    }
                }

        }
    }

    /**
     * Called by selecting an item in the settings popup menu
     *
     * This method will be invoked when a menu item is clicked if the item
     * itself did not already handle the event.
     *
     * @param item the menu item that was clicked
     * @return {@code true} if the event was handled, {@code false}
     * otherwise
     *
     * todo implement options
     */
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch(item.getItemId()){
            case(R.id.settings_opt_hide_menus):
                hideClutter();
                return true;
            case(R.id.settings_opt_del_favorite_sites):
                return true;
            case(R.id.settings_opt_del_favorite_species):
                return true;
            case(R.id.settings_opt_about):
                //todo
                return true;
        }

        return false;
    }

    // jump to full screen mode yeeaah
    public void hideClutter(){
        mFAB.hide();
        hideFABmenu();
        mBottomSheetButton.setVisibility(View.GONE);
        View bottomSheet = findViewById(R.id.bottom_sheet);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        //trying everything to get this bar to hide
        getSupportActionBar().hide();
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setVisibility(View.GONE);


        AppBarLayout toolbar = (AppBarLayout) findViewById(R.id.app_bar);
        toolbar.setExpanded(false,true);
        //toolbar.setVisibility(View.GONE);

        View mDecorView;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mDecorView = getWindow().getDecorView();
            mDecorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
            );
        }
                /*                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

                * | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                *
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE
                *
                * */
    }
}
