package me.jludden.reeflifesurvey.search

import io.reactivex.Observable
import me.jludden.reeflifesurvey.data.*

/**
 * Created by Jason on 11/12/2017.
 */


class SearchPresenter(
        val dataRepository: DataRepository,
        val searchView: SearchContract.View)
    : SearchContract.Presenter {

    init {
        searchView.presenter = this
    }

    override fun start() {
        //setupSearchView()
    }

    override fun onQueryTextSubmit(query: String?) {
        searchView.clearSearchResults()
        //TODO not really sure what to do here because i am already querying on text change
    }

    override fun onQueryTextChange(query: String?) {
        searchView.clearSearchResults()
        if( query != null && query != "") doSearch(query.toLowerCase())
    }

    private fun doSearch(query: String) {
        //merge fish species and survey sites, filter by query string, then take a max of 15 results
        val matchesQuery: Observable<SearchResultable> =
                Observable.concat(
                    dataRepository.getFishSpeciesObservable(),
                    dataRepository.getSurveySitesObservable())
                .filter({ it.matchesQuery(query) })
//                .filter({ result -> result.matchesQuery(query) }) todo IT. syntax
                .take(15)

        //map the resulting species/sites to SearchResults
        val resultsObservable: Observable<SearchResult> = matchesQuery
                .map({ res -> res.createResult(query) })

        resultsObservable
                .subscribe({ res -> searchView.addSearchResult(res) })

    }

    override fun onItemClicked(searchResult: SearchResult) {
        searchView.launchResultDetails(searchResult)
    }

}