package me.jludden.reeflifesurvey.model;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jason on 8/20/2017.
 */

public class SurveySiteList {

    /**
     * An array of sample (model) items.
     */
    public final List<SurveySite> ITEMS = new ArrayList<>();

    /**
     * A map of survey sites, by CODE. EMR1, 2, 3, etc should all map to "EMR",[1,2,3,...]
     * Multiple sites share the same realm & location but have different dive site names.
     * We can use this map to select all nearby dive sites
     */
    public final Map<String, List<SurveySite>> ITEM_MAP = new HashMap<>();

    //simple list of realms, one survey site per CODE
    public final List<SurveySite> SITE_CODE_LIST = new ArrayList<>();

    //Add a new survey site to all applicable collections
    //todo make private or something why is this a thing
    public void add(SurveySite site){
        ITEMS.add(site);

        String key = site.code;
        if(ITEM_MAP.containsKey(key)){
            ITEM_MAP.get(key).add(site);
        }
        else {
            List<SurveySite> newList = new ArrayList<>();
            newList.add(site);
            ITEM_MAP.put(key, newList);
            SITE_CODE_LIST.add(site);
        }

    }

    //summarizes the number of sites with the same code and some of the site names
    public String codeSummary(String code){
        return codeList(code, 3);    //todo
    }
    public String codeList(String code, int len){ //todo change len to charlen instead of numsites
        List<SurveySite> list = ITEM_MAP.get(code);
        if(list == null) return "Null - no sites";

        StringBuilder nameBuilder = new StringBuilder(list.size()+": ");

        int iterCount = 0;
        for(SurveySite tSite : list){
            nameBuilder.append(tSite.getSiteName()+", ");
            if(++iterCount>len) {
                nameBuilder.append(" ...");
                break;
            }
        }

        return nameBuilder.toString();
    }

    //the number of SurveySites in the collection
    public int size(){
        return SITE_CODE_LIST.size();
    }

    //#region STATIC
    //list of only selected survey sites
    public static List<SurveySite> SELECTED_SURVEY_SITES = new ArrayList<>(); //todo should i really have static here and also a data fragment? can this be in data frag? can it be a list<ids>?
    //public boolean selectSite(String code)


    public static class SurveySite {
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

    }
}