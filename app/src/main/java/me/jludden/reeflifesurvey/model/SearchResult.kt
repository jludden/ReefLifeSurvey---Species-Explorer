package me.jludden.reeflifesurvey.model

/**
 * Created by Jason on 11/12/2017.
 */
data class SearchResult constructor(
        val name: String,
        val type: Enum<SearchResultType>,
        val description: String = "",
        val imageURL: String? = null
)

interface SearchResultable {
    fun matchesQuery(query: String): Boolean
    fun createResult(query: String): SearchResult
}

enum class SearchResultType {
    FishSpecies, SurveySiteLocation
}