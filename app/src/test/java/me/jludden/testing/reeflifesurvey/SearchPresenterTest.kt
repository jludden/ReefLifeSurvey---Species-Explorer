package me.jludden.testing.reeflifesurvey

import io.reactivex.disposables.CompositeDisposable
import me.jludden.reeflifesurvey.data.DataRepository
import me.jludden.reeflifesurvey.data.DataSource
import me.jludden.reeflifesurvey.data.model.SearchResult
import me.jludden.reeflifesurvey.data.model.SearchResultType
import me.jludden.reeflifesurvey.search.SearchContract
import me.jludden.reeflifesurvey.search.SearchPresenter
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

/**
 * Created by Jason on 12/21/2017.
 *
 * Unit tests for the implementation of [SearchPresenter]
 */
class SearchPresenterTest {

    @Mock private lateinit var dataRepo: DataSource

    @Mock private lateinit var searchView: SearchContract.View

    private lateinit var searchPresenter: SearchPresenter

    private val compositeSubscription = CompositeDisposable()

    private lateinit var searchResults: MutableList<SearchResult>


    @Before fun setupSearchPresenter() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this)

        // Get a reference to the class under test
        searchPresenter = SearchPresenter(dataRepo, compositeSubscription, searchView)

        // The presenter won't update the view unless it's active.
        `when`(searchView.isActive).thenReturn(true)

        // creating three initial search results
        searchResults = arrayListOf(SearchResult("Title1", "Description1", SearchResultType.SurveySiteLocation, "1"),
                SearchResult("Title2", "Description2", SearchResultType.FishSpecies, "2"),
                SearchResult("Title3", "Description3", SearchResultType.FishSpecies, "3"))
    }

    @Test fun onItemClicked_ShowsDetailsUi() {
        //todo test survey site type as well
        val requestedSearchResult = SearchResult("DetailsRequested", "For this result", SearchResultType.FishSpecies, "123")

        searchPresenter.onItemClicked(requestedSearchResult)

        verify(searchView).launchResultDetails(requestedSearchResult)
    }

}