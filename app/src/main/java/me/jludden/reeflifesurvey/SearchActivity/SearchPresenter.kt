package me.jludden.reeflifesurvey.SearchActivity

import io.reactivex.Observable
import me.jludden.reeflifesurvey.model.*
import me.jludden.reeflifesurvey.model.SurveySiteList.*

/**
 * Created by Jason on 11/12/2017.
 */


class SearchPresenter(
        val dataRepository: DataRepository,
        val searchView: SearchContract.View) :
        SearchContract.Presenter {

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

       // dataRepository.getFishSpeciesObservable(searchView.)

        val matchesQuery: Observable<SearchResultable> =
                Observable.concat(
                    dataRepository.getFishSpeciesObservable(),
                    dataRepository.getSurveySitesObservable())
                .filter({result -> result.matchesQuery(query)})
                .take(15)

        val resultsObservable: Observable<SearchResult> = matchesQuery
                .map({ species -> species.createResult(query) })

        resultsObservable
                .subscribe({ res -> searchView.addSearchResult(res) })

    /*  val fishMatchQuery: Observable<InfoCard.CardDetails> = dataRepository.getFishSpeciesObservable()
                .filter({ species -> species.matchesQuery(query)})
                .take(15)

        val resultsObservable: Observable<SearchResult> = fishMatchQuery
                .map({ species -> species.createResult(query) })

        resultsObservable
                .subscribe({ res -> searchView.addSearchResult(res) })
    */

    /*    val sitesMatchingQuery: Observable<SurveySite> = dataRepository.getSurveySitesObservable()
                .filter({ site -> siteMatchesQuery(site, query) })
                .take(15)

        val resultsObservable: Observable<SearchResult> = sitesMatchingQuery
                .map({ site -> newSearchResult(site, query) }) //change the type from SurveySite to SearchResult

        resultsObservable
                .subscribe({ res -> searchView.addSearchResult(res) })
    */
    }

}