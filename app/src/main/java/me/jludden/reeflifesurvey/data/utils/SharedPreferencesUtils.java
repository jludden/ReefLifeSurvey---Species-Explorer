package me.jludden.reeflifesurvey.data.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.jludden.reeflifesurvey.R;
import me.jludden.reeflifesurvey.data.model.FishSpecies;

/**
 * Created by Jason on 10/29/2017.
 *
 * Mostly concerned with reading and writing to SharedPrefs
 */

public class SharedPreferencesUtils {

    private static final String FAV_SITES_KEY = "me.jludden.reeflifesurvey.SiteFavs";
    private static final String STORED_OFFLINE_SITES_KEY = "me.jludden.reeflifesurvey.SiteOffline";
    private static final String STORED_OFFLINE_SITES_EXT_KEY = "me.jludden.reeflifesurvey.SiteOfflineExternal"; //SD card
    private static final String STORED_OFFLINE_PATH_KEY = "me.jludden.reeflifesurvey.OfflineImagePath";
    private static final String STORED_OFFLINE_PATH_EXT_KEY = "me.jludden.reeflifesurvey.OfflineImagePathExternal";

    private static final String TAG = "SharedPreferenceUtils";

    public static final int FAVORITES_OUTLINE = R.drawable.ic_favorite_heart_outline_trim;
    public static final int FAVORITES_OUTLINE_WHITE = R.drawable.ic_favorite_heart_outline_trim_white;
    public static final int FAVORITES_SELECTED = R.drawable.ic_favorite_heart_filled_trim;


    //defines the interface for an item that has a favorites button and saves the favorited status to SharedPreferences
    public interface Favoritable {
        /**
         * @return the cached value of the favorites field
         */
        public boolean getFavorited();

        /**
         * Updates and returns the favorited value from SharedPreferences
         * @param activity if null return the local cached value (default false)
         * @return the updated value of the favorites field
         */
        public boolean getFavorited(@Nullable Activity activity);

        /**
         * Sets the updated favorites value
         */
        public void setFavorited(boolean favorited, Activity activity);
    }

    public static void setUpFavoritesButton(final Favoritable item, final CheckBox favoriteBtn, final Activity activity) {
        setUpFavoritesButton(item, favoriteBtn, activity, -1, -1);
    }


    /**
     * set up the favorites button initial state and onclick listener
     * @param item FishSpecies or SurveySite that can be favorited
     * @param favoriteBtn
     * @param activity
     * @param altSelected -1 for default favorited drawable icon, or resID
     * @param altUnselected -1 for default unfavorited drawable icon, or resID
     */
    public static void setUpFavoritesButton(final Favoritable item, final CheckBox favoriteBtn, final Activity activity, final int altSelected, final int altUnselected){
        if(item.getFavorited(activity)) {
            favoriteBtn.setButtonDrawable(altSelected == -1 ? FAVORITES_SELECTED : altSelected); //todo check the SharedPreferences cache
        } else {
            favoriteBtn.setButtonDrawable(altUnselected == -1 ? FAVORITES_OUTLINE : altUnselected);
        }

        //set up favorites star button
        favoriteBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View v){
            onFavoritesButtonClick(item, favoriteBtn, activity, altSelected, altUnselected);
        }
        });
    }

    //set up the on click listener for a favorites button
    private static void onFavoritesButtonClick(final Favoritable item, final CheckBox favoriteBtn, final Activity activity, final int altSelected, final int altUnselected) {
        Log.d(TAG, "Favorites Button onClick. now favorited: " + !item.getFavorited(null));

        if (item.getFavorited()) {
            item.setFavorited(false, activity);
            favoriteBtn.setButtonDrawable(altUnselected == -1 ? FAVORITES_OUTLINE : altUnselected);
        } else {
            item.setFavorited(true, activity);
            favoriteBtn.setButtonDrawable(altSelected == -1 ? FAVORITES_SELECTED : altSelected);
        }
    }

    //separate implementation for my wonky map FAB todo can I combine them?
    public static void setSiteFavoritedDrawable(final Favoritable item, final ImageButton favoriteBtn) {
        if(item.getFavorited()){
            favoriteBtn.setImageResource(R.drawable.ic_favorites_heart_filled_two);
        } else {
            favoriteBtn.setImageResource(R.drawable.ic_favorite_heart_outline_trim_white);
        }
    }


    //Saves a fish as a favorite
    public static void savePref(String id, String valKey, boolean val, final Activity activity) {
        Log.d("jludden.reeflifesurvey"  , "CardViewAdapter Saving Preference for Card: " + id + " key: " + valKey + " val: "+val );

        SharedPreferences prefs = activity.getSharedPreferences(
                "me.jludden.reeflifesurvey", Context.MODE_PRIVATE);

        String key = FishSpecies.generateSharedPrefKey(id,valKey);//"me.jludden.reeflifesurvey.CardPref_" + id + "_" + valKey;
        prefs.edit().putBoolean(key, val).apply();
    }

    //get the saved preferences for the favorites button for this fish ID
    public static boolean getPref(String id, String valKey, Activity activity){
        SharedPreferences prefs = activity.getSharedPreferences(
                "me.jludden.reeflifesurvey", Context.MODE_PRIVATE);

        String key = FishSpecies.generateSharedPrefKey(id,valKey); //"me.jludden.reeflifesurvey.CardPref_" + id + "_" + valKey;
        boolean temp=prefs.getBoolean(key, false);
        if(temp) Log.d("jludden.reeflifesurvey"  , "OnLoadFinished getPref-TRUE id: " + id + " key: " + valKey + " result: "+temp );
        return temp;//prefs.getBoolean(key, false);

        // use a default value using new Date()
        //long l = prefs.getLong(dateTimeKey, new Date().getTime());
    }
    
    public static void clearFavSpecies(Context context) {
        Log.d(TAG, "clearFavSpecies: clicked TODO not implemented") ;
        //TODO
    }

    /**
     * Loads the fav survey site combined codes from SharedPreferences
     * @param context
     * @return
     */
    public static Set<String> getFavSites(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                "me.jludden.reeflifesurvey", Context.MODE_PRIVATE);

        Set<String> siteCodes = new HashSet<>();
        siteCodes = prefs.getStringSet(FAV_SITES_KEY,siteCodes); //unordered set of SurveySite combinedCodes

        return siteCodes;
    }

    /**
     * Save the list of favorited sites to SharedPreferences
     * @param siteCodeList
     * @param context
     */
    public static void updateFavSites(List<String> siteCodeList, Context context) {
        if(context == null) {
            Log.e(TAG, "update favorite sites Shared Preferences - null context");
            return;
        }
        Log.d(TAG  , "updateFavSites. Saving " + siteCodeList.size() + " sites");
        SharedPreferences prefs = context.getSharedPreferences(
                "me.jludden.reeflifesurvey", Context.MODE_PRIVATE);

        Set<String> set = new HashSet<>(siteCodeList);
        prefs.edit().putStringSet(FAV_SITES_KEY, set).apply();
    }
    
    public static void clearFavSites(Context context) {
        updateFavSites(new ArrayList<String>(), context);
    }


    // save the set of sites that are stored offline
    //  also save a single storage path
    //  all fish images are saved in a single path - fish will often appear in multiple sites, so it is needlessly complex to sort them by site
    //  similarly, we will only support deleting ALL saved sites (todo possible enh)
    //
    public static void setSitesStoredOffline(List<String> siteCodeList, String path, Boolean isExternal, Context context) {

        String sitesKey = isExternal ? STORED_OFFLINE_SITES_EXT_KEY : STORED_OFFLINE_SITES_KEY;
        String pathKey = isExternal ? STORED_OFFLINE_PATH_EXT_KEY : STORED_OFFLINE_PATH_KEY;


        SharedPreferences prefs = context.getSharedPreferences(
                "me.jludden.reeflifesurvey", Context.MODE_PRIVATE);


        Set<String> set = new HashSet<>(siteCodeList);
        prefs.edit().putStringSet(sitesKey, set).apply();
        prefs.edit().putString(pathKey, path).apply(); //save the path

        Log.d(TAG, "setSitesStoredOffline: "+path+" sites: "+siteCodeList.size());
    }

    //return the lit of sites that are stored offline
    public static Set<String> loadAllSitesStoredOffline(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                "me.jludden.reeflifesurvey", Context.MODE_PRIVATE);

        Set<String> localCodes = new HashSet<>();
        Set<String> extCodes = new HashSet<>();
        localCodes = prefs.getStringSet(STORED_OFFLINE_SITES_KEY, localCodes); //unordered set of SurveySite combinedCodes
        extCodes = prefs.getStringSet(STORED_OFFLINE_SITES_EXT_KEY, extCodes); //unordered set of SurveySite combinedCodes
        localCodes.addAll(extCodes);
        return localCodes;
    }

    //return the list of sites that are stored offline
    public static Set<String> loadSitesStoredOffline(Context context, Boolean isExternal) {
        String sitesKey = isExternal ? STORED_OFFLINE_SITES_EXT_KEY : STORED_OFFLINE_SITES_KEY;
        SharedPreferences prefs = context.getSharedPreferences(
                "me.jludden.reeflifesurvey", Context.MODE_PRIVATE);

        Set<String> codes = new HashSet<>();
        codes = prefs.getStringSet(sitesKey, codes); //unordered set of SurveySite combinedCodes
        return codes;
    }

    public static String loadStoredOfflinePath(Boolean isExternal, Context context){
        SharedPreferences prefs = context.getSharedPreferences(
                "me.jludden.reeflifesurvey", Context.MODE_PRIVATE);
        String pathKey = isExternal ? STORED_OFFLINE_PATH_EXT_KEY : STORED_OFFLINE_PATH_KEY;
        return prefs.getString(pathKey, "");
    }
}
