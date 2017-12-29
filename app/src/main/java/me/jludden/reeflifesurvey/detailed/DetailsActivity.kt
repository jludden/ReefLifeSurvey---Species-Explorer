package me.jludden.reeflifesurvey.detailed

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.transition.Transition
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.android.synthetic.main.details_view_pager_item.*
import me.jludden.reeflifesurvey.Injection
import me.jludden.reeflifesurvey.R
import me.jludden.reeflifesurvey.customviews.BottomSheet
import me.jludden.reeflifesurvey.data.*
import me.jludden.reeflifesurvey.data.utils.SharedPreferencesUtils.setUpFavoritesButton
import me.jludden.reeflifesurvey.data.DataSource.*
import me.jludden.reeflifesurvey.data.model.*
import me.jludden.reeflifesurvey.data.utils.SharedPreferencesUtils.FAVORITES_OUTLINE_WHITE
import me.jludden.reeflifesurvey.data.utils.StoredImageLoader

/**
 * Created by Jason on 11/19/2017.
 */
class DetailsActivity : AppCompatActivity() {
    private lateinit var dataRepo: DataSource
    private var speciesCard: FishSpecies? = null
    private lateinit var favoriteBtn: CheckBox

    private lateinit var storedImageLoader: StoredImageLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_details)

        //supporting postpone enter transition, but it can be very slow
        //currently only postponing transition for fish species
        // supportPostponeEnterTransition() //postpone transition until the image is loaded

        supportActionBar!!.setDisplayHomeAsUpEnabled(true) // show back button
        dataRepo = Injection.provideDataRepository(applicationContext)
        storedImageLoader = StoredImageLoader(applicationContext)

        if(intent.hasExtra(FishSpecies.INTENT_EXTRA)){
            val card = intent.getParcelableExtra<FishSpecies>(FishSpecies.INTENT_EXTRA)
            loadFishSpecies(card.id)
        }
        else if(intent.hasExtra(SearchResult.INTENT_EXTRA)) {
            val searchResult
                    = intent.getParcelableExtra<SearchResult>(SearchResult.INTENT_EXTRA)

            if(searchResult.type == SearchResultType.FishSpecies) {
                loadFishSpecies(searchResult.id)
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

    private fun loadFishSpecies(id: String) {
        supportPostponeEnterTransition()

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

            override fun onDataNotAvailable(reason: String) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }

    fun setupFishDetails(card: FishSpecies) {
        val mainImageView = findViewById<ImageView>(R.id.details_image_main)
//        val textView = findViewById<TextView>(R.id.details_text)
       // val linkBtn = findViewById<Button>(R.id.link_btn)
        val scientificNames = findViewById<TextView>(R.id.details_label_scientific)
        val commonNames = findViewById<TextView>(R.id.details_label_common)

        speciesCard = card
        scientificNames.text = card.cardName
        commonNames.text = card.commonNames

        //set up favorites button
        //  val favoriteBtn = findViewById<CheckBox>(R.id.favorite_btn)
        //setUpFavoritesButton(card, favoriteBtn, this)

        //set up toolbar
        supportActionBar?.title = card.cardName
        /*val toolbarView = findViewById<View>(R.id.app_bar) as AppBarLayout


        findViewById<ImageButton>(R.id.favorite_btn)*/
//        android.support.v7.widget.AppCompatCheckBox
//        android.widget.CheckBox

        val newText = StringBuilder(
            "Card Name " + card.cardName + "\n" +
                "Common Names" + card.commonNames + "\n" +
                "Num sightings " + card.numSightings + "\n" +
                "Found in " + card.FoundInSites.size() + " sites" + "\n")

        val siteKeys = card.FoundInSites.keys()
        var site: SurveySiteList.SurveySite
        while (siteKeys.hasMoreElements())
        {
            site = siteKeys.nextElement()

            val numSightings = card.FoundInSites.get(site)
            newText.append("\n" + site.siteName + "\t" + numSightings)
        }

        val loadedSuccessfully = card.tryLoadPrimaryImageOffline(storedImageLoader, mainImageView)
        if(!loadedSuccessfully && (card.imageURLs != null && card.imageURLs.size >= 1)) {
            mainImageView.loadURL(card.imageURLs.get(0))
        } else {
            supportStartPostponedEnterTransition()
        }

        val additionalImages = findViewById<LinearLayout>(R.id.details_additional_images)
        if (card.imageURLs == null)
        {
            Log.d(TAG, "DetailsviewAdapter card details no images to load")
            newText.append("\n No Images Found")
        }
        else
        {
            newText.append("Number of images: " + card.imageURLs.size + "\n")
            for (url in card.imageURLs) {
                val iv = ImageView(this)
                iv.layoutParams = LinearLayout.LayoutParams(250, 250)

                //todo really shouldnt make a new one for each item
                iv.setOnClickListener {
                    Picasso.with(this)
                            .load(url)
                            .placeholder(mainImageView.drawable) //placeholder = current image, to minimize gap
                            .into(mainImageView)
                }

                Picasso.with(this)
                    .load(url)
                    .placeholder(R.drawable.ic_menu_camera)
                    .into(iv)
                iv.setPadding(0, 0, 0, 5)
                additionalImages.addView(iv)
            }
        }


        newText.append("\n").append("SPECIES PAGE URL: ").append(card.reefLifeSurveyURL)

       // textView.setText(newText.toString())

        Log.d(TAG,newText.toString())

        /*linkBtn.setOnClickListener {
            val url = card.reefLifeSurveyURL
            if (url.startsWith("http://") || url.startsWith("https://")) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }
        }*/

        }

    fun ImageView.loadURL(url: String) {
            Picasso.with(context)
            .load(url)
            .error(R.drawable.ic_menu_camera)
            .into(this, object: Callback {
                override fun onSuccess() {
                    supportStartPostponedEnterTransition()
                }

                override fun onError() {
                    supportStartPostponedEnterTransition()
                }
            })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.details_toolbar, menu)

        favoriteBtn = menu.findItem(R.id.favorite_btn).actionView as CheckBox
        favoriteBtn.alpha = 0.0f

        if(speciesCard == null) {
            favoriteBtn.visibility = View.GONE
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

    companion object {
        const val TAG: String = "DetailsActivity"
        const val REQUEST_CODE = 12345
    }
}