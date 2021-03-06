package me.jludden.reeflifesurvey.data;

import android.content.Context;
import android.os.OperationCanceledException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import me.jludden.reeflifesurvey.Injection;
import me.jludden.reeflifesurvey.data.model.SurveySiteList;
import me.jludden.reeflifesurvey.data.utils.LoaderUtils;
import me.jludden.reeflifesurvey.data.utils.StorageUtils;
import me.jludden.reeflifesurvey.fishcards.CardViewFragment;
import me.jludden.reeflifesurvey.data.model.FishSpecies;
import me.jludden.reeflifesurvey.R;
import me.jludden.reeflifesurvey.data.model.SurveySiteList.SurveySite;

import org.jetbrains.annotations.NotNull;
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
import java.util.Set;

import static me.jludden.reeflifesurvey.data.utils.LoaderUtils.isOnline;
import static me.jludden.reeflifesurvey.data.utils.SharedPreferencesUtils.loadAllSitesStoredOffline;

/**
 * Created by Jason on 5/1/2017.
 *
 * Loads and parses data from the ReefLifeSurvey API into a List of InfoCard.FishSpecies
 *
 * Request siteRequest = new Request.Builder()
 * .url("https://yanirs.github.io/tools/rls/api-site-surveys.json")
 * .build();
 * Response siteResponse = client.newCall(siteRequest).execute();
 * String siteResult = siteResponse.body().string();
 * Log.d(TAG  ,"getFishInCards siteResult: "+ siteResult.substring(0,50000));
 *
 * *** Survey site format ***
 * {code: [
 * realm,
 * ecoregion,
 * name,
 * longitude,
 * latitude,
 * number_of_surveys,
 * {
 * fishSpecies: surveyedCount
 * }], ... }
 *
 * such as:
 * {"NSW1":[
 * "Temperate Australasia",
 * "Cape Howe",
 * "Green Cape SE",
 * 150.05,
 * -37.26,
 * 4,
 * {
 * "1027":3,
 * "3005":3,
 * "3087":1,
 * "791":4
 * }], ... }
 *
 *
 *
 * *** Fish species format ***
 * {
 *  0: [
 *      "Scientific Name",
 *      "Common name, common name, common name",
 *      "https://reeflifesurvey.com/species/###", --species key
 *      #,                                        --
 *      [
 *          "https image1",
 *          "https image2"
 *      ]
 *  ],
 *  1: [
 *      ...
 *  ]
 * }
 *
 */

public class InfoCardLoader extends AsyncTaskLoader<List<FishSpecies>> implements DataRepository.LoadSurveySitesCallBack, DataRepository.LoadFishCardCallBack {

    private static final String TAG = "InfoCardLoader";
    private List<FishSpecies> mData; //final, sorted list of fish cards
    private String mPassedInSurveySiteCode = "";//optional passed in parameter. otherwise load favorite sites
    private CardViewFragment.CardType mCardType;
    private final static boolean DOWNLOAD_FISH_SPECIES_FROM_GITHUB = false;
    private Map<String, FishSpecies> mCardDict; //map of fish ID to fish card details. these values are then sorted to the final, mData, order
    private Iterator<FishSpecies> mCardListIterator; //iterator of fish IDs in sorted order
    private final int NUM_LOAD_PER_ITER = 20; //load 20 cards per iteration
    private SurveySiteList mSurveySitesList;

    private boolean loadingOffline = false;

    public InfoCardLoader(Context context, CardViewFragment.CardType cardType, @Nullable String siteCode) {
        super(context);
        mPassedInSurveySiteCode = siteCode;
        mCardType = cardType;
        Log.d(TAG  , "InfoCardLoader Created. Card Type to load: " + cardType);
    }

    /**
     * Starts the downloader
     */
    @Override
    protected void onStartLoading() {
        Log.d(TAG  , "InfoCardLoader onStartLoading called. takecontentchanged: " + takeContentChanged());
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
    public List<FishSpecies> loadInBackground() {

        boolean isOnline = isOnline("www.reeflifesurvey.com");
        Log.d(TAG  , "InfoCardLoader loadInBackground called "+isOnline+"_"+(mData == null || mData.size() <= 0)+"_"+(mPassedInSurveySiteCode.equals("")));

        //no internet, no passed in site - load downloaded
        //todo this is totally unnecessary, no part of this loading code needs to be online anyway
        if(!isOnline && (mData == null || mData.size() <= 0)) {
            Set<String> storedSites = loadAllSitesStoredOffline(getContext().getApplicationContext());
            Log.d(TAG, "InfoCardLoader: No Internet Detected and no passed in site - showing all downloaded ("+storedSites.size()+" sites stored offline");
            loadingOffline = true;
//            loadOffline();
//            return mData; //todo
        }


        if (mData == null) {
            mData = new ArrayList<>(NUM_LOAD_PER_ITER);
        } else {
            // TODO jank - loader manager won't call load finished if the data reference is the same?
            // TODO UPDATE 8/12 seems fine UPDATE 10/12 it will mostly work with the adapter, but loadfinished is definitely not called
            ArrayList<FishSpecies> tempList = new ArrayList<>();
            tempList.addAll(mData);
            mData = tempList;
        }

        try {

            loadFishCardsIncremental(); //Load more fish cards into mData


            //loadFishCardsIncrementalTWO(); TODO
            //  mCardDict - dictionary of id, fishCard to check against
            //  return mCardDict.Values() or whatever


        } catch (IOException e) {
            Log.e("jludden.reeflifesurvey", "IOException: " + e.toString());
        } catch (JSONException e) {
            Log.e("jludden.reeflifesurvey", "JSONException: " + e.toString());
        }

        return mData;

    }

    //todo switching to a new model for loading fish cards into the card list fragment & adapter
    //  this has the benefit of correcting the fish found in each location,
    //  but may be worse performance wise
    //incrementally loads fish cards into mData, based on selected survey sites
    private void loadFishCardsIncrementalTWO() throws IOException, JSONException {

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
            List<FishSpecies> siteList = loadSingleSite(site, mDataRetrievalCallback, numPerSite);
            numLoaded += siteList.size();

            mData.addAll(siteList);
        }*/
    }

    /**
     * incrementally loads fish cards into mData
     * <p>
     * Using Yanir Seroussi's API, from:
     * https://yanirseroussi.com/2017/06/03/exploring-and-visualising-reef-life-survey-data/
     * <p>
     * https://yanirs.github.io/tools/rls/api-species.json
     * species.json – a mapping from species ID to an array of five elements: scientific name, common name, species page URL, survey method (0: method 1, 1: method 2, or 2: both), and images (array of URLs).
     * https://yanirs.github.io/tools/rls/api-site-surveys.json
     * site-surveys.json – a mapping from site code to an array of seven elements: realm, ecoregion, site name, longitude, latitude, number of surveys, and species counts (mapping from each observed species ID to the number of surveys on which it was seen).
     */
    private void loadFishCardsIncremental() throws IOException, JSONException {
        // Call to set up the site surveys
        if(mCardDict ==  null) {
            //terrible todo
            if(mSurveySitesList == null) {
                Injection.provideDataRepository(getContext().getApplicationContext())
                        .getSurveySites(SurveySiteType.ALL_IDS, this);
                int i = 0;
                while (mSurveySitesList == null && i < 50) { //todo handle failure to load
                    try {
                        Log.d(TAG, "InfoCardLoader loading waiting: " + i++);
                        wait(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (i >= 50) {
                    Log.e(TAG, "InfoCardLoader failed to load survey sites");
                }
            }

            //todo restructure
            LoaderUtils.SPECIES_LOAD_TYPE loadList = LoaderUtils.determineLoadType(mPassedInSurveySiteCode, mSurveySitesList, null);
            Log.d(TAG, "setupFishLocations: loading: "+loadList);

            if(loadList == LoaderUtils.SPECIES_LOAD_TYPE.ALL_SPECIES) {
                mCardDict = new HashMap<>(); //dictionary mapping a fish id to its carddetails
                Injection.provideDataRepository(getContext().getApplicationContext())
                        .getFishSpeciesAll()
//                        .take(CardViewFragment.CardViewSettings.LOAD_ALL ? 999) //todo
                        .subscribe(fish -> mData.add(fish));
                return;
            }

            JSONObject speciesJSON = setupFishLocations();
            if (speciesJSON.length() == 0) return; //todo quit better

            mCardDict = mergeFishSpecies(speciesJSON);

            //sort the species by most common TODO performance could be improved, certainly
            //todo the number of sitings is not accurate, so this probably doesn't do anything
            //FishSpecies[] fishArray =  (FishSpecies[]) mCardDict.values().toArray(FishSpecies.type());
            FishSpecies[] fishArray = new FishSpecies[mCardDict.size()];
            mCardDict.values().toArray(fishArray);
            List<FishSpecies> fishList = new ArrayList<>(Arrays.asList(fishArray));
            Collections.sort(fishList);
            mCardListIterator = fishList.iterator();
            //or, if we don't want it sorted, just do: //Iterator<String> mCardListIterator = mCardDict.keySet().iterator(); //species iterator from dictionary
        }

        //todo
        if(LoaderUtils.determineLoadType(mPassedInSurveySiteCode, mSurveySitesList, null) == LoaderUtils.SPECIES_LOAD_TYPE.ALL_SPECIES) return;

//        int MAX_NUM_EL = mLoadAll ? 999 : 20; //only applies if loading incrementally
        int MAX_NUM_EL = CardViewFragment.CardViewSettings.LOAD_ALL ? 999 : 20; //only applies if loading incrementally

        int count = 0;
        String speciesKey;

        //add new fish
        while(mCardListIterator.hasNext() && (++count < MAX_NUM_EL)) {
            speciesKey = mCardListIterator.next().id;

            //Log.d(TAG  , "getFishInCards Download full string: " + speciesKey + " : " + basicData.toString());

            Injection.provideDataRepository(getContext().getApplicationContext()).getFishCard(speciesKey, this);
            
            
         //   FishSpecies cardDetails = mCardDict.get(speciesKey) ; // infocard shell already created - time to add details
            //Log.d(TAG  , "getFishInCards ID: "+cardDetails.getId()+" VS SPECIESKEY: " + speciesKey); //YES THEY ARE THE SAME

          // parseSpeciesDetailsHelper(cardDetails, mDataRetrievalCallback);

            //mData.add(cardDetails); //Add card to final card list
        }
    }

    /**
     * Filters the site json object to just the sites relevant to this load
     * and aggregates them in a new JSON object
     * also, parses out all the irrelevant site info so we are left with just the species found
     *
     * Loading order for the browse species screen:
     * 1. load passed in site
     * 2. load favorite sites
     * 3. load favorite species
     * 4. load none (show no sites selected. load popular fish?)
     * 5. load popular? fish
     *
     * @return
     */
    private JSONObject setupFishLocations(){



        List<SurveySite> siteList;
        //todo moving this logic to LoaderUtils.determineLoadType
        if(mPassedInSurveySiteCode.equals("")){
            siteList = mSurveySitesList.getFavoritedSitesAll();
            if(siteList.size() > 0)  Log.d(TAG  , "InfoCardLoader setupFishLocations loading "+siteList.size()+" favorite survey sites");
            else Log.d(TAG, "InfoCardLoader setupFishLocations no favorite sites or passed in site ");
        } else {
            siteList = mSurveySitesList.getSitesForCode(mPassedInSurveySiteCode);
            Log.d(TAG  , "InfoCardLoader setupFishLocations loading passed in survey site");
        }



        try {
            //todo at this point i would love to no longer use any JSON at all
            JSONObject result = new JSONObject(); //resulting jsonobject aggregated out of separate site jsons

            //todo load favorite fish instead (if no favorite sites or passed in site)
        /*    FAVORITE_SPECIES_LOADED = true;
            SharedPreferencesUtils.LoadAllFavoriteSpecies
            speciesFound = {"2952":1,"1420":1,"1423":2,"2963":2,"1691":1,"1692":1}
            result.accumulate(0, speciesFound);
            result.put(0, speciesFound);*/


            StringBuilder selectedSites = new StringBuilder(siteList.size() * 7);
            for( SurveySite selSite: siteList){
               // Log.d(TAG  , "InfoCardLoader loading site: "+selSite.getCode()+selSite.getID());
                selectedSites.append(selSite.getCode()).append(selSite.getID()).append(", ");
                String siteID = selSite.getCode()+selSite.getID();
                JSONObject speciesFound = selSite.getSpeciesFound();

                result.accumulate(siteID, speciesFound);
                result.put(siteID, speciesFound);
            }

            Log.d(TAG  , "InfoCardLoader setupFishLocations SELECTED SITES: " + selectedSites.toString());
            return result;

        } catch (JSONException e){
            Log.d(TAG  , "setupFishLocations JSONException: " + e.toString());

        }
        return null;
    }

    //merge fish species from json to the dictionary keys. then we will add the cards to the dictionary in getfishcards
    //todo shouldn't we be switching to using the SurveySiteList object, instead of raw json
    private HashMap<String, FishSpecies> mergeFishSpecies(JSONObject siteJSON) {
        HashMap<String, FishSpecies> speciesDictionary = new HashMap<>(); //dictionary mapping a fish id to its carddetails
        //PriorityQueue<Integer> fishSightingsIndex = new PriorityQueue<>();
        //multiset or bag?
        //HashMap<Integer, Integer> fishSightingsIndex = new HashMap<>(); //dictionary mapping the number of sightings for each fish to its fish ID
        Log.d(TAG  , "getFishInCards mergeFishSpecies: " + siteJSON.toString().substring(0,15));

        try {
            //return siteJSON.getJSONObject("NSW1");
            int siteCount = 0;
            int MAX_NUM_SITE_EL = 200; //?
            Iterator<String> sitesList = siteJSON.keys();
            JSONObject site;
            while(sitesList.hasNext() && siteCount++ < MAX_NUM_SITE_EL) {

                String siteID = sitesList.next();
                site = siteJSON.getJSONObject(siteID); // loop through sites

                String species;
                int numSightings;
                int count = 0;
                int MAX_NUM_SPECIES_PER_SITE_EL = 200; //?
                Iterator<String> speciesList = site.keys();

                //add each species to the dictionary
                while (speciesList.hasNext() && count++ < MAX_NUM_SPECIES_PER_SITE_EL) {
                    species = speciesList.next();
                    numSightings = site.getInt(species);
                    FishSpecies cardDetails;

                    if(speciesDictionary.containsKey(species)){ //check if already added from previously surveyed site
                      //  Log.d(TAG  , "getFishInCards mergeFishSpecies overwriting fish : " + species);
                        cardDetails = speciesDictionary.get(species);
                    }
                    else {
                        cardDetails = new FishSpecies(species);
                        speciesDictionary.put(species, cardDetails);
                    }

                    //Add the number of times the fish was seen per site
                    cardDetails.numSightings += numSightings;
                    cardDetails.setFoundInSites(siteID, numSightings);
                    if(loadingOffline) cardDetails.setOffline(true);
                }
            }

            return speciesDictionary;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * takes the fishcard in, which really just has an id and parses its corresponding JSON data
     *
     * @param fishCard input card, which should be empty besides the ID, and the JSONArray
     * @param basicData JSONArray of data for this fish species
     * @return the same fishCard, with details added (name, image url, etc.)
     * Created 10/24
     */
    public static FishSpecies parseSpeciesDetailsHelper(FishSpecies fishCard, JSONArray basicData) throws JSONException {
        fishCard.scientificName = basicData.getString(0);
        fishCard.commonNames = basicData.getString(1);
        fishCard.reefLifeSurveyURL = basicData.getString(2);

        //Parse out image URL
        String imageURL = basicData.getString(4);
        if (imageURL.length() <= 10)  Log.d(TAG  , "parseSpeciesDetailsHelper no image found1 for card (image url too short): " + fishCard.getId());
        else {
            imageURL = imageURL.substring(2, imageURL.length() - 2); //remove brackets and quotes
            imageURL = imageURL.replace("\\", ""); //remove weird backslashes
            List<String> urls = LoaderUtils.parseURLs(imageURL);
            if (urls.size() < 1)
                Log.d(TAG, "parseSpeciesDetailsHelper no image found for card (image url too short): " + fishCard.getId() + " original url string: "+basicData.getString(4));
            else {
    //            Log.d(TAG, "parseSpeciesDetailsHelper url parsing for: " + fishCard.getId() + " full: " + imageURL + "\n #1: " + urls.get(0));
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

    //no internet so lets just load whatevers on disk
    private void loadOffline() {
        Log.d(TAG, "InfoCardLoader loadOffline");
        mData = new ArrayList<>();

        Observable<FishSpecies> offlineCards
                = StorageUtils.Companion.loadStoredFishCards(getContext().getApplicationContext());

        offlineCards.subscribe(new Consumer<FishSpecies>() {
            @Override
            public void accept(FishSpecies cardDetails) throws Exception {
                Log.d(TAG, "load offline accept: "+cardDetails.scientificName +" ["+cardDetails.getId()+"]");

                cardDetails.setOffline(true);
                mData.add(cardDetails);
            }
        });


    }

    @Override
    public void onSurveySitesLoaded(@NotNull SurveySiteList sites) {
        mSurveySitesList = sites;
    }

    @Override
    public void onFishCardLoaded(@NotNull FishSpecies card) {
        mData.add(card);
    }

    @Override
    public void onDataNotAvailable(@NonNull String reason) {
        Log.d(TAG, "onDataNotAvailable: failed to load card id: "+reason);
    }
}
