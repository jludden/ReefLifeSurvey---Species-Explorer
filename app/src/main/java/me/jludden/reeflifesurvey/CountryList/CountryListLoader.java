package me.jludden.reeflifesurvey.CountryList;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.os.OperationCanceledException;
import android.util.Log;

import me.jludden.reeflifesurvey.model.DummyContent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static me.jludden.reeflifesurvey.BuildConfig.DEBUG;

/**
 * Created by Jason on 5/1/2017.
 */

class CountryListLoader extends AsyncTaskLoader<List<DummyContent.DummyItem>> {

    private List<DummyContent.DummyItem> mData;
    private OkHttpClient client = new OkHttpClient();


    public CountryListLoader(Context context) {
        super(context);
        if (DEBUG) Log.d("jludden.reeflifesurvey"  ,"CountryListLoader Created");

    }

    /**
     * Called on a worker thread to perform the actual load and to return
     * the result of the load operation.
     * <p>
     * Implementations should not deliver the result directly, but should return them
     * from this method, which will eventually end up calling {@link #deliverResult} on
     * the UI thread.  If implementations need to process the results on the UI thread
     * they may override {@link #deliverResult} and do so there.
     * <p>
     * To support cancellation, this method should periodically check the value of
     * {@link #isLoadInBackgroundCanceled} and terminate when it returns true.
     * Subclasses may also override {@link #cancelLoadInBackground} to interrupt the load
     * directly instead of polling {@link #isLoadInBackgroundCanceled}.
     * <p>
     * When the load is canceled, this method may either return normally or throw
     * {@link OperationCanceledException}.  In either case, the {@link Loader} will
     * call {@link #onCanceled} to perform post-cancellation cleanup and to dispose of the
     * result object, if any.
     *
     * @return The result of the load operation.
     * @throws OperationCanceledException if the load is canceled during execution.
     * @see #isLoadInBackgroundCanceled
     * @see #cancelLoadInBackground
     * @see #onCanceled
     */
    @Override
    public List<DummyContent.DummyItem> loadInBackground() {
        Log.d("jludden.reeflifesurvey"  ,"CountryListLoader loadInBackground called");

        mData = new ArrayList<DummyContent.DummyItem>();
//        mData.add(new CountryDetailsTWO("1", "hello world", "details 1"));
//        mData.add(new CountryDetailsTWO("2", "hihi", "details 2"));
//        mData.add(new CountryDetailsTWO("3", "abc123", "details 3"));

        try{
            JSONArray countryArray = new JSONArray(getBasetripCountries());   //Get JSON response from BaseTrip API

            for(int i = 0; i < countryArray.length(); i++){
                JSONObject countryData = countryArray.getJSONObject(i);

                mData.add(new DummyContent.DummyItem(countryData.getString("alpha2Code"), countryData.getString("nameSanitized"), "details unused"));
            }

        } catch (IOException e) {
            Log.e("jludden.reeflifesurvey" , "IOException: " + e.toString());
        } catch (JSONException e) {
            Log.e("jludden.reeflifesurvey" , "JSONException: " + e.toString());
        }

        return mData;
    }

    /**
     * Returns a list of countries from the Basetrip API
     * @return
     * @throws IOException
     * @throws JSONException
     */
    String getBasetripCountries() throws IOException, JSONException {
        Request request = new Request.Builder()
                .url("https://thebasetrip.p.mashape.com/v2/countries")
                .addHeader("X-Mashape-Key", "oyP1rS3JiPmshQbeM9K8EgPdpp2Qp1xVWyIjsnJE7V9aYuYK1u")
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();

        Response response = client.newCall(request).execute();
        String result = response.body().string();
        Log.d("jludden.reeflifesurvey"  ,"getBasetripCountries Download response: "+result);
        return result;
    }

    //TODO
    JSONObject getBasetripDetails() throws IOException, JSONException {
        Request request = new Request.Builder()
                .url("https://thebasetrip.p.mashape.com/v2/countries/indonesia?from=united-states")
                .addHeader("X-Mashape-Key", "oyP1rS3JiPmshQbeM9K8EgPdpp2Qp1xVWyIjsnJE7V9aYuYK1u")
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();

        Response response = client.newCall(request).execute();
        String result = response.body().string();
        Log.d("jludden.reeflifesurvey"  ,"getBasetripDetails Download response: "+result);

        //TODO parse better
        JSONObject basicData = new JSONObject(result).getJSONObject("basic");
        JSONObject nameData = basicData.getJSONObject("name");
        nameData.getString("common");

        //TODO return details in more useable format
        return new JSONObject(result);
    }

    /**
     * Starts the downloader
     */
    @Override
    protected void onStartLoading() {
        Log.d("jludden.reeflifesurvey"  ,"CountryListLoader onStartLoading called. takecontentchanged: "+takeContentChanged());
        if(takeContentChanged() || mData == null){
            forceLoad();
        }
    }
}
