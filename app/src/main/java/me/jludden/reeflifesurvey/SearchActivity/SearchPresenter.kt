package me.jludden.reeflifesurvey.SearchActivity

import android.text.TextUtils
import android.util.Log
import android.widget.SearchView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.activity_search_results_fragment.*
import me.jludden.reeflifesurvey.model.DataRepository
import me.jludden.reeflifesurvey.model.SearchResult
import me.jludden.reeflifesurvey.model.SearchResultType
import java.util.concurrent.TimeUnit

/**
 * Created by Jason on 11/12/2017.
 */


class SearchPresenter(
        val dataRepository: DataRepository,
        val searchView: SearchContract.View
) : SearchContract.Presenter {

    init {
        searchView.presenter = this
    }

    override fun start() {
        setupSearchView()
    }

    override fun onQueryTextChange(query: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onQueryTextSubmit(query: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun setupSearchView() {
        /*search_view.setOnQueryTextFocusChangeListener({
            v, hasFocus -> Log.d("jludden.reeflifesurvey", "search view query TEXT FOCUS CHANGE")
        })*/


        /* PseudoCode:
         *  Observable<List<SearchResults>> searchAsync = dataRepository.query(queryText)
         *  Observable<List<SearchResults>> searchAsync.map(
         *
         */



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

                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    text : String -> searchView.showSearchResults(
                        ArrayList<SearchResult>()
                                //.also(it.add(
                             //   SearchResult(text,SearchResultType.FishSpecies,"")))
                    )
                })
    }

    //todo handle a callback and update the search view contents

}