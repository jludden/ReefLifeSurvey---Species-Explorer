package me.jludden.reeflifesurvey.detailed

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.transition.Transition
import android.util.Log
import android.view.*
import android.widget.*
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.android.synthetic.main.activity_details_content.*
import me.jludden.reeflifesurvey.Injection
import me.jludden.reeflifesurvey.R
import me.jludden.reeflifesurvey.customviews.BaseDisplayableImage
import me.jludden.reeflifesurvey.customviews.BottomSheet
import me.jludden.reeflifesurvey.customviews.ImageDrawer
import me.jludden.reeflifesurvey.customviews.ImageDrawer.OnImageDrawerInteractionListener
import me.jludden.reeflifesurvey.data.*
import me.jludden.reeflifesurvey.data.utils.SharedPreferencesUtils.setUpFavoritesButton
import me.jludden.reeflifesurvey.data.DataSource.*
import me.jludden.reeflifesurvey.data.model.*
import me.jludden.reeflifesurvey.data.utils.SharedPreferencesUtils.FAVORITES_OUTLINE_WHITE
import me.jludden.reeflifesurvey.data.utils.StoredImageLoader
import me.jludden.reeflifesurvey.fullscreenquiz.ImagePagerAdapter

/**
 * Created by Jason on 11/19/2017.
 *
 * supporting postpone enter transition, but it can be very slow currently only postponing transition for fish species
 */
class DetailsActivity : AppCompatActivity(), OnImageDrawerInteractionListener {
    private lateinit var dataRepo: DataSource

    private var speciesCard: FishSpecies? = null
    private lateinit var favoriteBtn: CheckBox
    private val additionalImageURLs: MutableList<String> = ArrayList()
    private var isInitialLoad: Boolean = true
    private lateinit var viewAdapter: ImagePagerAdapter<BaseDisplayableImage>
    private lateinit var viewPager: ViewPager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_details)
        val toolbar = findViewById<View>(R.id.details_toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true) // show back button
        isInitialLoad = (savedInstanceState==null)
        var postpone = true //todo !!
        val b = intent.extras
        if(b != null && b.getBoolean(ARGS_NO_POSTPONE)) {
            postpone = false
        }
        Log.d(TAG, "details oncreate savedinstancestate-null?:${savedInstanceState==null}  postpone:$postpone ${b==null} ${b.getBoolean(ARGS_NO_POSTPONE)}")

        viewPager = findViewById<View>(R.id.main_image_pager) as ViewPager
        viewAdapter = ImagePagerAdapter(this, object: ImagePagerAdapter.ImagePagerAdapterListener{
            override fun onSuccess() { supportStartPostponedEnterTransition() }

            override fun onError() { supportStartPostponedEnterTransition() }

            override fun onLoadMoreRequested() { }
        })
        viewPager.adapter = viewAdapter

        dataRepo = Injection.provideDataRepository(applicationContext)

        if(intent.hasExtra(FishSpecies.INTENT_EXTRA)){
            val card = intent.getParcelableExtra<FishSpecies>(FishSpecies.INTENT_EXTRA)
            loadFishSpecies(card.id, postpone)
        }
        else if(intent.hasExtra(SearchResult.INTENT_EXTRA)) {
            val searchResult
                    = intent.getParcelableExtra<SearchResult>(SearchResult.INTENT_EXTRA)

            if(searchResult.type == SearchResultType.FishSpecies) {
                loadFishSpecies(searchResult.id, postpone)
            } else if (searchResult.type == SearchResultType.SurveySiteLocation){
                loadSurveySite(searchResult.id)
            }
        }
    }

    private fun loadSurveySite(code: String){
        details_fishspecies_scrollview.visibility = View.GONE
        //favoriteBtn.visibility = View.GONE
//        details_survey_site_parent.visibility = View.VISIBLE

        details_survey_site_parent.visibility = View.VISIBLE

        dataRepo.getSurveySites(SurveySiteType.CODES, object: LoadSurveySitesCallBack {
            override fun onSurveySitesLoaded(sites: SurveySiteList) {
                val siteList = sites.getSitesForCode(code)
                val site = siteList[1]

                supportActionBar?.title = site.ecoRegion
                details_survey_site_title.text = site.displayName
                details_survey_site_main.text = "${site.realm} [${site.position}]"
                details_survey_site_locations.text = BottomSheet.Companion.getCodeList(siteList)
            }
            override fun onDataNotAvailable(reason: String) {
                TODO("not implemented")
            }
        })
    }

    private fun loadFishSpecies(id: String, postpone: Boolean) {
        if(postpone) supportPostponeEnterTransition()

        if(isInitialLoad)
            //popping in the favorite button after the view loads for a little extra flair
            window.enterTransition.addListener(object : Transition.TransitionListener {
                override fun onTransitionEnd(transition: Transition) {
                    favoriteBtn.animate().alpha(1.0f)
                    window.enterTransition.removeListener(this)
                }
                override fun onTransitionResume(transition: Transition) { }
                override fun onTransitionPause(transition: Transition) { }
                override fun onTransitionCancel(transition: Transition) { }
                override fun onTransitionStart(transition: Transition) { }
        })

        dataRepo.getFishCard(id, object: LoadFishCardCallBack{
            override fun onFishCardLoaded(card: FishSpecies) {
                setupFishDetails(card)
            }

            override fun onDataNotAvailable(reason: String) { } //todo ;)
        })
    }

    fun setupFishDetails(card: FishSpecies) {

        val scientificNames = findViewById<TextView>(R.id.details_label_scientific)
        val commonNames = findViewById<TextView>(R.id.details_label_common)
        speciesCard = card
        scientificNames.text = card.scientificName
        commonNames.text = card.commonNames

        //set up toolbar
        supportActionBar?.title = card.scientificName

        val summaryText = StringBuilder(
            "Card Name " + card.scientificName + "\n" +
                "Common Names" + card.commonNames + "\n" +
                "Num sightings " + card.numSightings + "\n" +
                "Found in " + card.FoundInSites.size() + " sites" + "\n")

        val siteKeys = card.FoundInSites.keys()
        var site: SurveySiteList.SurveySite
        while (siteKeys.hasMoreElements())
        {
            site = siteKeys.nextElement()

            val numSightings = card.FoundInSites.get(site)
            summaryText.append("\n" + site.siteName + "\t" + numSightings)
        }

        if (card.imageURLs == null) {
            Log.d(TAG, "DetailsviewAdapter card details no images to load")
            summaryText.append("\n No Images Found")
            supportStartPostponedEnterTransition()
        }
        else {
            summaryText.append("Number of images: " + card.imageURLs.size + "\n")
            val imageDrawer = findViewById<ImageDrawer<BaseDisplayableImage>>(R.id.image_drawer)

            for (url in card.imageURLs) {
                val item = BaseDisplayableImage(url, card, null)
                imageDrawer.addItem(item)
                viewAdapter.addItem(item)

                additionalImageURLs.add(url) //todo ? do i need
            }
        }

        summaryText.append("\n").append("SPECIES PAGE URL: ").append(card.reefLifeSurveyURL)
        Log.d(TAG, summaryText.toString())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.details_toolbar, menu)

        favoriteBtn = menu.findItem(R.id.favorite_btn).actionView as CheckBox
        if(isInitialLoad) favoriteBtn.alpha = 0.0f //animate the button coming in, unless we in a config change etc

        if(speciesCard == null) {
            favoriteBtn.visibility = View.GONE
            favoriteBtn.alpha = 0.0f
        } else {
            setUpFavoritesButton(speciesCard, favoriteBtn, this, -1, FAVORITES_OUTLINE_WHITE)
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
             android.R.id.home -> {
                 onBackPressed()
                 return true
             }
            R.id.link_btn -> {
                val url = speciesCard?.reefLifeSurveyURL
                if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDrawerStateChanged(newState: Int) { }

    override fun onLoadMoreRequested() {  }

    override fun onImageClicked(item: BaseDisplayableImage, sharedElement: View) {
        val page = viewAdapter.findPageForItem(item)
        viewPager.currentItem = page
    }

    companion object {
        const val TAG: String = "DetailsActivity"
        const val REQUEST_CODE = 12345
        const val ARGS_NO_POSTPONE = "32"
    }
}
