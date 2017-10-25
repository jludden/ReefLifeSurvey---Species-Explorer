package me.jludden.reeflifesurvey;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.jludden.reeflifesurvey.R;

import me.jludden.reeflifesurvey.model.SurveySiteList;
import me.jludden.reeflifesurvey.model.SurveySiteList.SurveySite;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.data.geojson.GeoJsonLayer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Jason on 5/15/2017.
 *
 * 9/1 going from
 * com.getbase.floatingactionbutton to toan.android.floatingactionmenu
 *
 * TODO
 *  10/23: refactor. map+surveysitelist model into new folder
 *      3 different colors for map icons: new, added, selected. DONT HARDCODE?
 *
 */

public class MapViewFragment extends Fragment
        implements OnMapReadyCallback, GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {

    private static MapViewFragment mapFragment;
    static View rootView;
    private GoogleMap mMap;
    private MapView mMapView;
   // private MapCallback mMapCallback;
    private Marker mSelectedMarker;
    private ArrayList<Marker> mSelectedSiteList = new ArrayList<>(); //todo use static selected site list //todo save these preferences
    private FloatingActionButton mFAB;
    private FloatingActionButton[] mFABmenu;
    private BottomSheetBehavior mBottomSheetBehavior;

    public static final String TAG = "SurveySiteMap";

    //data retrieval from the retained, headless fragment
    private SurveySiteList mSurveySiteList;
    private ReefLifeDataFragment.ReefLifeDataRetrievalCallback mDataRetrievalCallback;

    private MapViewFragmentInteractionListener mListener;
    public interface MapViewFragmentInteractionListener{
        void showBottomSheet(SurveySiteList.SurveySite siteInfo);
        FloatingActionButton getFloatingActionButton(); //// TODO: 9/14/2017
        //i would rather, instead of getting the whole button, pass back to the activity the two different actions:
        //1. site selected , show summary, update fab icon (starred or unstarred)
        //2. site details, show details, bottom sheet
    }

    public MapViewFragment() {
    }

    public static MapViewFragment newInstance() {
        if(mapFragment == null) {
            mapFragment = new MapViewFragment();
            mapFragment.setRetainInstance(true);
        }
        //        Bundle args = new Bundle();
        //        args.putInt(ARG_PARAM1, typeToLoad.ordinal());
        //        args.putString(ARG_PARAM2, param2);
        //        fragment.setArguments(args);
        return mapFragment;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        String errMsg = "";

        if(context instanceof ReefLifeDataFragment.ReefLifeDataRetrievalCallback){
            mDataRetrievalCallback = (ReefLifeDataFragment.ReefLifeDataRetrievalCallback) context;
        } else {
            errMsg += context.toString() + "must implement" +
                    ReefLifeDataFragment.ReefLifeDataRetrievalCallback.class.getName();
        }
        if(context instanceof MapViewFragmentInteractionListener){
            mListener = (MapViewFragmentInteractionListener) context;
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
        Log.d("jludden.reeflifesurvey"  ,"map view fragment created");

        MainActivity main = (MainActivity) getActivity();
        //mFAB = main.getFAB(); //TODO
        mFAB = mListener.getFloatingActionButton();
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

        Log.d("jludden.reeflifesurvey"  , "MapViewFragment View Created. Has rootview: "+(rootView==null));

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


        View bottomSheet = getActivity().findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(mMap == null) {
            mMapView = (MapView) rootView.findViewById(R.id.fragment_map);
            mMapView.getMapAsync(this);
            mMapView.onCreate(getArguments());
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
        mMap.setOnInfoWindowClickListener(this);
//        mSelectedSiteList = new ArrayList<>();


        //todo set map settings
        //mMap.getUiSettings().setZoomControlsEnabled(true);

        //by default, when a marker on the map is clicked, a toolbar will popup
        //this toolbar links to the actual google maps app and has a directions button
        mMap.getUiSettings().setMapToolbarEnabled(false);

        addSurveySites();
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

        //if()
        mSelectedMarker = null;
        mFAB.setImageResource(R.drawable.ic_add_white);
        //todo delete ic_add_location_black_24dp, ic_add_loc_2,

        if(mBottomSheetBehavior.getState()==BottomSheetBehavior.STATE_EXPANDED)  mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        //hideFABmenu();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.v("ludden.reeflifesurvey" , "MapViewFragment onMarkerClick marker: "+ marker.toString());
        mSelectedMarker = marker;
        //hideFABmenu();
        mFAB.setImageResource(R.drawable.ic_fab_add_loc); //todo animate and change? //todo would a favorites star be a better icon?

        //already showing info for a different marker - update bottom sheet contents
        if(mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
            mListener.showBottomSheet((SurveySite) marker.getTag());
            //((MainActivity) getActivity()).showBottomSheet((SurveySite) marker.getTag());
        }

        return false;
    }

    /**
     * Launch the activities bottom sheet for additional site info
     * @param marker
     */
    @Override
    public void onInfoWindowClick(Marker marker) {
        Log.d("jludden.reeflifesurvey"  , "MapViewFragment onInfoWindowClick marker: "+ marker.toString());
        //Snackbar.make(mMapView, "clicked", Snackbar.LENGTH_SHORT).show();
        marker.hideInfoWindow();
        mListener.showBottomSheet((SurveySite) marker.getTag());
        //((MainActivity) getActivity()).showBottomSheet((SurveySite) mSelectedMarker.getTag());
    }

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
                mSelectedSiteList.add(mSelectedMarker); //todo save this to preferences
                SurveySiteList.SELECTED_SURVEY_SITES.add((SurveySite) mSelectedMarker.getTag()); //todo why both
                mSelectedMarker.setIcon((BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
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
        lastMark.setIcon((BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        SurveySiteList.SELECTED_SURVEY_SITES.remove(lastMark.getTag());

        //todo remove from preferences list
    }

    //Add the loaded survey sites to the map
    private void addSurveySites() {
        if (mMap == null) {
            Log.e("ludden.reeflifesurvey" ,"MapViewFragment addSurveySites mMap null");
            return;
        }

        //retrieve survey sites from data fragment
        if(mDataRetrievalCallback != null){
            mSurveySiteList = mDataRetrievalCallback.retrieveSurveySiteList();
        }

        if ((mDataRetrievalCallback == null) || (mSurveySiteList == null) || (mSurveySiteList.size() <= 0))
            Log.e("jludden.reeflifesurvey" ,"MapViewFragment addSurveySites mDataRetrievalCallback error");
        else Log.d("jludden.reeflifesurvey"  ,"MapViewFragment addSurveySites called. "+mSurveySiteList.size()+" survey sites loaded");

        //Add Survey Sites to Map
        for(SurveySite site : mSurveySiteList.SITE_CODE_LIST) {
            LatLng pos = site.getPosition();
            //String realm = site.getRealm();
            String ecoRegion = site.getEcoRegion();
            //String name = site.getSiteName(); //just the name of one site...
            String summary = mSurveySiteList.codeSummary(site.getCode());

            //mMap.addMarker(new MarkerOptions().position(pos).title(name));
            Marker newMarker = mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(ecoRegion)
                    .snippet(summary)
            );
            newMarker.setTag(site); //associate marker with an actual site object
        }

        //addUnnecessaryMapStuff();
       LatLng sydney = new LatLng(-34, 151);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onResume() {
        super.onResume();

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

        SupportMapFragment f = (SupportMapFragment) getFragmentManager()
                .findFragmentById(R.id.fragment_map);
        if (f != null)
            getFragmentManager().beginTransaction().remove(f).commit();

        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mMapView != null)
            mMapView.onSaveInstanceState(outState);

        outState.putParcelable("POS", mMap.getCameraPosition().target);
        Log.d("jludden.reeflifesurvey"  , "MapViewFragment onsaveinstancestate pos: "+ mMap.getCameraPosition().target);
    }

    //todo delete
    private void addUnnecessaryMapStuff(){
        // mViewAdapter.updateItems(data);
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        //polygon stuff
        /*
                // mPolygonOptions = new PolygonOptions();
//            mPolygonOptions.add(latLng);
//            mPolygonOptions.strokeColor(Color.BLACK);
//            mPolygonOptions.fillColor(Color.TRANSPARENT);
//            mPolygon = mMap.addPolygon(mPolygonOptions);
         */

        //geoJSON drawing
        //JSONObject geoJsonData = new JSONObject();
        //GeoJsonLayer layer = new GeoJsonLayer(mMap, geoJsonData);
        //GeoJsonLayer layer = new GeoJsonLayer(mMap, data.get(0));
        //layer.addLayerToMap();

        try {
            GeoJsonLayer layer ;

            String jsonString = "{\n" +
                    "  \"type\": \"FeatureCollection\",\n" +
                    "  \"features\": [\n" +
                    "    {\n" +
                    "      \"type\": \"Feature\",\n" +
                    "      \"properties\": {},\n" +
                    "      \"geometry\": {\n" +
                    "        \"type\": \"Polygon\",\n" +
                    "        \"coordinates\": [\n" +
                    "          [\n" +
                    "            [\n" +
                    "              109.1162109375,\n" +
                    "              -2.8991526985043006\n" +
                    "            ],\n" +
                    "            [\n" +
                    "              125.2880859375,\n" +
                    "              -2.8991526985043006\n" +
                    "            ],\n" +
                    "            [\n" +
                    "              125.2880859375,\n" +
                    "              8.363692651835823\n" +
                    "            ],\n" +
                    "            [\n" +
                    "              109.1162109375,\n" +
                    "              8.363692651835823\n" +
                    "            ],\n" +
                    "            [\n" +
                    "              109.1162109375,\n" +
                    "              -2.8991526985043006\n" +
                    "            ]\n" +
                    "          ]\n" +
                    "        ]\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            JSONObject jsonObj = new JSONObject(jsonString);
            //Log.d("jludden.reeflifesurvey"  ,"MapViewFragment jsonObj: "+jsonObj.toString());

            //jsonObj = data.get(0);

            layer = new GeoJsonLayer(mMap, jsonObj); //.getJSONObject("features")
            layer.addLayerToMap();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
