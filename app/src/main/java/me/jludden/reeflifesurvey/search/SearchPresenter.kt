package me.jludden.reeflifesurvey.search

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import me.jludden.reeflifesurvey.data.*
import me.jludden.reeflifesurvey.data.model.SearchResult
import me.jludden.reeflifesurvey.data.model.SearchResultable

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
    }

    override fun onQueryTextChange(query: String?) {
        searchView.clearSearchResults()
        if( query != null && query != "") doSearch(query.toLowerCase())
    }

    private fun doSearch(query: String) {
        //merge fish species and survey sites, filter by query string, then take a max of 15 results
        val matchesQuery: Observable<SearchResultable> =
                Observable.concat(
                    dataRepository.getFishSpeciesAll(),
                    dataRepository.getSurveySitesAll())
                .filter({ it.matchesQuery(query) })
                .take(15)

        //map the resulting species/sites to SearchResults
        val resultsObservable: Observable<SearchResult> = matchesQuery
                .map({ res -> res.createResult(query) })

        //subscribe in the SearchView and add it to the composite so it will be cleaned up later
        compositeSubscription.add(
                resultsObservable.subscribe({ res -> searchView.addSearchResult(res) }))
    }

    override fun onItemClicked(searchResult: SearchResult) {
        searchView.launchResultDetails(searchResult)
    }

}