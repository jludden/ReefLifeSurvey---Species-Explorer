package me.jludden.reeflifesurvey.search

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import me.jludden.reeflifesurvey.data.*
import me.jludden.reeflifesurvey.data.model.FishSpecies
import me.jludden.reeflifesurvey.data.model.SearchResult
import me.jludden.reeflifesurvey.data.model.SearchResultable
import me.jludden.reeflifesurvey.data.model.SurveySiteList
import me.jludden.reeflifesurvey.search.SearchContract.View.Message


/**
 * Created by Jason on 11/12/2017.
 */


class SearchPresenter(
        private val dataRepository: DataSource,
        private val compositeSubscription: CompositeDisposable,
        private val searchView: SearchContract.View)
    : SearchContract.Presenter {

    init {
        searchView.presenter = this
    }

    override fun start() {
        //setupSearchView()
//        searchView.
    }

    override fun onQueryTextChange(query: String?) {
        searchView.clearSearchResults()
        if( query != null && query != "") doSearch(query.toLowerCase())
    }

    private fun doSearch(query: String) {
        searchView.setProgressIndicator(true)

        //merge fish species and survey sites, filter by query string, then take a max of 15 results
        val resultsObservable = Observable.concat<SearchResultable>(
                dataRepository.getFishSpeciesAll(), dataRepository.getSurveySitesAll())
                .filter({ it.matchesQuery(query) })
                .take( SearchFragment.MAX_ITEM_DISPLAY_COUNT )
                .map<SearchResult>({ found -> found.createResult(query) }) //map to correct type

        //subscribe in the SearchView and add it to the composite so it will be cleaned up later
        //  subscribe on count as well to notify view if no results are found
        compositeSubscription.addAll(
                resultsObservable.subscribe(
                        { res -> searchView.addSearchResult(res) }),
                resultsObservable.count().subscribe(
                        {count -> if(count == 0L) noResultsFound() }))
    }

    private fun noResultsFound() {
        searchView.setProgressIndicator(false)
        searchView.setAdditionalMessage(Message.NO_RESULTS_RETURNED)
    }

    override fun onItemClicked(searchResult: SearchResult) {
        searchView.launchResultDetails(searchResult)
    }

}