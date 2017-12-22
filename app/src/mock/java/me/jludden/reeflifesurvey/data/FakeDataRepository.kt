package me.jludden.reeflifesurvey.data

import android.support.annotation.VisibleForTesting
import io.reactivex.Observable
import me.jludden.reeflifesurvey.data.model.InfoCard
import me.jludden.reeflifesurvey.data.model.SurveySiteList

/**
 * Created by Jason on 12/21/2017.
 */

class FakeDataRepository private constructor() : DataSource {

    private val FISH_SPECIES = LinkedHashMap<String, InfoCard.CardDetails>()
    private val SURVEY_SITES = LinkedHashMap<String, SurveySiteList.SurveySite>()

    override fun getSurveySitesAll(type: SurveySiteType): Observable<SurveySiteList.SurveySite> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getFishSpeciesAll(): Observable<InfoCard.CardDetails> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getFishSpeciesForSite(site: SurveySiteList.SurveySite): Observable<InfoCard.CardDetails> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSurveySites(type: SurveySiteType, callback: DataSource.LoadSurveySitesCallBack) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getFishSpeciesJSON(callback: DataSource.LoadFishSpeciesJSONCallBack) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getFishCard(id: String, callback: DataSource.LoadFishCardCallBack) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @VisibleForTesting fun addFishSpecies(vararg species: InfoCard.CardDetails) {
        for (fish in species) {
            FISH_SPECIES.put(fish.id, fish)
        }
    }

    @VisibleForTesting fun addSurveySites(vararg sites: SurveySiteList.SurveySite){
        for (site in sites) {
            SURVEY_SITES.put(site.code+site.id, site)
        }
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