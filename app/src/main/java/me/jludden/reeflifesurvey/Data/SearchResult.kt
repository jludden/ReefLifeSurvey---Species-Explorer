package me.jludden.reeflifesurvey.Data

/**
 * Created by Jason on 11/12/2017.
 */
data class SearchResult(
        val name: String,
        val description: String,
        val imageURL: String,
        val type: Enum<SearchResultType>,
        val id: String
){
    constructor(
            name: String,
            description: String,
            type: Enum<SearchResultType>,
            id: String
    ) : this (
            name,
            description,
            //todo set up placeholder for survey sites
            "http://www.brendontyree.com/wp-content/uploads/2013/10/placeholder_image18.png",
            type,
            id
    )
}

interface SearchResultable {
    fun matchesQuery(query: String): Boolean
    fun createResult(query: String): SearchResult
}

enum class SearchResultType {
    FishSpecies, SurveySiteLocation
}