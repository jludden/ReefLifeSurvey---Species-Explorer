package me.jludden.reeflifesurvey.SearchActivity

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.jludden.reeflifesurvey.R
import java.util.concurrent.TimeUnit

import kotlinx.android.synthetic.main.activity_search.*
import me.jludden.reeflifesurvey.model.DataRepository


/**
 * Created by Jason on 11/9/2017.
 *
 */
class SearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        //todo
        searchback.setOnClickListener(
                {
                    searchback.setBackground(null)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        finishAfterTransition()
                    } else{
                        finish()
                    }
                })

        //Create View
        val searchFragment = supportFragmentManager.findFragmentById(R.id.search_results_container)
            as SearchFragment? ?: SearchFragment().newInstance()

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.search_results_container, searchFragment, SearchFragment.TAG)
                .commit()

        //Create Presenter
        val searchPresenter = SearchPresenter(
                DataRepository.getInstance(applicationContext),
                searchFragment
        )

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