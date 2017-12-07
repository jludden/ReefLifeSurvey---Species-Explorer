package me.jludden.reeflifesurvey.data;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static me.jludden.reeflifesurvey.data.SharedPreferencesUtils.*;

/**
 * Created by Jason on 8/20/2017.
 *
 * todo - make it have a constructor? or separate out favorite sites from all sites from saved sites
 */

public class SurveySiteList {

    /**
     * Full List of Survey Sites
     */
    private final List<SurveySite> SITE_LIST = new ArrayList<>();

    /**
     * A map of survey sites, by CODE. EMR1, 2, 3, etc should all map to "EMR",[1,2,3,...]
     * Multiple sites share the same realm & location but have different dive site names.
     * We can use this map to select all nearby dive sites
     */
    private final Map<String, List<SurveySite>> CODE_MAP = new HashMap<>();

    //simple list of realms, one survey site per CODE
    //used for building out the Map of markers, where we want to group survey sites in the same location
    public final List<SurveySite> SITE_CODE_LIST = new ArrayList<>();

    //Add a new survey site to all applicable collections
    //todo make private or something why is this a thing
    public void add(SurveySite site){
        SITE_LIST.add(site);

        String key = site.code;
        if(CODE_MAP.containsKey(key)){
            CODE_MAP.get(key).add(site);
        }
        else {
            List<SurveySite> newList = new ArrayList<>();
            newList.add(site);
            CODE_MAP.put(key, newList);
            SITE_CODE_LIST.add(site);
       //     CODE_MAP.put(site.getCode(), site);
        }
    }

    //summarizes the number of sites with the same code and some of the site names
    /*public String codeSummary(String code){
        return codeList(code, 3);    //todo
    }*/
    //@param len number of survey sites, or -1 for no limit

    //get the list of survey sites corresponding to this code
    public List<SurveySite> getSitesForCode(String code){
        return CODE_MAP.get(code);
    }

    //the number of SurveySites in the collection
    public int size(){
        return SITE_LIST.size();
    }

    //list of only selected survey sites
    //but it should probably based on codes instead of sites, because we currently only allow selecting a full code with all sites included
    //todo possibly want this to be a List<Codes> (then we can use CODE_MAP to resolve the list of locations)
    private List<SurveySite> SELECTED_SURVEY_SITES = new ArrayList<>();

    //returns the list of favorited survey site codes
    //EST1, EST2, EST3, etc. will only be returned once as EST

    /**
     * Returns favorited survey sites by code
     *  e.g. EST1, EST2, EST3, etc. will only be returned once as EST
     * @return each unique code in favorited survey sites
     */
    public List<String> getFavoritedSiteCodes(){
        //todo performance
        Map<String, String> codeDict = new Hashtable<>();
        for (SurveySite possiblyDupedSite:
             SELECTED_SURVEY_SITES) {
            codeDict.put(possiblyDupedSite.getCode(),"");
        }
        return new ArrayList<String>(codeDict.keySet());
    }

    /**
     * Returns all favorited survey sites, with multiple ids per code
     *  e.g. EST1, EST2, EST3, etc. will all be returned as separate entries
     * @return all favorited survey site code+id combinations
     */
    public List<SurveySite> getFavoritedSitesAll(){
        return SELECTED_SURVEY_SITES;
    }

    /**
     * Loads the stored survey site codes from SharedPreferences
     * @param context
     * @return
     */
    public void loadFavoritedSites(Context context) {
        Set<String> favSites = getFavSites(context);
        Log.d("jludden.reeflifesurvey"  ,"Loading Favorite Sites : "+favSites.size());
        StringBuilder sitesLoaded = new StringBuilder();

        //convert the set of strings to a real SurveySiteList
        for(String siteCode : favSites){
            if(CODE_MAP.containsKey(siteCode)){
                sitesLoaded.append(siteCode);
                SELECTED_SURVEY_SITES.addAll(CODE_MAP.get(siteCode));
            }
            else {
                Log.e("jludden.reeflifesurvey"  ,"Loading Favorite Sites: unable to resolve favorited site key: "+siteCode);
            }
        }
        Log.d("jludden.reeflifesurvey"  ,"Loading Favorite Sites. successfully loaded : "+sitesLoaded.toString());

    }

    /**
     * NOTE THAT the ADD and REMOVE really only store the code
     *  saving EST1 saves "EST", and EST1, EST2, EST3, etc will all load
     *  this is intended. we only have that level of granularity at this time
     * @param siteCode
     * @param context
     */
    public void addFavoriteSite(String siteCode, Context context){
        List<SurveySite> list = CODE_MAP.get(siteCode);
        if(list == null) {
            Log.e("jludden.reeflifesurvey"  ,"Saving favorite site - unable to resolve sites corresponding to code: "+siteCode);
        }
        else{
            SELECTED_SURVEY_SITES.addAll(list);
            updateFavSites(getFavoritedSiteCodes(),context);
        }
    }

    public void removeFavoriteSite(String siteCode, Context context){
        List<SurveySite> list = CODE_MAP.get(siteCode);
        if(list == null) {
            Log.e("jludden.reeflifesurvey"  ,"Removing favorite site - unable to resolve sites corresponding to code: "+siteCode);
        }
        else {
            SELECTED_SURVEY_SITES.removeAll(list);
            updateFavSites(getFavoritedSiteCodes(),context);
        }
    }

    /**
     * Class to represent a Survey Site location
     *  Only weird thing is that there are
     */
    public static class SurveySite implements SearchResultable {
        public SurveySite(String combinedCode) {
            String[] parts = combinedCode.trim().split("(?=\\d)", 2);
            //Log.d("jludden.reeflifesurvey"  , "surveysite codes:" + combinedCode);

            this.code = parts[0];

            //bunch of junk due to some weird data, when I really just want this.id = Integer.parseInt(parts[1]);
            if (parts.length > 1) id = parts[1];
            else id = "";
        }

        final String code, id; //Combined, this should be like "EST1". We are splitting into separate identifiers for the numeric portion e.g. "EST"+"1"
        String realm;
        String ecoRegion;
        String siteName;
        LatLng position;//double longitude;double latitude;
        int number_of_surveys;
        JSONObject speciesFound;

        /*
         String realm = site1.getString(0);
                String ecoRegion = site1.getString(1);
                String name = site1.getString(2);
                double longitude = site1.getDouble(3);
                double latitude = site1.getDouble(4);
                String number_of_surveys = site1.getString(5);
                //JSONObject speciesFound = site1.getJSONObject(6);
                //Log.d("jludden.reeflifesurvey"  , "MapViewFragment addSurveySites site " + code + " stuff: " + realm + ecoRegion + longitude + latitude);

                //LatLng pos = new LatLng(-37.26, 150.05);
                LatLng pos = new LatLng(latitude, longitude);
        */

        public String getDisplayName() { return ecoRegion + " [" + code + "]";}

        public String getCode() {
            return code;
        }

        public String getID() {
            return id;
        }

        public String getRealm() {
            return realm;
        }

        public void setRealm(String realm) {
            this.realm = realm;
        }

        public String getEcoRegion() {
            return ecoRegion;
        }

        public void setEcoRegion(String ecoRegion) {
            this.ecoRegion = ecoRegion;
        }

        public String getSiteName() {
            return siteName;
        }

        public void setSiteName(String siteName) {
            this.siteName = siteName;
        }

        public LatLng getPosition() {
            return position;
        }

        public void setPosition(LatLng position) {
            this.position = position;
        }

        public int getNumberOfSurveys() {
            return number_of_surveys;
        }

        public void setNumberOfSurveys(int number_of_surveys) {
            this.number_of_surveys = number_of_surveys;
        }

        public JSONObject getSpeciesFound() {
            return speciesFound;
        }

        public void setSpeciesFound(JSONObject speciesFound) {
            this.speciesFound = speciesFound;
        }

        @Override
        public boolean matchesQuery(@NotNull String query) {
            String textToSearch =
                    siteName +
                    code +
                    ecoRegion +
                    realm;

            return textToSearch.toLowerCase().contains(query);
        }

        @NotNull
        @Override
        public SearchResult createResult(@NotNull String query) {
            String name = getDisplayName();

            String description = "";
            if(siteName.toLowerCase().contains(query)){
                description = "name: "+siteName;
            }
            if(code.toLowerCase().contains(query)){
                description = "code: "+code;
            }
            if(ecoRegion.toLowerCase().contains(query)){
                description = "ecoRegion: "+ecoRegion;
            }
            if(realm.toLowerCase().contains(query)){
                description = "realm: "+realm;
            }

            return new SearchResult(name, description, SearchResultType.SurveySiteLocation, code+id);
        }
    }
}