package me.jludden.reeflifesurvey.search

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import me.jludden.reeflifesurvey.R
import java.util.concurrent.TimeUnit

import kotlinx.android.synthetic.main.activity_search.*
import me.jludden.reeflifesurvey.data.DataRepository
import android.content.Context.INPUT_METHOD_SERVICE




/**
 * Created by Jason on 11/9/2017.
 *
 */
class SearchActivity : AppCompatActivity() {

    private val compositeSubscription = CompositeDisposable()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        //Create View
        val searchFragment = supportFragmentManager.findFragmentById(R.id.search_results_container)
                as SearchFragment? ?: SearchFragment.newInstance()

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.search_results_container, searchFragment, SearchFragment.TAG)
                .commit()

        //Create Presenter
        val searchPresenter = SearchPresenter(
                DataRepository.getInstance(applicationContext),
                compositeSubscription,
                searchFragment
        )


        //todo im still handlling on stuff here when it should be in view/presenter
        searchback.setOnClickListener {
            searchback.background = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAfterTransition()
            } else{
                finish()
            }
        }


        /*var searchViewListener: SearchView.OnQueryTextListener = object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                searchPresenter.onQueryTextSubmit(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchPresenter.onQueryTextChange(newText)
                return true
            }
        }

        search_view.setOnQueryTextListener(searchViewListener)*/


        compositeSubscription.add(
            Observable
                .create(ObservableOnSubscribe<String> { subscriber ->
                   search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                       override fun onQueryTextSubmit(query: String): Boolean {
                           //searchPresenter.onQueryTextSubmit(query)
                           hideKeyboard(search_view)
                           return true
                       }

                       override fun onQueryTextChange(query: String): Boolean {
                           subscriber.onNext(query)
                           return true
                       }
                   })
                })
                .debounce(1000, TimeUnit.MILLISECONDS )
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ query -> searchPresenter.onQueryTextChange(query)})
        )

    }

    fun showKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    fun hideKeyboard(v: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
    }

    override fun onDestroy() {
        compositeSubscription.clear()
        super.onDestroy()
    }

    override fun onEnterAnimationComplete() {
        search_view.requestFocus()
    }

    companion object {
        const val REQUEST_CODE = 1324
    }
}