package me.jludden.reeflifesurvey.data.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by Jason on 11/12/2017.
 *
 * todo make prettier
 * https://medium.com/@BladeCoder/reducing-parcelable-boilerplate-code-using-kotlin-741c3124a49a
 */
data class SearchResult(
        val name: String,
        val description: String,
        val imageURL: String,
        val type: Enum<SearchResultType>,
        val id: String)
    : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            SearchResultType.valueOf(parcel.readString()),
            parcel.readString()) {
    }

    constructor(
            name: String,
            description: String,
            type: Enum<SearchResultType>,
            id: String
    ) : this (
            name,
            description,
            "",
            type,
            id
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeString(imageURL)
        parcel.writeString(type.name)
        parcel.writeString(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SearchResult> {
        val INTENT_EXTRA = "SearchResult"
        override fun createFromParcel(parcel: Parcel): SearchResult {
            return SearchResult(parcel)
        }

        override fun newArray(size: Int): Array<SearchResult?> {
            return arrayOfNulls(size)
        }
    }

}


interface SearchResultable {
    fun matchesQuery(query: String): Boolean
    fun createResult(query: String): SearchResult
}

enum class SearchResultType {
    FishSpecies, SurveySiteLocation
}