package me.jludden.reeflifesurvey;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.RawRes;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.Target;
import me.jludden.reeflifesurvey.Data.SurveySiteList;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Jason on 9/6/2017.
 */

public class LoaderUtils {
    //region Loading Utility Functions

    /**
     * Parses a string containing one or more urls into a list of url strings
     * @param input input string
     * @return list of strings that match a web url
     */
    public static List<String> parseURLs(String input) {
        List<String> urlList = new ArrayList<>();
        Matcher m = android.util.Patterns.WEB_URL.matcher(input);//.matches();
        while(m.find()){
            urlList.add(m.group());
        }
        return urlList;
    }

    /**
     * Loads the Reef Life Survey survey site data layer, from disk or
     * @param context
     * @return json
     */
    public static JSONObject loadFishSurveySites(Context context){
        try {
            String surveys = LoaderUtils.loadStringFromDisk(R.raw.api_site_surveys, context); //TODO this can cause out of memory crash
            JSONObject json = new JSONObject(surveys);
            return json; //TODO

        } catch (IOException e) {
            Log.d("jludden.reeflifesurvey"  , "SurveySiteListLoader setupFishLocations ioexception: " + e.toString());
        } catch (JSONException e){
            Log.d("jludden.reeflifesurvey"  , "SurveySiteListLoader setupFishLocations JSONException: " + e.toString());

        }
        return null;
    }

    /**
     * Loads a resource file, such as a string in json-format, from disk
     *
     * @param id
     * @return
     * @throws IOException
     */
    public static String loadStringFromDisk(@RawRes int id, Context context) throws IOException {
        InputStream is =
                context.getResources().openRawResource(id);
        return loadStringFromDiskHelper(is);
    }

    public static String loadStringFromDiskHelper(InputStream is) throws IOException {
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            is.close();
        }

        String jsonString = writer.toString();
        return jsonString;
    }

    /**
     * utility function to parse survey site JSON to a List of Survey Sites
     * todo consider using Moshi
     * @param siteJSON json object
     * @return list of survey sites
     */
    public static SurveySiteList parseSurveySites(JSONObject siteJSON) {
        SurveySiteList siteList = new SurveySiteList();
        Log.d("jludden.reeflifesurvey"  , "InfoCardLoader.parseSurveySites  okokokok: "+siteJSON.length());

        String code;
        int count = 0;
        int MAX_NUM_EL = 4000; //max appears to be 3025 in the survey sites json file
        String prevCode = "";

        Iterator<String> keys = siteJSON.keys();

        while(keys.hasNext() && ++count < MAX_NUM_EL) {

            try {
                code = keys.next();
                // Log.d("jludden.reeflifesurvey"  , " parseSurveySites site code" + code);

                JSONArray siteArray = siteJSON.getJSONArray(code); // new JSONArray("NSW1");
                // Log.d("jludden.reeflifesurvey"  , " parrseSurveySites site " + code + site1.toString());

                SurveySiteList.SurveySite site = new SurveySiteList.SurveySite(code);
                site.setRealm(siteArray.getString(0));
                site.setEcoRegion(siteArray.getString(1));
                site.setSiteName(siteArray.getString(2));
                double longitude = siteArray.getDouble(3);
                double latitude = siteArray.getDouble(4);
                site.setPosition(new LatLng(latitude,longitude));
                site.setNumberOfSurveys(siteArray.getInt(5));
                site.setSpeciesFound(siteArray.getJSONObject(6));

                siteList.add(site);


            } catch (JSONException e){
                Log.d("jludden.reeflifesurvey"  , "InfoCardLoader.parseSurveySites error: "+e.toString());

            }

        }
        Log.d("jludden.reeflifesurvey"  , "InfoCardLoader.parseSurveySites  count: "+count);

        return siteList;
    }

    /**
     * One stop function for downloading one or more images using Glide
     * This is only for downloading them on a background thread.
     *
     * @param params
     * @return
     */
    public List<Drawable> downloadImages(Context context, String... params) {
        // fire everything into Glide queue
        FutureTarget<Drawable>[] requests = queueGlideRequests(context, params);

        // wait for each item
        List<Drawable> result = waitGlideResults(requests);
        return result;
    }

    /**
     * Method to start requesting images to load from Glide
     *
     * @param params image urls to load
     * @return array of future target drawables
     */
    public FutureTarget<Drawable>[] queueGlideRequests(Context context, String... params) {
        FutureTarget<Drawable>[] requests = new FutureTarget[params.length];
        for (int i = 0; i < params.length; i++) {
            if (isCancelled()) {
                break;
            }
            requests[i] = Glide.with(context)
                    .load(params[i])
                    .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
            ;
        }
        return requests;
    }

    //TODO enable cancelling
    private boolean isCancelled() {
        return false;
    }

    /**
     * Method to wait for images to load from Glide, after they have already been requested
     *
     * @param requests array of submitted future target requests
     * @return list of drawables, now fully loaded
     */
    public static List<Drawable> waitGlideResults(FutureTarget<Drawable>[] requests) {
        List<Drawable> result = new ArrayList<>();
        for (int i = 0; i < requests.length; i++) {
//            if (isCancelled()) todo
            try {
                result.add(requests[i].get(10, TimeUnit.SECONDS));
            } catch (Exception e) {
                //   result.failures.put(params[i], e);
            }
        }
        return result;
    }

    /**
     * Downloads a string from a URL with no parameters needed
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static String downloadStringFromURL(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    /**
     * Loads a bitmap image from a url
     * PROBABLY WANT TO USE GLIDE INSTEAD
     *
     * @param source url to load image from
     * @return
     * @throws MalformedURLException
     */
    public static Bitmap getBitmapFromURL(String source) throws IOException {
        Bitmap myBitmap;
        URL url = new URL(source);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.connect();
        InputStream input = connection.getInputStream();
        myBitmap = BitmapFactory.decodeStream(input);
        return myBitmap;
    }
    //endregion
}
