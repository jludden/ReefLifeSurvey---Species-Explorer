package me.jludden.reeflifesurvey.BrowseFish;

import android.content.Context;
import android.os.OperationCanceledException;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import me.jludden.reeflifesurvey.BrowseFish.model.InfoCard.CardDetails;
import me.jludden.reeflifesurvey.LoaderUtils;
import me.jludden.reeflifesurvey.R;
import me.jludden.reeflifesurvey.ReefLifeDataFragment;
import me.jludden.reeflifesurvey.model.SurveySiteList;
import me.jludden.reeflifesurvey.model.SurveySiteList.SurveySite;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static me.jludden.reeflifesurvey.BuildConfig.DEBUG;
import static me.jludden.reeflifesurvey.LoaderUtils.loadFishSurveySites;

/**
 * Created by Jason on 5/1/2017.
 */

public class InfoCardLoader extends AsyncTaskLoader<List<CardDetails>> {

    private List<CardDetails> mData; //final, sorted list of fish cards TODO why not just InfoCard, which is already a card list
    private CardViewFragment.CardType mCardType;
    private final static boolean DOWNLOAD_FISH_SPECIES_FROM_GITHUB = false;
    //private static JSONObject mFishData;    //json object from api_species.json
    private Map<String, CardDetails> mCardDict; //map of fish ID to fish card details. these values are then sorted to the final, mData, order
    private Iterator<CardDetails> mCardListIterator; //iterator of fish IDs in sorted order
    private String mNextSpeciesKey; //todo del
    private ReefLifeDataFragment.ReefLifeDataRetrievalCallback mDataRetrievalCallback;

    public InfoCardLoader(Context context, CardViewFragment.CardType cardType) {
        super(context);
        mCardType = cardType;
        if(DEBUG) Log.d("jludden.reeflifesurvey"  , "CardInfoLoader Created. Card Type to load: " + cardType);

        String errMsg = "";
        if(context instanceof ReefLifeDataFragment.ReefLifeDataRetrievalCallback){
            mDataRetrievalCallback = (ReefLifeDataFragment.ReefLifeDataRetrievalCallback) context;
        } else {
            errMsg += context.toString() + "must implement" +
                    ReefLifeDataFragment.ReefLifeDataRetrievalCallback.class.getName();
        }

        if(errMsg.length() > 0){
            throw new ClassCastException(errMsg);
        }
    }

    /**
     * Starts the downloader
     */
    @Override
    protected void onStartLoading() {
        Log.d("jludden.reeflifesurvey"  , "CardInfoLoader onStartLoading called. takecontentchanged: " + takeContentChanged());
        if (takeContentChanged() || mData == null) {
            forceLoad();
        }
    }

    /**
     * Loads fish info cards
     *     Supports being called multiple times:
     *          keeps track of what data has already been loaded,
     *          so when OnContentChanged() is called it will only load new data
     *          and add it to the existing list
     *
     * TODO:
     * To support cancellation, this method should periodically check the value of
     * {@link #isLoadInBackgroundCanceled} and terminate when it returns true.
     * Subclasses may also override {@link #cancelLoadInBackground} to interrupt the load
     * directly instead of polling {@link #isLoadInBackgroundCanceled}.
     * When the load is canceled, this method may either return normally or throw
     * {@link OperationCanceledException}.  In either case, the {@link Loader} will
     * call {@link #onCanceled} to perform post-cancellation cleanup and to dispose of the
     * result object, if any.
     *
     * @return The result of the load operation.
     * @throws IOException if an input/output exception is encounter.
     * @throws JSONException if there is an error parsing the JSON.
     * @throws OperationCanceledException if the load is canceled during execution.
     * @see #isLoadInBackgroundCanceled
     * @see #cancelLoadInBackground
     * @see #onCanceled
     */
    @Override
    public List<CardDetails> loadInBackground() {
        Log.d("jludden.reeflifesurvey"  , "CardInfoLoader loadInBackground called");

        if(mData == null) {
            mData = new ArrayList<>();
        }
        else {
        // TODO jank - loader manager won't call load finished if the data reference is the same?
        // TODO UPDATE 8/12 seems fine UPDATE 10/12 it will mostly work with the adapter, but loadfinished is definitely not called
            ArrayList<CardDetails> tempList = new ArrayList<>();
            tempList.addAll(mData);
            mData = tempList;
        }

        try {

          //  getBasetripCountries();

            loadFishCardsIncremental(); //Load more fish cards into mData

            /*
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                if (mCardType == CardViewFragment.CardType.Countries) {
                    USE_GLIDE_FOR_IMAGES = false; //still using old bitmap loading
                    mData.add(getBasetripDetails("indonesia")),mData.add(getBasetripDetails("argentina")),mData.add(getBasetripDetails("russia"));
                } else {
                    USE_GLIDE_FOR_IMAGES = true;
                    mData = getFishCards(mData); //Load fish cards
                }
            }*/
        } catch (IOException e) {
            Log.e("jludden.reeflifesurvey" , "IOException: " + e.toString());
        } catch (JSONException e) {
            Log.e("jludden.reeflifesurvey" , "JSONException: " + e.toString());
        }

        return mData;
    }


    //TODO!!!! #NOSHIP
    String getBasetripCountries() throws IOException, JSONException {
        //https://reeflifesurvey.com/species/####/
        Request request = new Request.Builder()
                .url("https://reeflifesurvey.com/species/4591/")
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();

        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();
        String result = response.body().string();
        Log.d("jludden.reeflifesurvey"  ,"getBasetripCountries Download response: "+result.substring(14000));
        Log.d("jludden.reeflifesurvey"  ,"getBasetripCountries Download response2: "+result.contains("54 cm"));
        Log.d("jludden.reeflifesurvey"  ,"getBasetripCountries Download response2: "+result.contains("Max Size"));

        int strStart = result.indexOf("Description");
        int strEnd = result.indexOf("Edit by");
        Log.d("jludden.reeflifesurvey"  ,"getBasetripCountries Download response2: "+strStart+"-"+strEnd+" substring: "+result.substring(strStart,strEnd));
        Log.d("jludden.reeflifesurvey"  ,"getBasetripCountries Download response2: "+strStart+"-"+strEnd+" substring: "+result.substring(strStart,strEnd));

//        int strStart = result.indexOf("Max Size");
//        Log.d("jludden.reeflifesurvey"  ,"getBasetripCountries Download response2: "+strStart+" substring: "+result.substring(strStart,strStart+300));

        return result;
    }

    //todo update to use loadsinglesite
    /**
     * inrecementally loads fish cards into mData
     * <p>
     * Using Yanir Seroussi's API, from:
     * https://yanirseroussi.com/2017/06/03/exploring-and-visualising-reef-life-survey-data/
     * <p>
     * https://yanirs.github.io/tools/rls/api-species.json
     * species.json – a mapping from species ID to an array of five elements: scientific name, common name, species page URL, survey method (0: method 1, 1: method 2, or 2: both), and images (array of URLs).
     * https://yanirs.github.io/tools/rls/api-site-surveys.json
     * . site-surveys.json – a mapping from site code to an array of seven elements: realm, ecoregion, site name, longitude, latitude, number of surveys, and species counts (mapping from each observed species ID to the number of surveys on which it was seen).
     */
    private void loadFishCardsIncremental() throws IOException, JSONException {

        // Call to get fish species data
//        if(mFishData == null) {
//            mFishData = mDataRetrievalCallback.retrieveFishSpecies();
//            /*String result = getFishSpeciesJSON();
//            //Log.d("jludden.reeflifesurvey"  , "getFishInCards Download response: " + result.substring(0, 50000)); //TODO cache the JSON strings for future loads
//            //"2":["Amphiprion akallopisos","Skunk Clownfish","http://reeflifesurvey.com/species/4605/",0,["http://dbzcuiesi59ut.cloudfront.net/0/species_ab_57c1476628af3.w1300.h866.jpg"]],
//            mFishData = new JSONObject(result);*/
//        }

        // Call to set up the site surveys TODO return type + should this be in constructor? Or can we cache?
        //TODO can we just return mCardDict with species added, but no cards added yet
        if(mCardDict ==  null) {
            JSONObject speciesJSON = setupFishLocations();
            mCardDict = mergeFishSpecies(speciesJSON); //TODO don't think we are still using the dictionary aspect - convert to list?

            //sort the species by most common TODO performance could be improved, certainly
            //CardDetails[] fishArray =  (CardDetails[]) mCardDict.values().toArray(CardDetails.type());
            CardDetails[] fishArray =  new CardDetails[mCardDict.size()];
            mCardDict.values().toArray(fishArray);
            List<CardDetails> fishList = new ArrayList<>(Arrays.asList(fishArray));
            Collections.sort(fishList);
            mCardListIterator = fishList.iterator();
            //or, if we don't want it sorted, just do: //Iterator<String> mCardListIterator = mCardDict.keySet().iterator(); //species iterator from dictionary
        }

//        int MAX_NUM_EL = mLoadAll ? 999 : 20; //only applies if loading incrementally
        int MAX_NUM_EL = CardViewFragment.CardViewSettings.LOAD_ALL ? 999 : 20; //only applies if loading incrementally

        int count = 0;
        String speciesKey;

        //add new fish
        while(mCardListIterator.hasNext() && (++count < MAX_NUM_EL)) {
            speciesKey = mCardListIterator.next().id;

            //Log.d("jludden.reeflifesurvey"  , "getFishInCards Download full string: " + speciesKey + " : " + basicData.toString());

            CardDetails cardDetails = mCardDict.get(speciesKey) ; // infocard shell already created - time to add details
            //Log.d("jludden.reeflifesurvey"  , "getFishInCards ID: "+cardDetails.getId()+" VS SPECIESKEY: " + speciesKey); //YES THEY ARE THE SAME

           parseSpeciesDetailsHelper(cardDetails, mDataRetrievalCallback);

            mData.add(cardDetails); //Add card to final card list
        }
    }

    //10/24 -
    //

    /**
     * takes the fishcard in, which really just has an id
     *   jumps to the api_species json and maps out the fields for the species onto the fishCard
     *   ASSUMES mFishData (the raw json from api_species)
     * @param fishCard input card, which should be empty besides the ID
     * @return the same fishCard, with details added (name, image url, etc.)
     * Created 10/24
     */
    private static CardDetails parseSpeciesDetailsHelper(CardDetails fishCard, ReefLifeDataFragment.ReefLifeDataRetrievalCallback dataRetrievalCallback) throws JSONException{
        JSONObject fishSpecies = dataRetrievalCallback.retrieveFishSpecies();
        JSONArray basicData = fishSpecies.getJSONArray(fishCard.getId());
        fishCard.cardName = basicData.getString(0);
        fishCard.commonNames = basicData.getString(1);

        //Parse out image URL
        //todo can do this better. dont set imageurl, just use the first one of the list, etc.
        String imageURL = basicData.getString(4);
        if (imageURL.length() <= 10)  Log.d("jludden.reeflifesurvey"  , "parseSpeciesDetailsHelper no image found1 for card (image url too short): " + fishCard.getId());
        else {
            imageURL = imageURL.substring(2, imageURL.length() - 2); //remove brackets and quotes
            imageURL = imageURL.replace("\\", ""); //remove weird backslashes
            List<String> urls = LoaderUtils.parseURLs(imageURL);
            if (urls.size() < 1)
                Log.d("jludden.reeflifesurvey", "parseSpeciesDetailsHelper no image found1 for card (image url too short): " + fishCard.getId());
            else {
                Log.d("jludden.reeflifesurvey", "parseSpeciesDetailsHelper url parsing for: " + fishCard.getId() + " full: " + imageURL + "\n #1: " + urls.get(0));
                fishCard.imageURLs = urls;
            }
        }

        return fishCard;
    }

    //merge fish species from json to the dictionary keys. then we will add the cards to the dictionary in getfishcards
    //todo shouldn't we be switching to using the SurveySiteList object, instead of raw json
    //todo delete this. we should separate processing of sites and species
    private HashMap<String, CardDetails> mergeFishSpecies(JSONObject siteJSON) {
        HashMap<String, CardDetails> speciesDictionary = new HashMap<>(); //dictionary mapping a fish id to its carddetails
        //PriorityQueue<Integer> fishSightingsIndex = new PriorityQueue<>();
        //multiset or bag?
        //HashMap<Integer, Integer> fishSightingsIndex = new HashMap<>(); //dictionary mapping the number of sightings for each fish to its fish ID
        Log.d("jludden.reeflifesurvey"  , "getFishInCards mergeFishSpecies: " + siteJSON.toString().substring(0,15));

        try {
            //return siteJSON.getJSONObject("NSW1");
            //todo these count loops arent working, they need incrementers
            int siteCount = 0;
            int MAX_NUM_SITE_EL = 200; //?
            Iterator<String> sitesList = siteJSON.keys();
            JSONObject site;
            while(sitesList.hasNext() && siteCount < MAX_NUM_SITE_EL) {

                site = siteJSON.getJSONObject(sitesList.next()); // loop through sites

                String species; //TODO
                int numSightings;
                int count = 0;
                int MAX_NUM_SPECIES_PER_SITE_EL = 200; //?
                Iterator<String> speciesList = site.keys();

                //add each species to the dictionary
                while (speciesList.hasNext() && count < MAX_NUM_SPECIES_PER_SITE_EL) {
                    species = speciesList.next();
                    numSightings = site.getInt(species);
                    CardDetails cardDetails;

                    if(speciesDictionary.containsKey(species)){ //check if already added from previously surveyed site
                        Log.d("jludden.reeflifesurvey"  , "getFishInCards mergeFishSpecies overwriting fish : " + species);
                        cardDetails = speciesDictionary.get(species);
                    }
                    else {
                        cardDetails = new CardDetails(species);
                        speciesDictionary.put(species, cardDetails);
                    }

                    //Add the number of times the fish was seen per site
                    cardDetails.numSightings += numSightings;
                }
            }

            return speciesDictionary;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    /*
     * Request siteRequest = new Request.Builder()
     .url("https://yanirs.github.io/tools/rls/api-site-surveys.json")
     .build();
     Response siteResponse = client.newCall(siteRequest).execute();
     String siteResult = siteResponse.body().string();
     Log.d("jludden.reeflifesurvey"  ,"getFishInCards siteResult: "+ siteResult.substring(0,50000));

      *** JSON FORMAT ***
            {code: [
                realm,
                ecoregion,
                name,
                longitude,
                latitude,
                number_of_surveys,
                {
                    fishSpecies: surveyedCount
                }], ... }

            such as:
            {"NSW1":[
                "Temperate Australasia",
                "Cape Howe",
                "Green Cape SE",
                150.05,
                -37.26,
                4,
                {
                    "1027":3,
                    "3005":3,
                    "3087":1,
                    "791":4
                }], ... }
     */

    //10/24 todo currently only called from mapview->bottomsheet
            //would like it to be called during the normal incremental load for each site, then have results aggregated
    public static List<CardDetails> loadSingleSite(SurveySite site, ReefLifeDataFragment.ReefLifeDataRetrievalCallback dataRetrievalCallback) throws JSONException {
        List<CardDetails> fishCards = new ArrayList<>(); //todo...
        int CARDS_TO_LOAD = 5;
        int cards_loaded = 0;
        String species;

        JSONObject fullSpeciesJSON = site.getSpeciesFound();
        Iterator<String> speciesList = fullSpeciesJSON.keys();

        //add each species to the dictionary
        while (speciesList.hasNext()
                && (cards_loaded++ < CARDS_TO_LOAD)) {
            species = speciesList.next();

            CardDetails fishCard = new CardDetails(species);
            int numSightings = fullSpeciesJSON.getInt(species); //number of sightings of this fish in this survey site (may not be accurate due to datasource)

            //Log.d("jludden.reeflifesurvey"  , "load single site. already );
            if(fishCards.contains(fishCard)) { //todo verify using overriden equals() func
                int index = fishCards.indexOf(fishCard);
                fishCards.get(index).setFoundInSites(site, numSightings);
            }
            else{
                fishCard = parseSpeciesDetailsHelper(fishCard, dataRetrievalCallback);
                fishCard.setFoundInSites(site, numSightings);
                fishCards.add(fishCard);
            }
        }
        return fishCards;
    }

    public JSONObject setupFishLocations(){
        try {
            JSONObject result = new JSONObject();

            /*
            //Log.d("jludden.reeflifesurvey"  , "setupFishLocations no flag found for card: " + i);
            String surveys = loadStringFromDisk(R.raw.api_site_surveys); //TODO this can cause out of memory crash
            Log.d("jludden.reeflifesurvey"  , "getFishInCards setupFishLocations: " + surveys.substring(0, 894));
            //surveys = surveys.substring(0, 894) + "}]}"; //todo trying to do a portion first
            //Log.d("jludden.reeflifesurvey"  , "getFishInCards setupFishLocations2: " + surveys);

            //parse the json:
            //            String code = basicData.getString(0);
            JSONObject surveySites = new JSONObject(surveys);
            */
            JSONObject surveySites = loadFishSurveySites(getContext());

            // String code = "NSW1"; //TODO (looping through them already in mapviewloader)
            //JSONArray site1 = json.getJSONArray(code); // new JSONArray("NSW1");
            //Log.d("jludden.reeflifesurvey"  , "getFishInCards setupFishLocations site1: " + site1.toString());


           // String code = "NSW1"; //TODO
            //String[] codes = {"NSW1", "AH1", "AH2", "BALI1", "RAJA19", "FLORES4"} ; //TODO get these site names from the maps activity

            if(SurveySiteList.SELECTED_SURVEY_SITES == null || SurveySiteList.SELECTED_SURVEY_SITES.size() <=0){
//                throw new JSONException("InfoCardLoader.setupFishLocations() - no locations selected");
                Log.e("jludden.reeflifesurvey" ,"ERROR InfoCardLoader.setupFishLocations() - no locations selected");

                SurveySiteList.SELECTED_SURVEY_SITES.add(new SurveySite("FLORES4"));
            }

            String selectedSites = "";
            for( SurveySite selSite: SurveySiteList.SELECTED_SURVEY_SITES){
                selectedSites = selectedSites + selSite.getCode() + selSite.getID() + ", ";

                String code = selSite.getCode()+selSite.getID();
//            }
//
//            String[] codes = {"FLORES1", "FLORES2", "FLORES2", "FLORES4", "FLORES5"} ; //TODO get these site names from the maps activity
//
//
//            int count = 0;
//            int MAX_NUM_EL = 4000; //max appears to be 3025 in the survey sites json file
//            String prevCode = "";
//
//            //TODO loop through JSON
//            //Iterator<String> keys = surveySites.keys();
//           // while(keys.hasNext() && count < MAX_NUM_EL) {
//            for( String code : codes){
               // if(count > 1) break; //todo
              //  code = keys.next();

                JSONArray site1 = surveySites.getJSONArray(code); // new JSONArray("NSW1");


            /*
            {code: [
                realm,
                ecoregion,
                name,
                longitude,
                latitude,
                ?,
                {
                    numSurveys: speciesCounts
                }], ... }
             */
                String realm = site1.getString(0);
                String ecoRegion = site1.getString(1);
                String name = site1.getString(2);
                double longitude = site1.getDouble(3);
                double latitude = site1.getDouble(4);
                String idkwhat = site1.getString(5);
                JSONObject speciesFound = site1.getJSONObject(6);
                //Log.d("jludden.reeflifesurvey"  , "getFishInCards setupFishLocations site1 stuff: " + realm + ecoRegion + longitude + latitude + speciesFound.toString());

                result.accumulate(code, speciesFound);
                result.put(code, speciesFound);
            }

                        Log.d("jludden.reeflifesurvey"  , "InfoCardLoader setupFishLocations SELECTED SITES: " + selectedSites);

            return result;

            //parse the fish species
            /*
            String fishSpecies;
            Iterator<String> keys = speciesFound.keys();
            while(keys.hasNext()){
                fishSpecies = keys.next();
                Log.d("jludden.reeflifesurvey"  , "getFishInCards setupFishLocations key: "+fishSpecies);
                int fishCount = (int) speciesFound.get(fishSpecies);
                Log.d("jludden.reeflifesurvey"  , "getFishInCards setupFishLocations val: "+fishCount);
            }
           */

//        } catch (IOException e) {
//            Log.d("jludden.reeflifesurvey"  , "setupFishLocations ioexception: " + e.toString());
        } catch (JSONException e){
            Log.d("jludden.reeflifesurvey"  , "setupFishLocations JSONException: " + e.toString());

        }
        return null;
    }

    /**
     * Get fish species JSON
     *
     * @return
     * @throws IOException
     */
    public String getFishSpeciesJSON() throws IOException {
        //option A, download image from Yanir's github
        if (DOWNLOAD_FISH_SPECIES_FROM_GITHUB) {
            return LoaderUtils.downloadStringFromURL("https://yanirs.github.io/tools/rls/api-species.json");
        }

        //option B, load from disk
        else {
            return LoaderUtils.loadStringFromDisk(R.raw.api_species, getContext());
        }
    }
}
