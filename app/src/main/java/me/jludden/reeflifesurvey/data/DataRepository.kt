package me.jludden.reeflifesurvey.data

import android.content.Context
import android.util.Log
import io.reactivex.Observable
import me.jludden.reeflifesurvey.data.InfoCardLoader.parseSpeciesDetailsHelper
import me.jludden.reeflifesurvey.R
import me.jludden.reeflifesurvey.data.model.FishSpecies
import org.json.JSONObject
import kotlin.collections.HashMap
import me.jludden.reeflifesurvey.data.SurveySiteType.*
import me.jludden.reeflifesurvey.data.model.SurveySiteList
import me.jludden.reeflifesurvey.data.utils.LoaderUtils
import java.io.InputStream
import java.util.ArrayList


/**
 * Created by Jason on 11/11/2017.
 *
 * Singleton Data Source that should be accessible across activities and threads
 */
class DataRepository private constructor(context: Context) : DataSource {

    //Singleton that provides the getInstance() method
    companion object : SingletonHolder<DataRepository, Context>(::DataRepository)

    private val siteList : SurveySiteList
    // private val speciesString : String
    //private var speciesJSON : JSONObject
    //private val fishSpecies : Observable<InfoCard.FishSpecies>
    //  private val fishStringStream : Observable<String>
    private var allSpeciesLoaded: Boolean = false
    private val speciesStream: InputStream

    //todo possibly sorted list better
    private var speciesCards: HashMap<String, FishSpecies>


    init {

        //load survey site list immediately
        val surveySites = LoaderUtils.loadFishSurveySites(context)
        siteList = LoaderUtils.parseSurveySites(surveySites)
        siteList.loadFavoritedSites(context) //Load Saved Sites

     //   speciesString = LoaderUtils.loadStringFromDisk(R.raw.api_species, context)
//        speciesJSON = JSONObject(LoaderUtils.loadStringFromDisk(R.raw.api_species, context))
       // fishSpecies = loadFromDisk(R.raw.api_species, context)
   //     fishStringStream = loadFromDisk(R.raw.api_species, context)

        //speciesCards = loadFromDiskBlocking(R.raw.api_species, context)
        speciesStream = context.resources.openRawResource(R.raw.api_species)
        speciesCards = loadFromDiskBlocking()
    }

    //use Observable.from to emit items one at a time from a iterable
    override fun getSurveySitesAll(type: SurveySiteType): Observable<SurveySiteList.SurveySite> {
        if(type != CODES) TODO()
        return Observable.fromIterable(siteList.SITE_CODE_LIST)
    }

    //todo improve memory usage
    // upon init just start the inputstream:  InputStream is = context.getResources().openRawResource(id);
    // https://stackoverflow.com/questions/43442480/rxjava-read-file-to-observable
    //
    //consider wrapping this in an async call (separate rxjava-async module):
    //https://github.com/ReactiveX/RxJava/wiki/Async-Operators
    //
    //the blocking method can be called with observable.just
    //and it can be defered with observable.defer until it is actually subscribed to:
    //Observable.defer(() -> Observable.just(slowBlockingMethod()))
    //
    override fun getFishSpeciesAll(): Observable<FishSpecies> {
        /*if(!allSpeciesLoaded){
            speciesCards = loadFromDiskBlocking()
        }*/
        return Observable.fromIterable(speciesCards.values)
    }

    override fun getFishSpeciesForSite(site: SurveySiteList.SurveySite): Observable<FishSpecies>
    {
        val speciesList = site.getSpeciesFound().keys()
        val speciesIDs = ArrayList<String>()

        //add each species to the list of ids
        while (speciesList.hasNext()) {
            speciesIDs.add(speciesList.next())
        }

        //map the id to the card
        return Observable.fromIterable(speciesIDs).map { speciesCards[it] }
    }

    //block the main thread and load the data but oh well at least its already on disk right todo delete and do it better
    private fun loadFromDiskBlocking(): HashMap<String, FishSpecies> {
        val speciesJSON = JSONObject(LoaderUtils.loadStringFromDiskHelper(speciesStream))
        val iter = speciesJSON.keys()

        val species = HashMap<String, FishSpecies>()

        while (iter.hasNext()){
            val curKey = iter.next()
            val speciesData = speciesJSON.getJSONArray(curKey)

            val cardDetails = FishSpecies(curKey)
            species.put(curKey, parseSpeciesDetailsHelper(cardDetails, speciesData))
        }
        Log.d("DataRepository", "loaded fish species: "+species.size)

        allSpeciesLoaded = true
        return species
    }

    override fun getSurveySites(type: SurveySiteType, callback: DataSource.LoadSurveySitesCallBack) {
 /*       if(type == ALL_IDS) TODO()
        else if(type == CODES) {
            callback.onSurveySitesLoaded(siteList.SITE_CODE_LIST)
        } "fail"
*/
        callback.onSurveySitesLoaded(siteList)
    }

    override fun getFishCard(id: String, callback: DataSource.LoadFishCardCallBack){
        val card: FishSpecies? = speciesCards[id]
        if(card != null) callback.onFishCardLoaded(card)
        else callback.onDataNotAvailable(id)
    }
}

interface DataSource {

    fun getSurveySitesAll(type: SurveySiteType = SurveySiteType.CODES) : Observable<SurveySiteList.SurveySite>

    fun getFishSpeciesAll() : Observable<FishSpecies>

    fun getFishSpeciesForSite(site: SurveySiteList.SurveySite) : Observable<FishSpecies>

    fun getSurveySites(type: SurveySiteType, callback: LoadSurveySitesCallBack)

    fun getFishCard(id: String, callback: LoadFishCardCallBack)

    //interface for providing callbacks for accessing data
    interface LoadSurveySitesCallBack : DataLoadCallback {
        fun onSurveySitesLoaded(sites : SurveySiteList)
    }

    interface LoadFishCardCallBack : DataLoadCallback {
        fun onFishCardLoaded(card: FishSpecies)
    }

    interface DataLoadCallback {
        fun onDataNotAvailable(reason: String)
    }

}


enum class SurveySiteType {
    ALL_IDS,    //load all sites (you probably just want Codes)
    CODES, //load only one site per code (usually more useful than having a bunch of markers at the same spot)
    FAVORITES //load only favorite sites
}


//SingletonHolder implementing a double-checked locking algorithm
open class SingletonHolder<out T, in A>(creator: (A) -> T) {
    private var creator: ((A) -> T)? = creator
    @Volatile private var instance: T? = null

    fun getInstance(arg: A): T {
        val i = instance
        if (i != null) {
            return i
        }

        return synchronized(this) {
            val i2 = instance
            if (i2 != null) {
                i2
            } else {
                val created = creator!!(arg)
                instance = created
                creator = null
                created
            }
        }
    }
}