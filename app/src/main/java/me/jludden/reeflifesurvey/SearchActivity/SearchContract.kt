package me.jludden.reeflifesurvey.SearchActivity

import me.jludden.reeflifesurvey.model.SearchResult

/**
 * Created by Jason on 11/12/2017.
 */
class SearchContract {

    interface View : BaseView<Presenter> {
        val isActive: Boolean

        fun setProgressIndicator(active: Boolean)

        fun addSearchResult(result: SearchResult)

        fun clearSearchResults()

     // fun showSearchResults(results: List<SearchResult>)

     // fun showSearchError()
    }

    interface Presenter : BasePresenter {

        fun onQueryTextChange(query: String?)

        fun onQueryTextSubmit(query: String?)

    }
}

interface BaseView<T> {

    var presenter: T
}

interface BasePresenter {

    fun start()

}