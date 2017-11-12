package me.jludden.reeflifesurvey.SearchActivity

import android.util.Log
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import me.jludden.reeflifesurvey.model.DataRepository
import me.jludden.reeflifesurvey.model.SearchResult
import me.jludden.reeflifesurvey.model.SearchResultType
import me.jludden.reeflifesurvey.model.SurveySiteList
import me.jludden.reeflifesurvey.model.SurveySiteList.*
import java.util.concurrent.TimeUnit

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

    override fun onQueryTextChange(query: String?) {
        doSearch(query)

    }

    override fun onQueryTextSubmit(query: String?) {
        searchView.clearSearchResults()
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    //todo have a Subject handle queries, perform debounce etc on it, then re-emit it as a query using the reset of the logic here
    private fun doSearch(query: String?) {


        Log.d("jludden.reeflifesurvey","Search Presenter DoSearch...")
        //dataRepository.getSurveySites(this)


        //use Observable.from to emit items one at a time from a iterable
        /*fun getSurveySitesObservable(): Observable<SurveySite> {
            return Observable.fromIterable(siteList.SITE_CODE_LIST)
        }*/


        searchView.clearSearchResults()

        if( query == null) return



        var sitesMatchingQuery: Observable<SurveySite> = dataRepository.getSurveySitesObservable()
                .filter({ site -> siteMatchesQuery(site, query) })
                .take(5)

        var resultsObservable: Observable<SearchResult> = sitesMatchingQuery
                .map({ site -> newSearchResult(site, query) }) //change the type from SurveySite to SearchResult

        resultsObservable
                .subscribe({ res -> searchView.addSearchResult(res) })



//        sitesMatchingQuery.subscribe(

        //sitesMatchingQuery = sitesMatchingQuery.map({}) use .map to filter out sites that dont match
        //or .filter( x => x > 10)


    }

    private fun newSearchResult(site: SurveySite, query: String) : SearchResult {
        val type = SearchResultType.SurveySiteLocation
        val name = "${site.ecoRegion}:(${site.code})"

        var description = ""
        if(site.siteName.contains(query)){
            description = "name: ${site.siteName}"
        }
        if(site.code.contains(query)){
            description = "code: ${site.code}"
        }
        if(site.ecoRegion.contains(query)){
            description = "ecoRegion: ${site.ecoRegion}"
        }
        if(site.realm.contains(query)){
            description = "realm: ${site.realm}"
        }

        return SearchResult(name, type, description, "")
    }

    private fun siteMatchesQuery(site : SurveySite,  query: String) : Boolean {
        val textToSearch = StringBuilder()
        textToSearch
                .append(site.siteName)
                .append(site.code)
                .append(site.ecoRegion)
                .append(site.realm)

        if(textToSearch.contains(query,ignoreCase = true)){
            return true
        };

        return false;
    }

    //todo temp delete
    private fun setupSearchView() {

                var helpMe: Disposable
                        = Observable
                        .create<List<SearchResult>> {  }
                        .take(1)
                        .subscribe()

                var meToo: Observable<List<SearchResult>>
                        = Observable
                        .create<List<SearchResult>> {  }



                /*search_view.setOnQueryTextFocusChangeListener({
                    v, hasFocus -> Log.d("jludden.reeflifesurvey", "search view query TEXT FOCUS CHANGE")
                })*/


        /* PseudoCode:
         *  Observable<List<SearchResults>> allItems = dataRepository.get
         *  Observable<List<SearchResults>> filtered = allItems.filter(item -> contains(item)
         *
         *
         *
         *
         */

        Observable
                .interval(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ i ->
                    searchView.addSearchResult(
                            SearchResult("abc", SearchResultType.SurveySiteLocation, "", ""))
                })


        //basically just submit a search if the search field is updated...
        Observable
                .just("Hello World")
             /*   .create(ObservableOnSubscribe<String> { subscriber ->
                    search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String): Boolean {
                            //  searchFor(query)
                        }

                        override fun onQueryTextChange(query: String): Boolean {
                            if (TextUtils.isEmpty(query)) {
                                //        clearResults()
                            }
                            subscriber.onNext(query)
                            return true
                        }
                    })
                })
*/


                //Throttles:
                // debounce - emits only those items from the source Observable that are not followed by another item within a specified duration
                // distinctUntilChanged - as name suggest it won't emit the same item twice in a row
                //.debounce(1000, TimeUnit.MILLISECONDS )
                //.distinctUntilChanged() //only query if its new


                //todo possibly want to set up the thread its on?
                //.subscribe({text -> submitSearch(text)});

                //

                //call something here

                //

               // .subscribeOn(Schedulers.newThread())
               // .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ text: String ->
                    searchView.addSearchResult(
                            SearchResult(text, SearchResultType.SurveySiteLocation, "", ""))
                })

    }

    //todo handle a callback and update the search view contents

}