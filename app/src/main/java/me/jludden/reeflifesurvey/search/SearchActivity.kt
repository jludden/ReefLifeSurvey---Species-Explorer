package me.jludden.reeflifesurvey.search

import android.app.Activity
import android.app.SharedElementCallback
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v7.app.AppCompatActivity
import android.transition.Transition
import android.transition.TransitionSet
import android.util.Log
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
import me.jludden.reeflifesurvey.Injection
import me.jludden.reeflifesurvey.data.DataRepository
import me.jludden.reeflifesurvey.transitions.CircularReveal

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
        searchFragment.setupSearchChips(object: SearchFragment.SearchChipHandler{
            override fun onChipClicked(chipText: String) {
                search_view.setQuery(chipText, true)
            }
        })

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.search_results_container, searchFragment, SearchFragment.TAG)
                .commit()

        //Create Presenter
        val searchPresenter = SearchPresenter(
                dataRepository = Injection.provideDataRepository(applicationContext),
                compositeSubscription = compositeSubscription,
                searchView = searchFragment
        )

        //set up return button and searchbox
        searchback.setOnClickListener {
            searchback.background = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAfterTransition()
            } else{
                finish()
            }
        }

        compositeSubscription.add(
            Observable
                .create(ObservableOnSubscribe<String> { subscriber ->
                   search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                       override fun onQueryTextSubmit(query: String): Boolean {
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

        setupTransitions()
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

        //display search chips when loading completes
        (supportFragmentManager.findFragmentById(R.id.search_results_container)
                as SearchFragment).displayChips(true)
    }

    //credit Nick Butcher - Plaid app
    private fun setupTransitions() {
        // grab the position that the search icon transitions in *from*
        // & use it to configure the return transition
        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onSharedElementStart(
                    sharedElementNames: List<String>,
                    sharedElements: List<View>?,
                    sharedElementSnapshots: List<View>) {
                if (sharedElements != null && !sharedElements.isEmpty()) {
                    val searchIcon = sharedElements[0]
                    if (searchIcon.id != R.id.searchback) return
                    val centerX = (searchIcon.left + searchIcon.right) / 2
                    val hideResults = findTransition(
                            window.returnTransition as TransitionSet,
                            CircularReveal::class.java, R.id.search_results_container) as CircularReveal?
                    if (hideResults != null) {
                        hideResults.setCenter(Point(centerX, 0))
                    }
                }
            }
        })
    }

    //credit Nick Butcher - Plaid app
    fun findTransition(
            set: TransitionSet,
            clazz: Class<out Transition>,
            @IdRes targetId: Int): Transition? {
        for (i in 0 until set.transitionCount) {
            val transition = set.getTransitionAt(i)
            if (transition.javaClass == clazz) {
                if (transition.targetIds.contains(targetId)) {
                    return transition
                }
            }
            if (transition is TransitionSet) {
                val child = findTransition(transition, clazz, targetId)
                if (child != null) return child
            }
        }
        return null
    }

/*
    @VisibleForTesting
    fun getCountingIdlingResource(): IdlingResource {
        return EspressoIdlingResource.idlingResource
    }
*/

    companion object {
        const val REQUEST_CODE = 1324
    }
}