package me.jludden.reeflifesurvey.BrowseFish;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;

import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.jludden.reeflifesurvey.R;
import me.jludden.reeflifesurvey.BrowseFish.model.InfoCard;
import me.jludden.reeflifesurvey.BrowseFish.model.InfoCard.CardDetails;

import java.util.List;

/**
 * A fragment with a Google +1 button.
 * Activities that contain this fragment must implement the
 * {@link OnCardViewFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CardViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CardViewFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<CardDetails>> {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TYPE_TO_LOAD = "param1";
    private static final String OPT_SURVEY_SITE = "param2";
    public static final String TAG = "FishCardView";
    public static final String TITLE = "Browse Fish";
    private static final int LOADER_ID = 0;

    // TODO: Rename and change types of parameters
    private CardType mCardType;
    private String mParam2;
    private FloatingActionButton mFloatingActionButton;
    private RecyclerView mRecyclerView;
    private CardViewAdapter mViewAdapter;
    private LinearLayoutManager mLayoutManager;
    private OnCardViewFragmentInteractionListener mListener;
    private View mProgressBar;

    //some not great stuff to enable loading all or loading incrementally
    //private boolean mMoreToLoad = true;

    //stuff to track scrolling
    private boolean mIsLoading = true;
    private int mCurrentPage = 0;
    private int mPreviousTotalItemCount = 0;
//    private boolean mFilterFavorites = false;
    private boolean mLoadedAll = false;
    private String mPassedInSurveySiteCode = "";//optional passed in parameter. otherwise load favorite sites


    public enum CardType{
        Fish,
        Countries
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnCardViewFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFishDetailsRequested(InfoCard.CardDetails cardDetails, View sharedElement);
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCardType = CardType.values()[getArguments().getInt(TYPE_TO_LOAD, 0)]; //wow why is getting an enum from an int so difficult
            mPassedInSurveySiteCode = getArguments().getString(OPT_SURVEY_SITE);
        }

        Log.d("jludden.reeflifesurvey"  ,"CardViewFragment Created. Card type: "+mCardType);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("jludden.reeflifesurvey"  ,"CardViewFragment OnActivityCreated");
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d("jludden.reeflifesurvey"  ,"CardViewFragment View Created");

        View view = inflater.inflate(R.layout.card_view_fragment, container, false);

        Log.d("jludden.reeflifesurvey"  ,"CardViewFragment view instanceof recyclerview!");

        Context context = view.getContext();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.jason_cards);
        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mViewAdapter = new CardViewAdapter(this, InfoCard.ITEMS, mRecyclerView, mListener); //todo reconcile infocard.items with the onloadfinished(arraylist<InfoCard.CardDetails>)
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
                int previousTotal = 20; //todo
                int visibleThreshold = 3; //todo
                //Log.d("jludden.reeflifesurvey"  ,"CardViewfragment onScrolled! "+visibleItemCount+" tot"+totalItemCount+" first: "+firstVisibleItem+" isloading:"+mIsLoading);


                if (mIsLoading) {
                    if (totalItemCount > mPreviousTotalItemCount) {
                        mIsLoading = false;
                        mPreviousTotalItemCount = totalItemCount;
                    }
                }
                if (!mIsLoading && (totalItemCount - visibleItemCount)
                        <= (firstVisibleItem + visibleThreshold)) {
                    onLoadMore(false);
                    mIsLoading = true;
                }
            }

        });

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
            return;
        }

        if(CardViewSettings.LOAD_ALL){ //filters must have been applied, load all
            mLoadedAll = true;
            getLoaderManager().restartLoader(LOADER_ID, null, this); //will call onCreateLoader again, with the loadAll parameter passed in
        }
        else getLoaderManager().getLoader(LOADER_ID).onContentChanged();
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
    }

    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFishDetailsRequested(uri);
//        }
//    }

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
    public Loader<List<CardDetails>> onCreateLoader(int id, Bundle args) {
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
    public void onLoadFinished(Loader<List<CardDetails>> loader, List<CardDetails> data) {

        //update cards from the sharedpreferences data
     /*   for(CardDetails card : data){
            if(getPref(card.getId(),InfoCard.PREF_FAVORITED)) card.favorited = true;
        }*/
        onFilterApplied(); //reapply any filters

        int iCountPrev = mViewAdapter.getItemCount();
        mViewAdapter.updateItems(data); //update items in the adapter
        int iCountAfter = mViewAdapter.getItemCount();

        Log.d("jludden.reeflifesurvey"  ,"CardViewfragment onLoadFinished loaderid: "+loader.getId()+" data length: "+data.size() + " adapter item count increased from "+iCountPrev+" to "+iCountAfter);


        // Hide it (with animation):
        animateView(mProgressBar, View.GONE, 0, 200);

        //try to pass data back to the main activity
        //edit - this can be done better by using a retained, headless fragment
//        Intent intent = new Intent(getActivity().getBaseContext(),
//                MainActivity.class);
//        intent.putExtra("cardInfo", data.get(0)); //TODO put the whole list?
       // getActivity().startActivity(intent); TODO can i pass the data without starting the activity again?
        // (MainActivity) getActivity().setCardDetails(data)
    }



    /**
     * Applies any filters to the list of card
     * at this point, can assume all cards are loaded
     * constraints are stored in the static class CardViewSettings
     *
     */
    public void onFilterApplied(){
        Log.d("jludden.reeflifesurvey","apply filter Fragment");

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
    public void onLoaderReset(Loader<List<CardDetails>> loader) {

    }

    public static class CardViewSettings {
        public static boolean LOAD_ALL = false;
        public static boolean FILTER_FAVORITES = false;
        public static String SEARCH_CONSTRAINT = "";
    }

}
