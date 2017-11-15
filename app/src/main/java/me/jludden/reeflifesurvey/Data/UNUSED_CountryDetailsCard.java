package me.jludden.reeflifesurvey.Data;

import android.graphics.Bitmap;
import android.util.Pair;

import me.jludden.reeflifesurvey.CountryList.model.CityInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample cardName for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class UNUSED_CountryDetailsCard {

    /**
     * An array of sample (model) items.
     */
    public static final List<CountryDetailsTWO> ITEMS = new ArrayList<CountryDetailsTWO>();

    /**
     * A map of sample (model) items, by ID.
     */
    public static final Map<String, CountryDetailsTWO> ITEM_MAP = new HashMap<String, CountryDetailsTWO>();

    private static final int COUNT = 0;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    private static void addItem(CountryDetailsTWO item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static CountryDetailsTWO createDummyItem(int position) {
        return new CountryDetailsTWO(String.valueOf(position), "Item " + position, null);
    }

    private static CountryDetailsTWO createRealItem(int position, String content, String details) {
        return new CountryDetailsTWO(String.valueOf(position), content, null);
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A model item representing a piece of cardName.
     */
    public static class CountryDetailsTWO {
        public final String id;
        public final String content;

        public final Bitmap flagBM;
        public String language;// = "Indonesian"; //todo remove defaults
        public String subregion;// = "South-Eastern Asia";
        public CityInfo capitalCity;



        public CountryDetailsTWO(String id, String content, Bitmap flagBM) {
            this.id = id;
            this.content = content;

            this.flagBM = flagBM;

            //TODO setting defaults
            Pair<Double,Double> coordinates = new Pair<>(Double.valueOf(-6.1744651),Double.valueOf(106.822745));
            language = "Indonesian";
            subregion = "South-Eastern Asia";
        }

        public void setLanguage(String language){
            this.language = language;
        }

        public void setSubregion(String subregion){
            this.subregion = subregion;
        }

        public void setCapitalCity(CityInfo city){
            this.capitalCity = city;
        }


        @Override
        public String toString() {
            return content;
        }
    }
}
