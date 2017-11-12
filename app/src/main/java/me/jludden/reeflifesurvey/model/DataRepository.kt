package me.jludden.reeflifesurvey.model

import android.content.Context
import io.reactivex.Observable
import me.jludden.reeflifesurvey.LoaderUtils
import org.json.JSONObject

/**
 * Created by Jason on 11/11/2017.
 *
 * Singleton Data Source that should be accessible across activities and threads
 */
class DataRepository private constructor(context: Context) {

    //Singleton that proves the getInstance() method
    companion object : SingletonHolder<DataRepository, Context>(::DataRepository)

    private var siteList : SurveySiteList

    init {

        //load survey site list immediately
        val surveySites = LoaderUtils.loadFishSurveySites(context)
        siteList = LoaderUtils.parseSurveySites(surveySites)
        siteList.loadFavoritedSites(context) //Load Saved Sites


    }

    //use Observable.from to emit items one at a time from a iterable
    fun getSurveySitesObservable(): Observable<SurveySiteList.SurveySite> {
        return Observable.fromIterable(siteList.SITE_CODE_LIST) //todo possibly want the full site_list
    }

    //interface for providing callbacks for accessing data
    //todo add onDataNotAvailable callbacks to each interface as well
    interface LoadSurveySitesCallBack {
        fun onSurveySitesLoaded(sites : SurveySiteList)
    }

    interface LoadFishSpeciesJSONCallBack {
        fun onFishSpeciesJSONLoaded(speciesJSON: JSONObject)
    }

    fun getSurveySites(callback: LoadSurveySitesCallBack) {
        callback.onSurveySitesLoaded(siteList)
    }

    fun getFishSpeciesJSON(callback: LoadFishSpeciesJSONCallBack) {
        //callback.onFishSpeciesJSONLoaded()
    }
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