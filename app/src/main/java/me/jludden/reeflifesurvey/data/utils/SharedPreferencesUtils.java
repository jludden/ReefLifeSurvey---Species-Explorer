package me.jludden.reeflifesurvey.data.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

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



    public static void setUpFavoritesButton(final FishSpecies cardDetails, final CheckBox favoriteBtn, final Activity activity) {
        setUpFavoritesButton(cardDetails, favoriteBtn, activity, false);
    }

    //set up the favorites button initial state and onclick listener
    public static void setUpFavoritesButton(final FishSpecies cardDetails, final CheckBox favoriteBtn, final Activity activity, final Boolean useWhiteOutline){
        if(cardDetails.getFavorited(activity)) {
            favoriteBtn.setButtonDrawable(FAVORITES_SELECTED); //todo check the SharedPreferences cache
        } else {
            favoriteBtn.setButtonDrawable(useWhiteOutline? FAVORITES_OUTLINE_WHITE : FAVORITES_OUTLINE);
        }

        //set up favorites star button
        favoriteBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View v){
            onFavoritesButtonClick(cardDetails, favoriteBtn, activity, useWhiteOutline);
        }
        });
    }

    //set up the on click listener for a favorites button
    //todo unhappy about direct refernces to carddetails.favorited
    public static void onFavoritesButtonClick(final FishSpecies cardDetails, final CheckBox favoriteBtn, final Activity activity, final Boolean useWhiteOutline) {
        Log.d(TAG, "Favorites Button onClick. now favorited: " + !cardDetails.favorited);

        if (cardDetails.favorited) {
            favoriteBtn.setButtonDrawable(useWhiteOutline? FAVORITES_OUTLINE_WHITE : FAVORITES_OUTLINE);
            cardDetails.favorited = false;
        } else {
            cardDetails.favorited = true;
            favoriteBtn.setButtonDrawable(FAVORITES_SELECTED);
        }
        savePref(cardDetails.id, FishSpecies.PREF_FAVORITED, cardDetails.favorited, activity);
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
