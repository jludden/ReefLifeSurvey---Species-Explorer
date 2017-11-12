package me.jludden.reeflifesurvey.SearchActivity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_search.*
import me.jludden.reeflifesurvey.model.SearchResult
import java.util.concurrent.TimeUnit
import kotlinx.android.synthetic.main.activity_search_results_fragment.*
import me.jludden.reeflifesurvey.R


/**
 * Created by Jason on 11/12/2017.
 */
class SearchFragment : Fragment(), SearchContract.View {

    override var isActive: Boolean = false
        get() = isAdded

    override lateinit var presenter: SearchContract.Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.activity_search_results_fragment, container, false)
        return root
    }

    override fun onResume() {
        super.onResume()
        presenter.start()
    }

    fun newInstance(): SearchFragment {
        return SearchFragment();
    }

    //todo not used yet
    internal var searchViewListener: SearchView.OnQueryTextListener = object : SearchView.OnQueryTextListener{
        /**
         * Called when the user submits the query. This could be due to a key press on the
         * keyboard or due to pressing a submit button.
         * The listener can override the standard behavior by returning true
         * to indicate that it has handled the submit request. Otherwise return false to
         * let the SearchView handle the submission by launching any associated intent.
         *
         * @param query the query text that is to be submitted
         *
         * @return true if the query has been handled by the listener, false to let the
         * SearchView perform the default action.
         */
        override fun onQueryTextSubmit(query: String?): Boolean {
            presenter.onQueryTextSubmit(query)
            return true
        }

        /**
         * Called when the query text is changed by the user.
         *
         * @param newText the new content of the query text field.
         *
         * @return false if the SearchView should perform the default action of showing any
         * suggestions if available, true if the action was handled by the listener.
         */
        override fun onQueryTextChange(newText: String?): Boolean {
            presenter.onQueryTextChange(newText)
            return true
        }

    }

    override fun setProgressIndicator(active: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addSearchResult(result: SearchResult) {
        Log.d("jludden.reeflifesurvey","searchfragment addSearchResult ")


        val curText = search_results_text.text
        val newResult = result.name + "_" + result.description

        val newText = "$curText \n $newResult"

        search_results_text.text = newText
    }

    //todo
    override fun clearSearchResults() {
        search_results_text.text = ""
    }

    companion object {
       const val TAG: String = "SearchResultsFragment"
    }

}