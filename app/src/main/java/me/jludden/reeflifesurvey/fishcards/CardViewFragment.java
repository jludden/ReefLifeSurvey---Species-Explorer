package me.jludden.reeflifesurvey.fishcards;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;

import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import me.jludden.reeflifesurvey.Injection;
import me.jludden.reeflifesurvey.MainActivity;
import me.jludden.reeflifesurvey.data.DataRepository;
import me.jludden.reeflifesurvey.data.InfoCardLoader;
import me.jludden.reeflifesurvey.data.utils.StorageUtils;
import me.jludden.reeflifesurvey.data.model.SurveySiteList;
import me.jludden.reeflifesurvey.data.SurveySiteType;
import me.jludden.reeflifesurvey.R;
import me.jludden.reeflifesurvey.data.model.FishSpecies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static me.jludden.reeflifesurvey.data.utils.LoaderUtils.checkInternetConnection;

/**
 * A fragment with a Google +1 button.
 * Activities that contain this fragment must implement the
 * {@link OnCardViewFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CardViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CardViewFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<List<FishSpecies>>,
        DataRepository.LoadSurveySitesCallBack,
        CompoundButton.OnCheckedChangeListener {
    private static final String TYPE_TO_LOAD = "card_type_to_load";
    private static final String OPT_SURVEY_SITE = "optional_survey_site";
    public static final String TAG = "FishCardView";
    private static final int LOADER_ID = 0;

    private CardType mCardType;
    private FloatingActionButton mFloatingActionButton;
    public RecyclerView mRecyclerView;
    private CardViewAdapter mViewAdapter;
    private LinearLayoutManager mLayoutManager;
    private OnCardViewFragmentInteractionListener mListener;
    private View mProgressBar;

    private onDataLoadedCallback mDataLoadedCallback; //todo list?

    private boolean mIsLoading = true;
    private int mPreviousTotalItemCount = 0;
    private boolean mLoadedAll = false;
    private String mPassedInSurveySiteCode = "";//optional passed in parameter. otherwise load favorite sites
    private SurveySiteList mSurveySiteList;
    private int SCROLLING_VISIBLE_ITEM_THRESHOLD = 3;

    public enum CardType{
        Fish,
        Countries
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity
     */
    public interface OnCardViewFragmentInteractionListener {
        void onFishDetailsRequested(FishSpecies cardDetails, View sharedElement);

    }

    public CardViewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param typeToLoad Parameter 1.
     * @param siteCode Parameter 2. OPTIONAL.
     *                 if null, will load favorited sites
     *                 if non-null, will load all sites matching this code
     * @return A new instance of fragment CardViewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CardViewFragment newInstance(CardType typeToLoad, @Nullable String siteCode) {
        CardViewFragment fragment = new CardViewFragment();
        Bundle args = new Bundle();
        args.putInt(TYPE_TO_LOAD, typeToLoad.ordinal());
        args.putString(OPT_SURVEY_SITE, siteCode);
//        args.putParcelable(ARG_CARD, cardDetails);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCardViewFragmentInteractionListener) {
            mListener = (OnCardViewFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCardViewFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCardType = CardType.values()[getArguments().getInt(TYPE_TO_LOAD, 0)]; //wow why is getting an enum from an int so difficult
            mPassedInSurveySiteCode = getArguments().getString(OPT_SURVEY_SITE);
        }

        checkInternetConnection(getActivity(), "www.reeflifesurvey.com", R.string.no_internet_detected);
        setHasOptionsMenu(true);

        Log.d("jludden.reeflifesurvey"  ,"CardViewFragment Created. Card type: "+mCardType);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("jludden.reeflifesurvey"  ,"CardViewFragment OnActivityCreated");
        getLoaderManager().initLoader(LOADER_ID, null, this);
        animateView(mProgressBar, View.VISIBLE, 0.4f, 200);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d("jludden.reeflifesurvey"  ,"CardViewFragment View Created");

        View view = inflater.inflate(R.layout.card_view_fragment, container, false);

        Log.d("jludden.reeflifesurvey"  ,"CardViewFragment view instanceof recyclerview!");

        Context context = view.getContext();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_card_list);
        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mViewAdapter = new CardViewAdapter(this, mRecyclerView, mListener);
        mRecyclerView.setAdapter(mViewAdapter);

        mProgressBar = view.findViewById(R.id.progress_overlay);

        //Set OnScroll listener
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            /**
             * Callback for when the card list is scrolled. If we are reaching the end of the list, load more cards
             * @param recyclerView The RecyclerView which scrolled.
             * @param dx The amount of horizontal scroll.
             * @param dy The amount of vertical scroll.
             */
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = recyclerView.getChildCount();
                int totalItemCount = mLayoutManager.getItemCount();
                int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

                //Log.d("jludden.reeflifesurvey"  ,"CardViewfragment onScrolled! "+visibleItemCount+" tot"+totalItemCount+" first: "+firstVisibleItem+" isloading:"+mIsLoading);
                if (mIsLoading) {
                    if (totalItemCount > mPreviousTotalItemCount) {
                        mIsLoading = false;
                        mPreviousTotalItemCount = totalItemCount;
                    }
                }
                if (!mIsLoading && (totalItemCount - visibleItemCount)
                        <= (firstVisibleItem + SCROLLING_VISIBLE_ITEM_THRESHOLD)) {
                    onLoadMore(false);
                    mIsLoading = true;
                }
            }

        });

        Injection.provideDataRepository(getContext().getApplicationContext()).getSurveySites(SurveySiteType.CODES, this);

        return view;
    }

    /**
     * Triggers the loader to load more data
      */
    public void onLoadMore(boolean forceReload){
        Log.d("jludden.reeflifesurvey"  ,"CardViewfragment onLoadMore: loadall? "+CardViewSettings.LOAD_ALL);

        if(forceReload){ //survey sites changed, restart everything
            // Show progress overlay (with animation):
            animateView(mProgressBar, View.VISIBLE, 0.4f, 200);

            getLoaderManager().restartLoader(LOADER_ID, null, this); //will call onCreateLoader again, with the loadAll parameter passed in
            return;
        }

        if(mLoadedAll){ //everything is already loaded
            Log.d("jludden.reeflifesurvey"  ,"CardViewfragment onLoadMore suppressed (everything already loaded)");

            //onloadfinished wont be called again - pass back card list now
            if(mDataLoadedCallback != null){
                mDataLoadedCallback.onCardsLoaded( mViewAdapter.getCardList());
            }

            return;
        }

        if(CardViewSettings.LOAD_ALL){ //filters must have been applied, load all
            mLoadedAll = true;
            getLoaderManager().restartLoader(LOADER_ID, null, this); //will call onCreateLoader again, with the loadAll parameter passed in
        }
        else getLoaderManager().getLoader(LOADER_ID).onContentChanged();
    }

    //onloadMore with a callback
    public void loadAll(boolean forceReload, onDataLoadedCallback callback){
        mDataLoadedCallback = callback;
        onLoadMore(forceReload);
    }

    /**
     * Thanks to Jonik
     *  https://stackoverflow.com/questions/18021148/display-a-loading-overlay-on-android-screen
     *  Todo this would go great in a utils class
     * @param view         View to animate
     * @param toVisibility Visibility at the end of animation
     * @param toAlpha      Alpha at the end of animation
     * @param duration     Animation duration in ms
     */
    public static void animateView(final View view, final int toVisibility, float toAlpha, int duration) {
        if(view == null) Log.e("jludden", "Trying to animate NULL VIEW");

        boolean show = toVisibility == View.VISIBLE;
        if (show) {
            view.setAlpha(0);
        }
        view.setVisibility(View.VISIBLE);
        view.animate()
                .setDuration(duration)
                .alpha(show ? toAlpha : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(toVisibility);
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        AppBarLayout toolbar = (AppBarLayout) getActivity().findViewById(R.id.app_bar);
//        AppBarLayout toolbar = (AppBarLayout) getActivity().findViewById(R.id.app_bar);
//        CollapsingToolbarLayout toolbar2 = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);
//        Toolbar toolbar3 = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setExpanded(false, false);
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) toolbar.getLayoutParams();
        lp.height = -2; //wrap content
        ////         android:layout_height="@dimen/app_bar_height_expanded"
//        getResources().getDimension(R.dimen.app_bar_height_expanded
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<List<FishSpecies>> onCreateLoader(int id, Bundle args) {
        Log.d("jludden.reeflifesurvey"  ,"CardViewFragment OnCreateLoader");
        return new InfoCardLoader(getActivity(), mCardType, mPassedInSurveySiteCode);
    }

    /**
     * Called when a previously created loader has finished its load.  Note
     * that normally an application is <em>not</em> allowed to commit fragment
     * transactions while in this call, since it can happen after an
     * activity's state is saved.  See {@link FragmentManager#beginTransaction()
     * FragmentManager.openTransaction()} for further discussion on this.
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<List<FishSpecies>> loader, List<FishSpecies> data) {

        //update cards from the sharedpreferences data
     /*   for(FishSpecies card : data){
            if(getPref(card.getId(),InfoCard.PREF_FAVORITED)) card.favorited = true;
        }*/
        Log.d("jludden.reeflifesurvey"  ,"CardViewfragment onLoadFinished - still need to apply filter and update adapter ");

        onFilterApplied(); //reapply any filters

        int iCountPrev = mViewAdapter.getItemCount();
        mViewAdapter.updateItems(data); //update items in the adapter
        int iCountAfter = mViewAdapter.getItemCount();

        Log.d("jludden.reeflifesurvey"  ,"CardViewfragment onLoadFinished and adapter updated. loaderid: "+loader.getId()+" data length: "+data.size() + " adapter item count increased from "+iCountPrev+" to "+iCountAfter);

        // remove progress bar
        animateView(mProgressBar, View.GONE, 0, 200);

        if(mDataLoadedCallback != null) mDataLoadedCallback.onCardsLoaded(data);
    }

    /**
     * Store the current configuration of sites and all their relevant fish images to disk
     */
    public void storeInLocal() {

        CardViewSettings.LOAD_ALL = true;
        loadAll(false, new onDataLoadedCallback() {
            @Override
            public void onCardsLoaded(List<FishSpecies> cards) {
                saveOfflineHelper(cards);
            }
        });
    }

    private void saveOfflineHelper(List<FishSpecies> cards) {

        //todo refactor. this logic in both infocardloader and cardviewfragment
        List<String> siteCodes;
        if (mPassedInSurveySiteCode.equals("")) siteCodes = mSurveySiteList.getFavoritedSiteCodes();
        else {
            siteCodes = new ArrayList<>();
            siteCodes.add(mPassedInSurveySiteCode);
        }

        StorageUtils.Companion.promptToSaveOffline(
               cards, siteCodes, getActivity());
    }


    /**
     * Applies any filters to the list of card
     * at this point, can assume all cards are loaded
     * constraints are stored in the static class CardViewSettings
     *
     */
    public void onFilterApplied(){
        Log.d(TAG,"apply filter Fragment");

        //constraint = CardViewSettings.FILTER_FAVORITES ? "F" : "";

       mViewAdapter.getFilter().filter(CardViewSettings.SEARCH_CONSTRAINT); //using standard filterable interface
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<List<FishSpecies>> loader) {

    }

    @Override
    public void onSurveySitesLoaded(@NotNull SurveySiteList sites) {
        mSurveySiteList = sites;

        // todo update to add th actual sites being searched. like this doesnt work when we are looking at a single site
        addSiteLocationsToToolbar(sites.getFavoritedSiteCodes());

        addStaticMapToToolbar(sites);
    }

    /**
     * Set the expandable toolbar buttons to include the site locations
     * called when launching CardViewFragment
     */
    public void addSiteLocationsToToolbar(List<String> siteCodeList) {
        LinearLayout toolbarLayout = (LinearLayout) getActivity().findViewById(R.id.toolbar_layout);
        //todo try getSupportActionBar
        if(toolbarLayout == null ) return;
        int offset = 1;//TODO this is the number of buttons before the survey sites

        //remove the previously added site buttons
        int childCount = toolbarLayout.getChildCount()-offset;
        if(childCount > 0) {
            toolbarLayout.removeViews(offset, childCount);
        }

        int count = 0;
        for(String siteCode : siteCodeList){
            ToggleButton siteButton = new ToggleButton(getContext());
            siteButton.setId(offset + count++);
            siteButton.setTextOn(siteCode);
            siteButton.setChecked(true); //default state true
            siteButton.setTextOff("("+siteCode+")");
            siteButton.setTag(siteCode); //store the site associated with this button
            siteButton.setOnCheckedChangeListener(this);
            toolbarLayout.addView(siteButton);
        }
    }

    public void addStaticMapToToolbar(SurveySiteList sites) {
        Log.d(TAG, "launchNewCardViewFragment: loading map url");
        final ImageView topImage = getActivity().findViewById(R.id.collapsing_toolbar_image);

        String siteCode;
        LatLng pos;

        if (!mPassedInSurveySiteCode.equals("")){ //TODO please refactor we shouldnt do this logic everywhere
            siteCode = mPassedInSurveySiteCode;
            pos = sites.getSitesForCode(siteCode).get(0).getPosition();
        }
        else {

            List<String> favCodes = sites.getFavoritedSiteCodes();

            if(favCodes.size() != 1){ //can't handle multiple sites yet
                topImage.setImageDrawable(getActivity().getDrawable(R.drawable.rls_logo_horizontal_rev));
                return;
            }

       /* for(String code : sites.getFavoritedSiteCodes()){
            sites.
        }
        siteCodeList.*/


            siteCode = favCodes.get(0);
            pos = sites.getSitesForCode(siteCode).get(0).getPosition();
        }

        //loading static map
        //fixed size to fit in expandable toolbar (TODO)
        //add markers + label:CODE
//        StringBuilder mapsURL = new StringBuilder();
        String mapsURL = "https://maps.googleapis.com/maps/api/staticmap?\n" +
                "size=400x200\n" +
                "&maptype=terrain\n" +
                "&zoom=5\n" +
                "&markers=color:red%7Clabel:" + siteCode + "%7C" + pos.latitude + "," + pos.longitude + "\n" +

                "&key=" + getString(R.string.google_maps_key);
/*                "&markers=color:blue%7Clabel:S%7C40.702147,-74.015794\n" +
                "&markers=color:green%7Clabel:G%7C40.711614,-74.012318\n" +
                "&markers=color:red%7Clabel:C%7C40.718217,-73.998284\n" +*/

        Log.d(TAG, "launchNewCardViewFragment: loading map url. latlng: " + pos.latitude + "," + pos.longitude
         + "\n full url: " + mapsURL);

        Picasso.with(getContext()).load(mapsURL).into(topImage);
    }

    @Override
    public void onDataNotAvailable(@NotNull String reason) {
        Log.d(TAG,"onDataNotAvailable - unable to add survey sites to toolbar");
    }

    /**
     * Called when the checked state of a compound button has changed.
     *
     * @param buttonView The compound button view whose state has changed.
     * @param isChecked  The new checked state of buttonView.
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG,"unhandled toggle button (#"+buttonView.getId()+") pressed: "+buttonView.getText());
        if(buttonView.getTag() != null && mSurveySiteList != null) {
            //SurveySiteList.SurveySite site = (SurveySiteList.SurveySite) buttonView.getTag();
            String siteCode = (String) buttonView.getTag();
            Log.d(TAG, "toggle button tag: " + siteCode + " is checked: " + buttonView.isChecked());
            if (buttonView.isChecked())
                mSurveySiteList.addFavoriteSite(siteCode, getContext()); //update saved sites in datarepo
            else mSurveySiteList.removeFavoriteSite(siteCode, getContext());
            onLoadMore(true);
        }
    }

    //region override options menu for card fragment specific toolbar items
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.cardview_toolbar, menu); //main activity menu already inflated - add to it
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) { //handle items in cardview_toolbar. rest handled in MainActivity
            case R.id.action_fullscreen:
                Log.d(TAG,"options select action fullscreen");
                ((MainActivity) getActivity()).launchFullScreenQuizModeActivity();
                return true;
            case R.id.action_download_site:
                Log.d(TAG,"options select action download");
                storeInLocal();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //endregion

    public static class CardViewSettings {
        public static boolean LOAD_ALL = false;
        public static boolean FILTER_FAVORITES = false;
        public static String SEARCH_CONSTRAINT = "";
    }

    //can currently call a single callback after the data is finished loading
    interface onDataLoadedCallback {
        void onCardsLoaded(List<FishSpecies> cards);
    }

}
