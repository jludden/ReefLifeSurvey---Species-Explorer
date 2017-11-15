package me.jludden.reeflifesurvey;

import android.content.Context;
import android.os.OperationCanceledException;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import me.jludden.reeflifesurvey.Data.SurveySiteList;

import org.json.JSONObject;

import static me.jludden.reeflifesurvey.BuildConfig.DEBUG;

/**
 * Created by Jason on 5/29/2017.
 */

class SurveySiteListLoader extends AsyncTaskLoader<SurveySiteList> {

    public SurveySiteListLoader(Context context) {
        super(context);
        if (DEBUG) Log.d("jludden.reeflifesurvey"  ,"SurveySiteListLoader Created");

    }

    /**
     * Called on a worker thread to perform the actual load and to return
     * the result of the load operation.
     * <p>
     *
     * @return The result of the load operation.
     * @throws OperationCanceledException if the load is canceled during execution.
     * @see #isLoadInBackgroundCanceled
     * @see #cancelLoadInBackground
     * @see #onCanceled
     */
    @Override
    public SurveySiteList loadInBackground() {
        Log.d("jludden.reeflifesurvey"  ,"SurveySiteListLoader loadInBackground()");

        //Add some coordinates for reef survey sites
        JSONObject surveySites = LoaderUtils.loadFishSurveySites(getContext());
        SurveySiteList siteList = LoaderUtils.parseSurveySites(surveySites);
        siteList.loadFavoritedSites(getContext()); //Load Saved Sites

        return siteList;
    }

    /**
     * Starts the downloader
     */
    @Override
    protected void onStartLoading() {
        Log.d("jludden.reeflifesurvey"  ,"SurveySiteListLoader onStartLoading called. takecontentchanged: "+takeContentChanged());
        forceLoad();
//        if(takeContentChanged()){
//            forceLoad();
//        }
    }


}
