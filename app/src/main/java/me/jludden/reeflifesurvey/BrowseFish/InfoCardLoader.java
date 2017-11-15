package me.jludden.reeflifesurvey.BrowseFish;

import android.content.Context;
import android.os.OperationCanceledException;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import me.jludden.reeflifesurvey.model.InfoCard.CardDetails;
import me.jludden.reeflifesurvey.LoaderUtils;
import me.jludden.reeflifesurvey.R;
import me.jludden.reeflifesurvey.ReefLifeDataFragment;
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
 *
 * Loads and parses data from the ReefLifeSurvey API into a List of InfoCard.CardDetails
 *
     Request siteRequest = new Request.Builder()
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
 *
 *
 *
 * API format to fish species:
 {
    0: [
        "Scientific Name",
        "Common name, common name, common name",
        "https://reeflifesurvey.com/species/###", --species key
        #,                                        --
        [
            "https image1",
            "https image2"
        ]
    ],
    1: [
        ...
    ]
 }
 *
 */

public class InfoCardLoader extends AsyncTaskLoader<List<CardDetails>> {

    private List<CardDetails> mData; //final, sorted list of fish cards TODO why not just InfoCard, which is already a card list
    private String mPassedInSurveySiteCode = "";//optional passed in parameter. otherwise load favorite sites

    private CardViewFragment.CardType mCardType;
    private final static boolean DOWNLOAD_FISH_SPECIES_FROM_GITHUB = false;
    //private static JSONObject mFishData;    //json object from api_species.json
    private Map<String, CardDetails> mCardDict; //map of fish ID to fish card details. these values are then sorted to the final, mData, order
    private Iterator<CardDetails> mCardListIterator; //iterator of fish IDs in sorted order
    private String mNextSpeciesKey; //todo del
    private ReefLifeDataFragment.ReefLifeDataRetrievalCallback mDataRetrievalCallback;
    private final int NUM_LOAD_PER_ITER = 20; //load 20 cards per iteration


    public InfoCardLoader(Context context, CardViewFragment.CardType cardType, @Nullable String siteCode) {
        super(context);
        mPassedInSurveySiteCode = siteCode;
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
            mData = new ArrayList<>(NUM_LOAD_PER_ITER);
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

            //loadFishCardsIncrementalTWO(); TODO
            //  mCardDict - dictionary of id, fishCard to check against
            //  return mCardDict.Values() or whatever

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


    //todo switching to a new model for loading fish cards into the card list fragment & adapter
    //  this has the benefit of correcting the fish found in each location,
    //  but may be worse performance wise
    //incrementally loads fish cards into mData, based on selected survey sites
    private void loadFishCardsIncrementalTWO() throws IOException, JSONException {

        //todo this list of selected sites should be moved from a static reference to a member on the data frag
        //  List<SurveySite> SELECTED_SURVEY_SITES
        //TODO loadSingleSite isn't properly filtering out cards already picked up (by other sites or previous iterations)
        //  either it must check mData.contains (poor performance) or a Dictionary(id, fishCard)
        //  ideally I would like to see this class's static data member be a dictionary,
        //  and then it can return a list using dictionary
        //TODO performance - when picking back up, all the sites will start looping from the beginning of a new site.getSpeciesFound().keys() iterator
        //  SurveySite could potentially cache the iterator and pick it back up?
        //      how would that behave with the card carousel? should that show 5 new fish each time, or the same 5?
        //  site could potentially      JSONObject fullSpeciesJSON = site.getSpeciesFound();
        //        Iterator<String> speciesList = fullSpeciesJSON.keys();

      /*  int numSitesLeftToLoad = SurveySiteList.SELECTED_SURVEY_SITES.size();
        int numLoaded = 0;
        int numPerSite;


        for(SurveySite site : SurveySiteList.SELECTED_SURVEY_SITES) {

            if (CardViewFragment.CardViewSettings.LOAD_ALL) numPerSite = 999;
            else {
                numPerSite = (NUM_LOAD_PER_ITER - numLoaded) / numSitesLeftToLoad--;
            }
            List<CardDetails> siteList = loadSingleSite(site, mDataRetrievalCallback, numPerSite);
            numLoaded += siteList.size();

            mData.addAll(siteList);
        }*/
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
            if(speciesJSON.length()==0) return; //todo quit better

            mCardDict = mergeFishSpecies(speciesJSON);

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

    /**
     * Filters the site json object to just the sites relevant to this load
     * and aggregates them in a new JSON object
     * also, parses out all the irrelevant site info so we are left with just the species found
     * @return
     */
    private JSONObject setupFishLocations(){
        try {
            JSONObject result = new JSONObject(); //resulting jsonobject aggregated out of separate site jsons
            JSONObject surveySites = loadFishSurveySites(getContext()); //full sitejson from the file

            List<SurveySite> siteList;
            if(mPassedInSurveySiteCode.equals("")){
                siteList = mDataRetrievalCallback.retrieveSurveySiteList().getSelectedSitesAll();
                Log.d("jludden.reeflifesurvey"  , "InfoCardLoader loading favorite survey sites");
            } else {
                siteList = mDataRetrievalCallback.retrieveSurveySiteList().getSitesForCode(mPassedInSurveySiteCode);
                Log.d("jludden.reeflifesurvey"  , "InfoCardLoader loading passed in survey site");
            }

            String selectedSites = "";
            for( SurveySite selSite: siteList){
                Log.d("jludden.reeflifesurvey"  , "InfoCardLoader loading site: "+selSite.getCode()+selSite.getID());

                selectedSites = selectedSites + selSite.getCode() + selSite.getID() + ", ";

                String siteID = selSite.getCode()+selSite.getID();
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

                JSONArray site1 = surveySites.getJSONArray(siteID); // new JSONArray("NSW1");


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

                result.accumulate(siteID, speciesFound);
                result.put(siteID, speciesFound);
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

                String siteID = sitesList.next();
                site = siteJSON.getJSONObject(siteID); // loop through sites

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
                      //  Log.d("jludden.reeflifesurvey"  , "getFishInCards mergeFishSpecies overwriting fish : " + species);
                        cardDetails = speciesDictionary.get(species);
                    }
                    else {
                        cardDetails = new CardDetails(species);
                        speciesDictionary.put(species, cardDetails);
                    }

                    //Add the number of times the fish was seen per site
                    cardDetails.numSightings += numSightings;
                    cardDetails.setFoundInSites(siteID, numSightings);
                }
            }

            return speciesDictionary;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    //11/4 todo shouldn't this really be loading all sites for a code?
    //10/24 todo currently only called from mapview->bottomsheet
            //would like it to be called during the normal incremental load for each site, then have results aggregated
    public static List<CardDetails> loadSingleSite(SurveySite site, ReefLifeDataFragment.ReefLifeDataRetrievalCallback dataRetrievalCallback, final int CARDS_TO_LOAD) throws JSONException {
        Log.d("jludden.reeflifesurvey"  , "InfoCardLoader loadSingleSite: "+site.getSiteName()+" attempting to load: "+CARDS_TO_LOAD+" fish cards");

        List<CardDetails> fishCards = new ArrayList<>(); //todo...
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

            if(fishCards.contains(fishCard)) { //todo verify using overriden equals() func
                Log.d("jludden.reeflifesurvey"  , "load single site - already have fish card loaded: "+fishCard.getId()+"-"+fishCard.commonNames);

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

    /**
     * takes the fishcard in, which really just has an id
     *   jumps to the api_species json and maps out the fields for the species onto the fishCard
     *   ASSUMES mFishData (the raw json from api_species)
     * @param fishCard input card, which should be empty besides the ID
     * @return the same fishCard, with details added (name, image url, etc.)
     * Created 10/24
     */
    private static CardDetails parseSpeciesDetailsHelper(CardDetails fishCard, ReefLifeDataFragment.ReefLifeDataRetrievalCallback dataRetrievalCallback) throws JSONException {
        JSONObject fishSpecies = dataRetrievalCallback.retrieveFishSpecies();
        JSONArray basicData = fishSpecies.getJSONArray(fishCard.getId());
        return parseSpeciesDetailsHelperTwo(fishCard, basicData);
    }

    public static CardDetails parseSpeciesDetailsHelperTwo(CardDetails fishCard, JSONArray basicData) throws JSONException {
        fishCard.cardName = basicData.getString(0);
        fishCard.commonNames = basicData.getString(1);
        fishCard.reefLifeSurveyURL = basicData.getString(2);

        //Parse out image URL
        //todo can do this better. dont set imageurl, just use the first one of the list, etc.
        String imageURL = basicData.getString(4);
        if (imageURL.length() <= 10)  Log.d("jludden.reeflifesurvey"  , "parseSpeciesDetailsHelper no image found1 for card (image url too short): " + fishCard.getId());
        else {
            imageURL = imageURL.substring(2, imageURL.length() - 2); //remove brackets and quotes
            imageURL = imageURL.replace("\\", ""); //remove weird backslashes
            List<String> urls = LoaderUtils.parseURLs(imageURL);
            if (urls.size() < 1)
                Log.d("jludden.reeflifesurvey", "parseSpeciesDetailsHelper no image found for card (image url too short): " + fishCard.getId() + " original url string: "+basicData.getString(4));
            else {
    //            Log.d("jludden.reeflifesurvey", "parseSpeciesDetailsHelper url parsing for: " + fishCard.getId() + " full: " + imageURL + "\n #1: " + urls.get(0));
                fishCard.imageURLs = urls;
            }
        }

        return fishCard;
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
