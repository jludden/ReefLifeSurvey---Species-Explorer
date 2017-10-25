package me.jludden.reeflifesurvey;

import android.content.Context;
import android.os.OperationCanceledException;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import me.jludden.reeflifesurvey.BrowseFish.InfoCardLoader;
import me.jludden.reeflifesurvey.model.SurveySiteList;

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


/*
    private JSONObject getCountryOutlineGeoJson(String country) throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://assets.thebasetrip.com/api/v2/countries/maps/argentina.geo.json")
                .build();
//        Request request = new Request.Builder()
//                .url("https://thebasetrip.p.mashape.com/v2/countries/"+country) //?from=united-states
//                .addHeader("X-Mashape-Key", "oyP1rS3JiPmshQbeM9K8EgPdpp2Qp1xVWyIjsnJE7V9aYuYK1u")
//                .addHeader("Content-Type", "application/json")
//                .addHeader("Accept", "application/json")
//                .build();

        Response response = client.newCall(request).execute();
        String result = response.body().string();
        Log.d("jludden.reeflifesurvey"  ,"SurveySiteListLoader Download response: "+result);


        //parse out the coordinates
        //    ew JSONObject(result)).getJSONObject("features").getJSONObject("geometry").getJSONArray("coordinates")
        JSONObject jsonCoordinates =  (new JSONObject(result));
        Log.d("jludden.reeflifesurvey"  ,"SurveySiteListLoader Download jsonCoordinates: "+jsonCoordinates.toString());

        return jsonCoordinates;
    }*/

}
