package me.jludden.reeflifesurvey.InterfaceComponents

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.daimajia.slider.library.SliderLayout
import com.daimajia.slider.library.SliderTypes.BaseSliderView
import com.daimajia.slider.library.SliderTypes.TextSliderView
import me.jludden.reeflifesurvey.Data.DataRepository
import me.jludden.reeflifesurvey.Data.DataRepository.*
import me.jludden.reeflifesurvey.Data.InfoCard
import me.jludden.reeflifesurvey.Data.LoaderUtils
import me.jludden.reeflifesurvey.Data.SurveySiteList

import me.jludden.reeflifesurvey.R

/**
 * Created by Jason on 11/16/2017.
 */

class BottomSheet : LinearLayout, LoadSurveySitesCallBack, LoadFishCardCallBack {
    private lateinit var dataRepo: DataRepository
    private lateinit var imageSliders: SliderLayout
    private lateinit var interactionListener: OnBottomSheetInteractionListener

    constructor(context: Context) : super(context) {
        inflateViews(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        inflateViews(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        inflateViews(context)
    }

    private fun inflateViews(context: Context) {
        val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.bottom_sheet, this)

        if (context is OnBottomSheetInteractionListener) interactionListener = context else TODO()

        dataRepo = DataRepository.getInstance(context.applicationContext)
        imageSliders = findViewById(R.id.site_preview_carousel) as SliderLayout
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        Log.d(TAG, "onFinishInflate: ")
        //TODO setup onclick listeners, etc
    }

    /**
     * set up image sliders for this survey site
     * @param: site survey site to load images for
     */
    fun setupImageSliders(site: SurveySiteList.SurveySite) {
        imageSliders.removeAllSliders()
        LoaderUtils.loadSingleSite(site, dataRepo, this, 5);
    }

    override fun onFishCardLoaded(card: InfoCard.CardDetails) {
        val textSliderView: TextSliderView = createCarouselEntry(card);
        imageSliders.addSlider(textSliderView);
    }

    //creates a TextSliderView (a fish preview image, with description and onclick listener) that can be added to a SliderLayout (an image carousel of fish previews)
    private fun createCarouselEntry(card: InfoCard.CardDetails): TextSliderView {
        val name = card.cardName
        val textSliderView = TextSliderView(context)
        textSliderView
                .description(name)
                .image(card.primaryImageURL)
                .setScaleType(BaseSliderView.ScaleType.Fit)
                .setOnSliderClickListener {
                    interactionListener.onImageSliderClick(card, textSliderView.view)
                }
        return textSliderView
    }


    /**
     * function to dispose of any image sliders or anything else that might leak
     */
    fun clearView() {
        imageSliders.removeAllSliders()
        imageSliders.stopAutoCycle()
    }

    //todo fun restartView()

    override fun onSurveySitesLoaded(sites: SurveySiteList) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDataNotAvailable(reason: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }



    companion object {

        val TAG = "BottomSheet"
    }

    interface OnBottomSheetInteractionListener {
        fun onImageSliderClick(card: InfoCard.CardDetails, sharedElement: View)
    }

}