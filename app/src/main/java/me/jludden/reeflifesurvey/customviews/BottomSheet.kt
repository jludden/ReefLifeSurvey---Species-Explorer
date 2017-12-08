package me.jludden.reeflifesurvey.customviews

import android.content.Context
import android.text.method.ScrollingMovementMethod
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.daimajia.slider.library.SliderLayout
import com.daimajia.slider.library.SliderTypes.BaseSliderView
import com.daimajia.slider.library.SliderTypes.TextSliderView
import me.jludden.reeflifesurvey.data.DataRepository.*
import kotlinx.android.synthetic.main.bottom_sheet.view.*
import me.jludden.reeflifesurvey.data.*
import me.jludden.reeflifesurvey.R
import me.jludden.reeflifesurvey.data.model.InfoCard
import me.jludden.reeflifesurvey.data.model.SurveySiteList
import me.jludden.reeflifesurvey.data.utils.LoaderUtils

/**
 * Created by Jason on 11/16/2017.
 */

class BottomSheet : LinearLayout, LoadSurveySitesCallBack, LoadFishCardCallBack {
    private lateinit var dataRepo: DataRepository
    private lateinit var imageSliders: SliderLayout
    private lateinit var interactionListener: OnBottomSheetInteractionListener

    private var surveySiteList : SurveySiteList? = null
    private var currentSite : SurveySiteList.SurveySite? = null

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
        imageSliders = findViewById(R.id.site_preview_carousel)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        Log.d(TAG, "onFinishInflate: ")
        //TODO setup onclick listeners, etc

        bottom_sheet_main_text.movementMethod = ScrollingMovementMethod()

    }

    /**
     * Public function to call to update the bottom sheet display
     *  this will trigger calls to the data repository
     *  when survey sites load the text can come in
     *  as fish come in they will be added to the image preview slider
     * @param: site survey site to load images for
     */
    fun loadNewSite(site: SurveySiteList.SurveySite) {
        currentSite = site
        if(surveySiteList == null) dataRepo.getSurveySites(SurveySiteType.CODES, this)
        else setupSiteDetails()

        imageSliders.removeAllSliders()
        LoaderUtils.loadSingleSiteSpeciesPreview(site, dataRepo, this, 5);
    }

    private fun setupSiteDetails() {
        val siteInfo = currentSite
        siteInfo ?: return
        surveySiteList ?: return

        val details = StringBuilder()
        val code = siteInfo.code
        var indivSites = surveySiteList?.getSitesForCode(code)
        details.append("Code (ID) : " + code + " (" + siteInfo.getID() + ")")
        details.append("\n SiteName " + siteInfo.getSiteName())
        details.append("\n EcoRegion " + siteInfo.getEcoRegion())
        details.append("\n Realm " + siteInfo.getRealm())
        details.append("\n Position: " + siteInfo.getPosition())
        details.append("\n Num Surveys " + siteInfo.getNumberOfSurveys())
        details.append("\n" + getCodeList(indivSites!!))

        bottom_sheet_main_text.text = details
        bottom_sheet_main_text.scrollTo(0, 0)

        bottom_sheet_number_surveys.text = resources.getString(
                R.string.bottom_sheet_total_surveys,
                getTotalSurveysForCode(indivSites),
                indivSites.size)

        //"${surveySiteList?.getTotalSurveysForCode(code)} Surveys"
    }

    //creates a TextSliderView (a fish preview image, with description and onclick listener) that can be added to a SliderLayout (an image carousel of fish previews)
    private fun addSliderImage(card: InfoCard.CardDetails) {
        val name = card.cardName
        val textSliderView = TextSliderView(context)
        textSliderView
                .description(name)
                .setScaleType(BaseSliderView.ScaleType.Fit)
                .setOnSliderClickListener {
                    interactionListener.onImageSliderClick(card, textSliderView.view)
                }

        if(card.primaryImageURL == "") textSliderView.image(R.drawable.ic_menu_camera)
        else textSliderView.image(card.primaryImageURL)

        imageSliders.addSlider(textSliderView);
    }

    /**
     * function to dispose of any image sliders or anything else that might leak
     */
    fun clearView() {
        imageSliders.removeAllSliders()
        imageSliders.stopAutoCycle()
    }

    //todo fun restartView()


    /**
     * TODO instead of chaining these callbacks, use Observable.flatmap to handle the dependency
     *
     */
    override fun onSurveySitesLoaded(sites: SurveySiteList) {
        surveySiteList = sites
        setupSiteDetails()
    }

    override fun onFishCardLoaded(card: InfoCard.CardDetails) {
        addSliderImage(card)
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

    //todo combine these loops
    //possibly destructured syntax https://kotlinlang.org/docs/reference/multi-declarations.html
    fun getCodeList(siteList: List<SurveySiteList.SurveySite>): String { //todo change len to charlen instead of numsites
        val nameBuilder = StringBuilder().append(siteList.size).append(": ")

        var iterCount = 0
        for (tSite in siteList) {
            nameBuilder.append(tSite.getSiteName() + ", ")
        }

        nameBuilder.delete(nameBuilder.length - 2, nameBuilder.length) //remove trailing comma

        return nameBuilder.toString()
    }

    fun getTotalSurveysForCode(siteList: List<SurveySiteList.SurveySite>): Int {
        var numSurveys = 0
        for (site in siteList) {
            numSurveys += site.getNumberOfSurveys()
        }
        return numSurveys
    }


}