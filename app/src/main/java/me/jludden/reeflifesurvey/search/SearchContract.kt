package me.jludden.reeflifesurvey.search

import me.jludden.reeflifesurvey.data.SearchResult

/**
 * Created by Jason on 11/12/2017.
 */
class SearchContract {

    interface View : BaseView<Presenter> {
        val isActive: Boolean

        fun setProgressIndicator(active: Boolean)

        fun addSearchResult(result: SearchResult)

        fun clearSearchResults()

        fun launchResultDetails(searchResult: SearchResult)
    }

    interface Presenter : BasePresenter {

        fun onQueryTextChange(query: String?)

        fun onQueryTextSubmit(query: String?)

        fun onItemClicked(searchResult: SearchResult)

    }
}

interface BaseView<T> {

    var presenter: T
}

interface BasePresenter {

    fun start()

}