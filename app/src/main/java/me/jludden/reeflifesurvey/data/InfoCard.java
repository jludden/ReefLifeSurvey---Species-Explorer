package me.jludden.reeflifesurvey.data;

import android.app.Activity;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import static me.jludden.reeflifesurvey.data.SharedPreferencesUtils.getPref;

/**
 * Helper class for providing sample cardName for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class InfoCard {

    /**
     * An array of sample (model) items.
     */
    //public static final List<CardDetails> ITEMS = new ArrayList<CardDetails>();

    /**
     * A map of sample (model) items, by ID.
     */
  /*  public static final Map<String, CardDetails> ITEM_MAP = new HashMap<String, CardDetails>();

    private static final int COUNT = 0;*/


    //static members to help load/save to SharedPreferences
    public static final String PREF_FAVORITED = "FAVORITED_KEY";
    public static String generateSharedPrefKey(String id, String valKey) {
        return "me.jludden.reeflifesurvey.CardPref_" + id + "_" + valKey;
    }

    /*
    TODO actually use this as the public interface, use this main class as the card list, you know

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    private static void addItem(CardDetails item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static CardDetails createDummyItem(int position) {
        return new CardDetails(String.valueOf(position), "Item " + position, null);
    }

    private static CardDetails createRealItem(int position, String content, String details) {
        return new CardDetails(String.valueOf(position), content, null);
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }*/

    /**
     * CardDetails class for containing all the data pieces a card needs to be displayed in my app
     * When a listview or pager loads, we assume it will load many of these card details
     * If they load incrementally, they may not be able to garbage collect older entries
     * With that in mind, try not to save any images or drawables here.
     * Those can be loaded and garbage collected by the view adapter
     */
    public static class CardDetails implements Parcelable, Comparable, SearchResultable{
        private static final String TAG = "InfoCard.CardDetails";
        public final String id;
        public String cardName;
        public String subregion;// = "South-Eastern Asia";
        public String commonNames;
        public int numSightings; //TODO this could redone... times seen per survey site... Dictionary<SurveySiteList.SurveySite, Integer> FoundInSites
        public Dictionary<SurveySiteList.SurveySite, Integer> FoundInSites = new Hashtable<>();
        public boolean favorited = false;
        public List<String> imageURLs;
        private boolean offline = false;
        public String reefLifeSurveyURL;

        public static final String INTENT_EXTRA = "InfoCardDetail";

        public CardDetails(String id) {
            this.id = id;
        }

        //todo performance impact? we are not caching this value but preferences should be in memory
        public boolean getFavorited(Activity activity) {
            if(getPref(id, InfoCard.PREF_FAVORITED, activity)) this.favorited = true;
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
           // if(cardName!=null) sb.append(cardName);
            if (commonNames!=null) sb.append("Also known as: " + commonNames);
            if (subregion!=null) sb.append("\n Location: " + subregion);

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

          //  imageBitmap.writeToParcel(dest, flags); TODO bitmap is too big for parcel
            //either shrink bitmap, or save to shared cache

        }

        // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
        public static final Parcelable.Creator<CardDetails> CREATOR = new Parcelable.Creator<CardDetails>() {
            public CardDetails createFromParcel(Parcel in) {
                return new CardDetails(in);
            }

            public CardDetails[] newArray(int size) {
                return new CardDetails[size];
            }
        };

        //constructor for CardDetails that takes a Parcel
        private CardDetails(Parcel in){
            id = in.readString();
            cardName = in.readString();
            commonNames = in.readString();
         //   imageBitmap = in.readParcelable(Bitmap.class.getClassLoader());
        }

        @Override
        public boolean equals(Object obj) {
            CardDetails fishObj = (CardDetails) obj;
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
         *
         * @param o the object to be compared.
         * @return a negative integer, zero, or a positive integer as this object
         * is less than, equal to, or greater than the specified object.
         * @throws NullPointerException if the specified object is null
         * @throws ClassCastException   if the specified object's type prevents it
         *                              from being compared to this object.
         */
        @Override
        public int compareTo(@NonNull Object o) {
            return ((CardDetails) o).numSightings - this.numSightings;
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
                //return "http://www.brendontyree.com/wp-content/uploads/2013/10/placeholder_image18.png"; //todo set up a placeholder image
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
    }
}
