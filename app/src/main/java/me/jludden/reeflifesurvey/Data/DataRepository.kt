package me.jludden.reeflifesurvey.Data

import android.content.Context
import android.util.Log
import io.reactivex.Observable
import me.jludden.reeflifesurvey.Data.InfoCardLoader.parseSpeciesDetailsHelper
import me.jludden.reeflifesurvey.R
import me.jludden.reeflifesurvey.Data.InfoCard.CardDetails
import org.json.JSONObject
import kotlin.collections.HashMap
import me.jludden.reeflifesurvey.Data.SurveySiteType.*


/**
 * Created by Jason on 11/11/2017.
 *
 * Singleton Data Source that should be accessible across activities and threads
 */
class DataRepository private constructor(context: Context) {

    //Singleton that proves the getInstance() method
    companion object : SingletonHolder<DataRepository, Context>(::DataRepository)

    private var siteList : SurveySiteList
   // private val speciesString : String
    //private var speciesJSON : JSONObject
    //private val fishSpecies : Observable<InfoCard.CardDetails>
  //  private val fishStringStream : Observable<String>
    private val speciesCards: HashMap<String, CardDetails> //todo possibly sorted list better

    init {

        //load survey site list immediately
        val surveySites = LoaderUtils.loadFishSurveySites(context)
        siteList = LoaderUtils.parseSurveySites(surveySites)
        siteList.loadFavoritedSites(context) //Load Saved Sites

     //   speciesString = LoaderUtils.loadStringFromDisk(R.raw.api_species, context)
//        speciesJSON = JSONObject(LoaderUtils.loadStringFromDisk(R.raw.api_species, context))
       // fishSpecies = loadFromDisk(R.raw.api_species, context)
   //     fishStringStream = loadFromDisk(R.raw.api_species, context)

        speciesCards = loadFromDiskSync(R.raw.api_species, context)
    }

    //use Observable.from to emit items one at a time from a iterable
    fun getSurveySitesObservable(type: SurveySiteType = CODES): Observable<SurveySiteList.SurveySite> {
        if(type != CODES) TODO()
        return Observable.fromIterable(siteList.SITE_CODE_LIST)
    }

    //todo improve memory usage
    // upon init just start the inputstream:  InputStream is = context.getResources().openRawResource(id);
    // https://stackoverflow.com/questions/43442480/rxjava-read-file-to-observable
    //
    //consider wrapping this in an async call (separate rxjava-async module):
    //https://github.com/ReactiveX/RxJava/wiki/Async-Operators
    fun getFishSpeciesObservable(): Observable<CardDetails> {
        return Observable.fromIterable(speciesCards.values)
    }

    //NOT asynchronously load the data but oh well at least its already on disk right todo delete and do it better
    fun loadFromDiskSync(resID: Int, context: Context): HashMap<String, CardDetails> {
        val speciesJSON = JSONObject(LoaderUtils.loadStringFromDisk(R.raw.api_species, context))
        val iter = speciesJSON.keys()

        val species = HashMap<String, CardDetails>()

        while (iter.hasNext()){
            val curKey = iter.next()
            val speciesData = speciesJSON.getJSONArray(curKey)

            val cardDetails = CardDetails(curKey)
            species.put(curKey, parseSpeciesDetailsHelper(cardDetails, speciesData))
        }
        Log.d("DataRepository", "loaded fish species: "+species.size)

        return species
    }

    /*
    fun getFishSpeciesObservable(context: Context): Observable<CardDetails> {

        fishStringStream
                .map { str -> JSONObject(str)}
                .flatMap { json -> Observable.range(1,3) }
                .flatMap {
                    json -> ObservableOnSubscribe {

                    subscriber.onNext("1")

                }
                }

        Observable.create<> { ObservableOnSubscribe<> {
           result : String =
            subscriber.onNext(result)
        } }

        ObservableFrom


//        fun JSONObject.iterable(): MutableIterator<String> {
//            return this.keys()
//        }
//
//        val keys : MutableIterator<String> = speciesJSON.keys()
//                .fromIterable(speciesJSON)
        //.fromIterable<String>(speciesJSON.keys())

        //Flowable.

      /*
        Observable.create<JSONArray> {
            ObservableOnSubscribe<String> { subscriber ->

            }
        }
        .create(ObservableOnSubscribe<String> { subscriber ->
            search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    //  searchFor(query)
                }

                override fun onQueryTextChange(query: String): Boolean {
                    if (TextUtils.isEmpty(query)) {
                        //        clearResults()
                    }
                    subscriber.onNext(query)
                    return true
                }
            })
*/
        /*return Observable
                .fromCallable<MutableIterator<String>>({speciesJSON.keys()})
                .flatMap { iter ->

                }
                .map<JSONArray>{ key -> speciesJSON.getJSONArray(key) }
                .map<CardDetails>{ speciesData -> parseSpeciesDetailsHelper(CardDetails("1"), speciesData)}*/
    }
        */

    //interface for providing callbacks for accessing data
    //todo add onDataNotAvailable callbacks to each interface as well
    interface LoadSurveySitesCallBack : DataLoadCallback {
        fun onSurveySitesLoaded(sites : SurveySiteList)
    }

    interface LoadFishSpeciesJSONCallBack : DataLoadCallback {
        fun onFishSpeciesJSONLoaded(speciesJSON: JSONObject)
    }

    interface LoadFishCardCallBack : DataLoadCallback {
        fun onFishCardLoaded(card: CardDetails)
    }

    fun getSurveySites(type: SurveySiteType, callback: LoadSurveySitesCallBack) {
 /*       if(type == ALL_IDS) TODO()
        else if(type == CODES) {
            callback.onSurveySitesLoaded(siteList.SITE_CODE_LIST)
        } "fail"
*/
        callback.onSurveySitesLoaded(siteList)
    }

    fun getFishSpeciesJSON(callback: LoadFishSpeciesJSONCallBack) {
        //callback.onFishSpeciesJSONLoaded()
    }

    fun getFishCard(id: String, callback: LoadFishCardCallBack){
        val card: CardDetails? = speciesCards.get(id)
        if(card != null) callback.onFishCardLoaded(card)
        else callback.onDataNotAvailable(id)
    }
}

interface DataLoadCallback {
    fun onDataNotAvailable(reason: String)
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