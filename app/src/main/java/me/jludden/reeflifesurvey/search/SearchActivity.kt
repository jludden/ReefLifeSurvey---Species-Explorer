package me.jludden.reeflifesurvey.search

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.jludden.reeflifesurvey.R
import java.util.concurrent.TimeUnit

import kotlinx.android.synthetic.main.activity_search.*
import me.jludden.reeflifesurvey.data.DataRepository


/**
 * Created by Jason on 11/9/2017.
 *
 */
class SearchActivity : AppCompatActivity() {
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
                searchFragment
        )


        //todo im still handlling on stuff here when it should be in view/presenter
        searchback.setOnClickListener()
        {
            searchback.setBackground(null)
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


        Observable
                .create(ObservableOnSubscribe<String> { subscriber ->
                   search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                       override fun onQueryTextSubmit(query: String): Boolean {
                           searchPresenter.onQueryTextSubmit(query)
                           return true;
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

    }



    /**
     * show the keyboard
     */
    override fun onEnterAnimationComplete() {
        search_view.requestFocus()
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInputFromWindow(search_view.getWindowToken(),0,0);
    }
}