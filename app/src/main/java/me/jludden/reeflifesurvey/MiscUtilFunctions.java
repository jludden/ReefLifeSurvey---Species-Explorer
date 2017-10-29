package me.jludden.reeflifesurvey;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

import me.jludden.reeflifesurvey.BrowseFish.model.InfoCard;

/**
 * Created by Jason on 10/29/2017.
 */

public class MiscUtilFunctions {


    //set up the favorites button initial state and onclick listener
    public static void setUpFavoritesButton(final InfoCard.CardDetails cardDetails, final CheckBox mFavoriteBtn, final Activity activity){
        if(cardDetails.getFavorited(activity)) {
            mFavoriteBtn.setButtonDrawable(R.drawable.ic_star_selected); //todo check the SharedPreferences cache
        }

        //set up favorites star button
        mFavoriteBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View v){
            onFavoritesButtonClick(cardDetails, mFavoriteBtn, activity);
        }
        });
    }

    //set up the on click listener for a favorites button
    //todo unhappy about direct refernces to carddetails.favorited
    public static void onFavoritesButtonClick(final InfoCard.CardDetails cardDetails, final CheckBox mFavoriteBtn, final Activity activity) {
        Log.d("jludden.reeflifesurvey", "Favorites Button onClick. now favorited: " + !cardDetails.favorited);

        if (cardDetails.favorited) {
            mFavoriteBtn.setButtonDrawable(R.drawable.ic_star_border);
            cardDetails.favorited = false;
        } else {
            cardDetails.favorited = true;
            mFavoriteBtn.setButtonDrawable(R.drawable.ic_star_selected);
        }
        savePref(cardDetails.id, InfoCard.PREF_FAVORITED, cardDetails.favorited, activity);
    }


    //Saves a fish as a favorite
    public static void savePref(String id, String valKey, boolean val, final Activity activity) {
        Log.d("jludden.reeflifesurvey"  , "CardViewAdapter Saving Preference for Card: " + id + " key: " + valKey + " val: "+val );

        SharedPreferences prefs = activity.getSharedPreferences(
                "me.jludden.reeflifesurvey", Context.MODE_PRIVATE);

        String key = InfoCard.generateSharedPrefKey(id,valKey);//"me.jludden.reeflifesurvey.CardPref_" + id + "_" + valKey;
        prefs.edit().putBoolean(key, val).apply();
    }

    //get the saved preferences for the favorites button for this fish ID
    public static boolean getPref(String id, String valKey, Activity activity){
        SharedPreferences prefs = activity.getSharedPreferences(
                "me.jludden.reeflifesurvey", Context.MODE_PRIVATE);

        String key = InfoCard.generateSharedPrefKey(id,valKey); //"me.jludden.reeflifesurvey.CardPref_" + id + "_" + valKey;
        boolean temp=prefs.getBoolean(key, false);
        if(temp) Log.d("jludden.reeflifesurvey"  , "OnLoadFinished getPref-TRUE id: " + id + " key: " + valKey + " result: "+temp );
        return temp;//prefs.getBoolean(key, false);

        // use a default value using new Date()
        //long l = prefs.getLong(dateTimeKey, new Date().getTime());
    }

}
