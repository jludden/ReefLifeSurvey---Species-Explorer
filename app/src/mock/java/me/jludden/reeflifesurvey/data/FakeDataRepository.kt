package me.jludden.reeflifesurvey.data

import android.content.Context
import android.support.annotation.VisibleForTesting
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Observable
import me.jludden.reeflifesurvey.data.model.FishSpecies
import me.jludden.reeflifesurvey.data.model.SurveySiteList
import me.jludden.reeflifesurvey.data.model.SurveySiteList.SurveySite
import org.json.JSONObject

/**
 * Created by Jason on 12/21/2017.
 */

class FakeDataRepository private constructor() : DataSource {

    private val FISH_SPECIES = LinkedHashMap<String, FishSpecies>()

    private val SITE_LIST = SurveySiteList()

    init {





    }


    override fun getSurveySitesAll(type: SurveySiteType): Observable<SurveySiteList.SurveySite> {
        return Observable.fromIterable(SITE_LIST.SITE_CODE_LIST)
    }

    override fun getFishSpeciesAll(): Observable<FishSpecies> {
        return Observable.fromIterable(FISH_SPECIES.values)
    }

    override fun getFishSpeciesForSite(site: SurveySiteList.SurveySite): Observable<FishSpecies> {
        return Observable.fromIterable(FISH_SPECIES.values)
    }

    override fun getSurveySites(type: SurveySiteType, callback: DataSource.LoadSurveySitesCallBack) {
        callback.onSurveySitesLoaded(SITE_LIST)
    }

    override fun getFishCard(id: String, callback: DataSource.LoadFishCardCallBack) {
        val ret = FISH_SPECIES["-1"]
        if(ret != null){
            callback.onFishCardLoaded(ret)
        } else {
            callback.onDataNotAvailable("Couldn't make a fishcard in mock")
        }
    }

    @VisibleForTesting fun addFishSpecies(vararg species: FishSpecies) {
        for (fish in species) {
            FISH_SPECIES.put(fish.id, fish)
        }
    }

    @VisibleForTesting fun addFavoriteSites(context: Context, vararg sites: SurveySite){
        for (site in sites) {
            SITE_LIST.add(site)
            SITE_LIST.addFavoriteSite(site.code, context)
        }
    }

    @VisibleForTesting fun createSimpleTestSite1(): SurveySite {
        val fakeSite = SurveySite("abc123")
        fakeSite.position = LatLng(-34.0, 151.0)
        fakeSite.ecoRegion = "Fake Site Sydney"
        fakeSite.siteName = "survey site 1"
        fakeSite.realm = "fake realm 1"
        fakeSite.speciesFound = JSONObject().accumulate("-1",1) //links to fake fish #1
        return fakeSite
    }

    @VisibleForTesting fun createSimpleFishSpecies1(): FishSpecies {
        val fakeSpecies = FishSpecies("-1")
        fakeSpecies.scientificName = "Fake Species 1"
        fakeSpecies.commonNames = "Common Name 1, Common Name 2, @#$@#$*&112 bunch of characters!./.,,/.,asdfjoij``2~=-0)(_+*($%^&$%^&@#$%()*"
        val urls = ArrayList<String>()
        urls.add("https://images.reeflifesurvey.com/0/species_bc_570467fe7625f.w1300.h866.jpg")
        urls.add("https://images.reeflifesurvey.com/0/species_e0_5769fd811425b.w1300.h866.jpg")
        urls.add("https://images.reeflifesurvey.com/0/species_01_570467f734e9e.w1300.h866.jpg")
        fakeSpecies.imageURLs = urls
        return fakeSpecies
    }

    companion object {

        private lateinit var INSTANCE: FakeDataRepository
        private var needsNewInstance = true

        @JvmStatic fun getInstance(): FakeDataRepository {
            if (needsNewInstance) {
                INSTANCE = FakeDataRepository()
                needsNewInstance = false
            }
            return INSTANCE
        }
    }
}