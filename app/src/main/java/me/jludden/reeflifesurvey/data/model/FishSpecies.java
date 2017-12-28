package me.jludden.reeflifesurvey.data.model;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;

import org.jetbrains.annotations.NotNull;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import me.jludden.reeflifesurvey.data.utils.StoredImageLoader;

import static me.jludden.reeflifesurvey.data.utils.SharedPreferencesUtils.getPref;

/**
 * FishSpecies class for containing all the data pieces a fish card needs to be displayed in my app
 * When a listview or pager loads, we assume it will load many of these card details
 * If they load incrementally, they may not be able to garbage collect older entries
 * With that in mind, try not to save any images or drawables here.
 * Those can be loaded and garbage collected by the view adapter
 */
public class FishSpecies implements Parcelable, Comparable, SearchResultable {
    public final String id;
    public String cardName;
    public String commonNames;
    public int numSightings; //TODO
    public Dictionary<SurveySiteList.SurveySite, Integer> FoundInSites = new Hashtable<>();
    public boolean favorited = false;
    public List<String> imageURLs;
    private boolean offline = false;
    public String reefLifeSurveyURL;

    public static final String INTENT_EXTRA = "SpeciesCard";
    //static members to help load/save to SharedPreferences
    public static final String PREF_FAVORITED = "FAVORITED_KEY";
    private static final String TAG = "InfoCard.FishSpecies";

    public FishSpecies(String id) {
        this.id = id;
    }

    public static String generateSharedPrefKey(String id, String valKey) {
        return "me.jludden.reeflifesurvey.CardPref_" + id + "_" + valKey;
    }

    //todo performance impact? we are not caching this value but preferences should be in memory
    public boolean getFavorited(Activity activity) {
        if(getPref(id, PREF_FAVORITED, activity)) this.favorited = true;
        return this.favorited;
    }

    public void setFoundInSites(String siteID, int sightingsCount) {
        //todo
        // /SurveySiteList.SurveySite site =
        //setFoundInSites(site, sightingsCount);
    }

    public void setFoundInSites(SurveySiteList.SurveySite site, int sightingsCount){
        int prevCount = FoundInSites.get(site) != null ? FoundInSites.get(site) : 0;
        FoundInSites.put(site, prevCount+sightingsCount);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        if (commonNames!=null) sb.append("Also known as: " + commonNames);

        //sb.append("\nSeen " + numSightings + " times");

        return sb.toString();
    }


    //#region Parcelable methods
    @Override
    public int describeContents() {
        return 0; //usually can ignore this
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(cardName);
        dest.writeString(commonNames);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Creator<FishSpecies> CREATOR = new Creator<FishSpecies>() {
        public FishSpecies createFromParcel(Parcel in) {
            return new FishSpecies(in);
        }

        public FishSpecies[] newArray(int size) {
            return new FishSpecies[size];
        }
    };

    //constructor for FishSpecies that takes a Parcel
    private FishSpecies(Parcel in){
        id = in.readString();
        cardName = in.readString();
        commonNames = in.readString();
     //   imageBitmap = in.readParcelable(Bitmap.class.getClassLoader());
    }

    @Override
    public boolean equals(Object obj) {
        FishSpecies fishObj = (FishSpecies) obj;
        return (this.getId().equals(fishObj.getId()));
    }

    @Override
    public int hashCode() {
        int result = 31;
        result = result * 17
                + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    /**
     * Compares this object with the specified object for order based on the number of sightings
     * TODO better fish species comparator
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(@NonNull Object o) {
        return ((FishSpecies) o).numSightings - this.numSightings;
    }

    public boolean getOffline(){
        return offline;
    }

    public void setOffline(boolean isOffline){
        offline = isOffline;
    }

    public String getId() {
        return id;
    }

    public String getPrimaryImageURL() {
        if(imageURLs == null || imageURLs.get(0) == null) {
            Log.d(TAG, "Card "+getId()+ "-"+cardName+" no primary URL found");
            return "";
        } else {
            return imageURLs.get(0);
        }
    }

    @Override
    public boolean matchesQuery(@NotNull String query) {
        return cardName.toLowerCase().contains(query) || commonNames.toLowerCase().contains(query);
    }

    @NotNull
    @Override
    public SearchResult createResult(@NotNull String query) {
        return new SearchResult(cardName, commonNames, getPrimaryImageURL(), SearchResultType.FishSpecies, getId());
    }

    /**
     * Tries to load the saved-offline primary image for this species into the passed in imageview
     * @param storedImageLoader
     * @param imageView
     * @return true if loaded into the imageview successfully
     */
    public boolean tryLoadPrimaryImageOffline(StoredImageLoader storedImageLoader, ImageView imageView) {
        if(getOffline()) {
            Bitmap img = storedImageLoader.loadPrimaryCardImage(this);
            if(img != null) {
                imageView.setImageBitmap(img);
                return true;
            }
        }
        return false;
    }
}
