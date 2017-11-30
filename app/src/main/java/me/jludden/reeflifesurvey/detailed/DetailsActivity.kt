package me.jludden.reeflifesurvey.detailed

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.squareup.picasso.Picasso
import me.jludden.reeflifesurvey.R
import me.jludden.reeflifesurvey.customviews.BottomSheet
import me.jludden.reeflifesurvey.data.*
import me.jludden.reeflifesurvey.data.SharedPreferencesUtils.setUpFavoritesButton

/**
 * Created by Jason on 11/19/2017.
 */
class DetailsActivity : AppCompatActivity(), BottomSheet.OnBottomSheetInteractionListener {
    private lateinit var dataRepo: DataRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_details)

        //todo set support postpone enter transition, but it can be very slow
        //supportPostponeEnterTransition() //postpone transition until the image is loaded
        dataRepo = DataRepository.getInstance(applicationContext)


        if(intent.hasExtra(SearchResult.INTENT_EXTRA)) {
            val searchResult
                    = intent.getParcelableExtra<SearchResult>(SearchResult.INTENT_EXTRA)

            if(searchResult.type == SearchResultType.FishSpecies) {

                dataRepo.getFishCard(searchResult.id, object: DataRepository.LoadFishCardCallBack{
                    override fun onFishCardLoaded(card: InfoCard.CardDetails) {
                        setupFishDetails(card)
                    }

                    override fun onDataNotAvailable(reason: String) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }
                })


            } else if (searchResult.type == SearchResultType.SurveySiteLocation){

            }
        }


        supportActionBar!!.setDisplayHomeAsUpEnabled(true) // show back button

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.getItemId()
        if (id == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    fun setupFishDetails(card: InfoCard.CardDetails) {
        val mainImageView = findViewById(R.id.details_image_main) as ImageView
        val textView = findViewById(R.id.details_text) as TextView
        val favoriteBtn = findViewById(R.id.favorite_btn) as CheckBox //star to favorite the fish
        val linkBtn = findViewById(R.id.link_btn) as ImageButton


        //set up favorites star button
        setUpFavoritesButton(card, favoriteBtn, this)


       /* glide
            .load(card.imageURL)
            .into(mainImageView);*/

        val newText = StringBuilder(
                card.cardName + "\n" +
                card.commonNames + "\n" +
                "Num sightings " + card.numSightings + "\n" +
                "Found in " + card.FoundInSites.size() + " sites" + "\n" +
                "Number images: " + card.imageURLs.size + "\n")

        val siteKeys = card.FoundInSites.keys()
        var site: SurveySiteList.SurveySite
        while (siteKeys.hasMoreElements())
        {
            site = siteKeys.nextElement()

            val numSightings = card.FoundInSites.get(site)
            newText.append("\n" + site.siteName + "\t" + numSightings)
        }

        if (card.imageURLs != null && card.imageURLs.size >= 1)
        {
            mainImageView.loadURL(card.imageURLs.get(0))
        }


        val additionalImages = findViewById(R.id.details_additional_images) as LinearLayout
        if (card.imageURLs == null)
        {
            Log.d(TAG, "DetailsviewAdapter card details no images to load")
            newText.append("\n No Images Found")
        }
        else
        {
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

        textView.setText(newText.toString())

        linkBtn.setOnClickListener {
            val url = card.reefLifeSurveyURL
            if (url.startsWith("http://") || url.startsWith("https://")) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }
        }

        }

    fun ImageView.loadURL(url: String) {
        Glide.with(context)
                .load(url)
                .listener(object: RequestListener<Drawable>{
                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        supportStartPostponedEnterTransition()
                        return false
                    }

                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        supportStartPostponedEnterTransition()
                        return false
                    }

                })
                .into(this)
                .onLoadFailed(getDrawable(R.drawable.ic_menu_camera))


        /*    Picasso.with(context)
            .load(url)
            .error(R.drawable.ic_menu_camera)
            .into(this)*/

        /*    Glide.with(context)
                    .load(url)
                    .transition(withCrossFade())
                    .into(this)*/


    }
    override fun onImageSliderClick(card: InfoCard.CardDetails, sharedElement: View) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    companion object {
        const val TAG: String = "DetailsActivity"
    }
}