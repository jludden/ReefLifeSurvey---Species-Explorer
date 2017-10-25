package me.jludden.reeflifesurvey.CountryList.model;

import android.support.v4.util.Pair;

/**
 * Created by Jason on 5/21/2017.
 */

public class CityInfo {
    private final String city;
    private final String timeZone;
    private final Pair<Double, Double> coordinates;

    public CityInfo(String city, String timeZone, Pair<Double, Double> coordinates) {

        this.city = city;
        this.timeZone = timeZone;
        this.coordinates = coordinates;

    }

    public String toString(){
        return city + " (" + (timeZone) + ") [" + coordinates.first + "," + coordinates.second + "]";
    }
}
