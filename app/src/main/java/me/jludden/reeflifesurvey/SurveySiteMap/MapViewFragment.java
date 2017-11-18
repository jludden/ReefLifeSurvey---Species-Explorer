package me.jludden.reeflifesurvey.SurveySiteMap;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

import me.jludden.reeflifesurvey.Data.DataRepository;
import me.jludden.reeflifesurvey.Data.SurveySiteList;
import me.jludden.reeflifesurvey.Data.SurveySiteList.SurveySite;
import me.jludden.reeflifesurvey.Data.SurveySiteType;
import me.jludden.reeflifesurvey.MainActivity;
import me.jludden.reeflifesurvey.R;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Created by Jason on 5/15/2017.
 *
 * TODO
 *  10/23: refactor. map+surveysitelist model into new folder
 *      3 different colors for map icons: new, added, selected. DONT HARDCODE?
 *          new - red marker? or maybe a custom icon
 *          favorites - star
 *          currently selected - adding a red marker to the currently selected location could be cool, or whatever
 *
 *  11/1:
 *      Update UI. when just viewing, can have a minimized or no bottom sheet showing
 *      when you click on a survey location, immediately show the data within the bottom sheet - no more marker menus
 *      bottom sheet buttons:
 *          have a favorites/unfavorites button within the bottom sheet (I think within the sheet)
 *          have the FAB be a direct link to the browse fish for this survey site location!!!!
 *          Maybe another button that links to a relevant reeflifesurvey.com website?
 *          Maybe a share button?
 *
*       bottom sheet behavior:
 *          collapses - (will popup when selecting a marker).
 *              200-300dp, with name, 2/3 buttons + FAB, ecoregion, etc, survey sites
 *          expanded -
 *              full survey site list, Fish preview gallery, probably 70% of the screen (small map at the top for reference)
 *          full-screen
 *              More of expanded with no map showing
 *
 */

public class MapViewFragment extends Fragment
        implements OnMapReadyCallback,
            GoogleMap.OnMapClickListener,
            GoogleMap.OnMarkerClickListener,
            DataRepository.LoadSurveySitesCallBack {

    private static final float FAVORITED_SITE_COLOR = BitmapDescriptorFactory.HUE_AZURE;
    private static final float NORMAL_SITE_COLOR = BitmapDescriptorFactory.HUE_RED;

    private MapViewFragment mapFragment; //todo just use fragment manager
    View rootView;
    private GoogleMap mMap;
    private MapView mMapView;
   // private MapCallback mMapCallback;
    private Marker mSelectedMarker;
    private ArrayList<Marker> mSelectedSiteList = new ArrayList<>(); //todo use static selected site list //todo save these preferences //11/16 TODO DELETE
    private FloatingActionButton mFAB;
    private FloatingActionButton[] mFABmenu;

    public static final String TAG = "MapViewFragment";

    //data retrieval from the retained, headless fragment
    private SurveySiteList mSurveySiteList;

    private MapViewFragmentInteractionListener mMapViewFragmentInteractionListener;

    public interface MapViewFragmentInteractionListener{
        void peekBottomSheet(SurveySite siteInfo);

        FloatingActionButton getFloatingActionButton(); //// TODO: 9/14/2017
        //i would rather, instead of getting the whole button, pass back to the activity the two different actions:
        //1. site selected , show summary, update fab icon (starred or unstarred)
        //2. site details, show details, bottom sheet
    }

    public MapViewFragment() {
    }

    public static MapViewFragment newInstance() {
        MapViewFragment mapFragment = new MapViewFragment();
        mapFragment.setRetainInstance(true);
        return mapFragment;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        DataRepository.Companion.getInstance(getContext().getApplicationContext())
                .getSurveySites(SurveySiteType.CODES,this);

        String errMsg = "";
        if(context instanceof MapViewFragmentInteractionListener){
            mMapViewFragmentInteractionListener = (MapViewFragmentInteractionListener) context;
        } else {
            errMsg += "\n" + context.toString() + "must implement" +
                    MapViewFragmentInteractionListener.class.getName();
        }
        if(errMsg.length() > 0){
            throw new ClassCastException(errMsg);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG  ,"map view fragment created");

        MainActivity main = (MainActivity) getActivity();
        //mFAB = main.getFAB(); //TODO
//        mFAB = mMapViewFragmentInteractionListener.getFloatingActionButton();
       // mFAB = main.getFloatingActionButton();

        //make sure these are being cleaned up in onDestroy TODO
//        mFABmenu = main.getFABmenu();
//
//        mFABmenu[0].setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d("jludden.reeflifesurvey"  ,"mMiniFab1 clicked");
//                hideFABmenu();
//            }
//        });
//
//        mFABmenu[1].setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d("jludden.reeflifesurvey"  ,"mMiniFab2 clicked");
//                hideFABmenu();
//            }
//        });

//        setContentView(R.layout.maps_view_fragment);
//        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        Log.d("jludden.reeflifesurvey"  , "MapViewFragment onCreateView. Has rootview: "+(rootView==null));

        if(rootView == null){
            rootView = inflater.inflate(R.layout.maps_view_fragment, null, false); //pass in viewgroup?
        }

//        rootView.seton

//        mMapView = (MapView) rootView.findViewById(R.id.fragment_map);
//        mMapView.getMapAsync(this);
//        mMapView.onCreate(getArguments());

//        if(mMapCallback==null) {
//            mMapCallback.onMapReady(((SupportMapFragment)
//                    getFragmentManager().findFragmentById(R.id.fragment_map)).getMapAsync(this));
//        }

//        SupportMapFragment mapFragment  = ((SupportMapFragment) getFragmentManager().findFragmentById(
//                R.id.fragment_map));
//        mapFragment.getMapAsync(this);

//        mMap = ((SupportMapFragment) getFragmentManager().findFragmentById(
//                R.id.fragment_map)).getMapAsync(this);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: ");
        if(mMap == null) {
            mMapView = (MapView) rootView.findViewById(R.id.fragment_map);
            mMapView.getMapAsync(this);
            mMapView.onCreate(getArguments());
        }
        if(mFAB == null){
            mFAB = mMapViewFragmentInteractionListener.getFloatingActionButton();
        }

    }

    /**
     * Manipulates the map once available.
     * This mMapCallback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("jludden.reeflifesurvey"  ,"SurveySiteListLoader OnMapReady()");
        mMap = googleMap;

        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);
        //mMap.setOnInfoWindowClickListener(this);


        //todo set map settings
        //mMap.getUiSettings().setZoomControlsEnabled(true);

        //by default, when a marker on the map is clicked, a toolbar will popup
        //this toolbar links to the actual google maps app and has a directions button
        mMap.getUiSettings().setMapToolbarEnabled(false);

        if(mSurveySiteList != null) {
            addSurveySites();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mMapView != null)
            mMapView.onStart();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.v("ludden.reeflifesurvey" , "MapViewFragment onMapClick latlng: "+latLng.toString());

        if(mSelectedMarker != null) {
            mSelectedMarker.setIcon((BitmapDescriptorFactory.defaultMarker(NORMAL_SITE_COLOR)));
        }
        mSelectedMarker = null;
        mFAB.setImageResource(R.drawable.ic_add_white);
        //todo delete ic_add_location_black_24dp, ic_add_loc_2,
        View bottomSheet = getActivity().findViewById(R.id.bottom_sheet);
        BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        if(bottomSheetBehavior.getState()==BottomSheetBehavior.STATE_EXPANDED)
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        //hideFABmenu();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        //todo only use peekbottomsheet for this method. rest is junk

        if(mSelectedMarker != null) {
            mSelectedMarker.setIcon((BitmapDescriptorFactory.defaultMarker(NORMAL_SITE_COLOR)));
        }

        Log.d("ludden.reeflifesurvey" , "MapViewFragment onMarkerClick marker: "+ marker.toString());
        mSelectedMarker = marker;
        mSelectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        //hideFABmenu();
        mFAB.setImageResource(R.drawable.ic_fab_add_loc); //todo animate and change? //todo would a favorites star be a better icon?

        mMapViewFragmentInteractionListener.peekBottomSheet((SurveySite) marker.getTag());//todo only this


        //todo want to center the map, but it doesnt work so well
        //if the bottom sheet is hiding most of the map.
        //could do some math and look at where the bottom sheet is at to find a better center spot
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition())); //center map on marker
        return true; //will not show the default infowindow
    }

    // 11/10/17 will no longer show the default window and will just use the bottom sheet
    /**
     * Launch the activities bottom sheet for additional site info
     * @param marker
     */
    /*@Override
    public void onInfoWindowClick(Marker marker) {
        Log.d("jludden.reeflifesurvey"  , "MapViewFragment onInfoWindowClick marker: "+ marker.toString());
        //Snackbar.make(mMapView, "clicked", Snackbar.LENGTH_SHORT).show();
        marker.hideInfoWindow();
        mMapViewFragmentInteractionListener.expandBottomSheet((SurveySite) marker.getTag());
        //((MainActivity) getActivity()).expandBottomSheet((SurveySite) mSelectedMarker.getTag());
    }*/

    /**
     * Called when the floating action button is pressed
     * If a marker is currently select, add it to the currently active site list
     * Else, show additional buttons for more options
     *  todo - one option to show the currently active sites
     *  todo - another option to switch currently active site lists
     *  todo probably dont need both mSelectedSiteList and SurveySiteList.SELECTED_SURVEY_SITES
     *  TODO - ADD Other sites with the same location?
     *      TODO - have info window to expand sites at location, choose individually based on name
     */
    public void onFABclick() {
        Log.d("jludden.reeflifesurvey"  ,"map view fragment fab clicked. markersel? "+(mSelectedMarker!=null));

        if(mSelectedMarker != null) { //Add marker to sitelist as appropriate
            //TODO animate the FAB being clicked here as well
            if (mSelectedSiteList.contains(mSelectedMarker)) {
                Snackbar.make(mMapView, mSelectedMarker.getTitle()+" is already in the site list" , Snackbar.LENGTH_LONG).show();
                //todo should this remove? how else to remove an icon
            } else {
                mSelectedSiteList.add(mSelectedMarker); //todo save this to preferences 11/16 TODO DELETE
                String siteCode = ((SurveySite) mSelectedMarker.getTag()).getCode();
                mSurveySiteList.saveFavoriteSite(siteCode, getContext());
                //mSurveySiteList.SELECTED_SURVEY_SITES.add((SurveySite) mSelectedMarker.getTag()); //todo why both REMOVE
                mSelectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(FAVORITED_SITE_COLOR));
                mSelectedMarker.hideInfoWindow();
                mSelectedMarker = null;
                mFAB.setImageResource(R.drawable.ic_add_white);

                Snackbar.make(mMapView, "Added to site list", Snackbar.LENGTH_LONG)
                        .setAction("Undo", new android.view.View.OnClickListener() {

                            /**
                             * Called when a view has been clicked.
                             *
                             * @param v The view that was clicked.
                             */
                            @Override
                            public void onClick(View v) {
                                MapViewFragment.this.removeLastMarker(); // fancy classname.this (qualified this) syntax
                                Snackbar.make(mMapView, "Removed", Snackbar.LENGTH_SHORT).show();
                            }
                        }).show();

               }
        }
        else{ //display a list of menu options from the floating action button
            //displayFABmenu();
             Snackbar.make(mMapView, "Select a location to add it to the list" , Snackbar.LENGTH_LONG).show();
        }

        Log.d("jludden.reeflifesurvey"  ,"map view fragment fab clicked. markerlist: "+ mSelectedSiteList.toString());
    }

    private void displayFABmenu(){
        ((MainActivity) getActivity()).showFABmenu();
    }

    /**
     * hides and resets the position of the mini FAB menu elements
     */
    private void hideFABmenu(){
        ((MainActivity) getActivity()).hideFABmenu();
    }

    /**
     * Remove the last marker added
     */
    private void removeLastMarker(){
        Marker lastMark = mSelectedSiteList.remove(mSelectedSiteList.size()-1);
        lastMark.setIcon((BitmapDescriptorFactory.defaultMarker(NORMAL_SITE_COLOR)));
        String siteCode = ((SurveySite) lastMark.getTag()).getCode();
        mSurveySiteList.removeFavoriteSite(siteCode, getContext());

        //todo remove from preferences list
    }

    @Override
    public void onSurveySitesLoaded(@NotNull SurveySiteList sites) {
        mSurveySiteList = sites;
        if(mMap != null) { //make sure that onMapReady has already been called
            addSurveySites();
        }
    }

    @Override
    public void onDataNotAvailable(@NotNull String reason) {

    }

    //Add the loaded survey sites to the map
    //called both when the map has finished loading and the datafragment has finished loading
    private void addSurveySites() {
        if (mMap == null) {
            Log.e("ludden.reeflifesurvey" ,"MapViewFragment addSurveySites mMap null");
            return;
        }

        //retrieve survey sites from data fragment
    /*    if(mDataRetrievalCallback != null){
            mSurveySiteList = mDataRetrievalCallback.retrieveSurveySiteList();
        }

        if ((mDataRetrievalCallback == null) || (mSurveySiteList == null) || (mSurveySiteList.size() <= 0))
            Log.e("jludden.reeflifesurvey" ,"MapViewFragment addSurveySites mDataRetrievalCallback error");
        else Log.d("jludden.reeflifesurvey"  ,"MapViewFragment addSurveySites called. "+mSurveySiteList.size()+" survey sites loaded");*/

        //Add Survey Sites to Map
        for(SurveySite site : mSurveySiteList.SITE_CODE_LIST) {
            LatLng pos = site.getPosition();
            //String realm = site.getRealm();
            String ecoRegion = site.getEcoRegion();
            //String name = site.getSiteName(); //just the name of one site...
            String summary = mSurveySiteList.codeSummary(site.getCode());

            float color = BitmapDescriptorFactory.HUE_RED; //todo make it a final field
            if(mSurveySiteList.getSelectedSiteCodes().contains(site.getCode())) { //already favorited so set the color!
                color = FAVORITED_SITE_COLOR;
            }

            //mMap.addMarker(new MarkerOptions().position(pos).title(name));
           // Marker newMarker = addMarker(pos, color, false); //drop the marker in with an animation. maybe we have too many for this approach

            Marker newMarker = mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(site.getCode())
                    .icon(BitmapDescriptorFactory.defaultMarker(color))
                    //.title(ecoRegion)
                    //.snippet(summary)
            );
            newMarker.setTitle(site.getCode());
            newMarker.setTag(site); //associate marker with an actual site object


           /* if(mSurveySiteList.getSelectedSiteCodes().contains(site.getCode())) { //already favorited so set the color!
                newMarker.setIcon(BitmapDescriptorFactory.defaultMarker(FAVORITED_SITE_COLOR));
            }*/
        }

        //addUnnecessaryMapStuff();
       LatLng sydney = new LatLng(-34, 151);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "onResume: ");

        //TODO - can move camera back to previous position, if necessary
        //   if(mPrevCameraPos != null) mMap.moveCamera(CameraUpdateFactory.newLatLng(mPrevCameraPos));

        if (mMapView != null)
            mMapView.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mMapView != null)
            mMapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mMapView != null)
            mMapView.onDestroy();
    }

    @Override
    public void onDestroyView() {
        Log.d("jludden.reeflifesurvey"  ,"map view fragment destroyed");

        Fragment f = getFragmentManager()
                .findFragmentByTag(MapViewFragment.TAG);
        if (f != null)
            getFragmentManager().beginTransaction().remove(f).commit();

        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMapView = null;
        mMapViewFragmentInteractionListener = null;
        mFAB = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mMapView != null)
            mMapView.onSaveInstanceState(outState);

        outState.putParcelable("POS", mMap.getCameraPosition().target);
        Log.d("jludden.reeflifesurvey"  , "MapViewFragment onsaveinstancestate pos: "+ mMap.getCameraPosition().target);
    }

    /**
     * Thanks to piruin
     * https://gist.github.com/piruin/94dc141e7736851b002c
     * @param position
     * @param color
     * @param draggable
     * @return
     */
    protected Marker addMarker(LatLng position, float color, boolean draggable) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.draggable(draggable);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(color));
        markerOptions.position(position);
        Marker pinnedMarker = mMap.addMarker(markerOptions);
        startDropMarkerAnimation(pinnedMarker);
        return pinnedMarker;
    }

    /**
     * Thanks to piruin
     * https://gist.github.com/piruin/94dc141e7736851b002c
     * @param marker
     */
    private void startDropMarkerAnimation(final Marker marker) {
        final LatLng target = marker.getPosition();
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mMap.getProjection();
        Point targetPoint = proj.toScreenLocation(target);
        final long duration = (long) (200 + (targetPoint.y * 0.6));
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        startPoint.y = 0;
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final Interpolator interpolator = new LinearOutSlowInInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);
                double lng = t * target.longitude + (1 - t) * startLatLng.longitude;
                double lat = t * target.latitude + (1 - t) * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));
                if (t < 1.0) {
                    // Post again 16ms later == 60 frames per second
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

}
