package me.jludden.reeflifesurvey

import android.content.Context
import me.jludden.reeflifesurvey.data.DataRepository
import me.jludden.reeflifesurvey.data.DataSource
import me.jludden.reeflifesurvey.data.FakeDataRepository

/**
 * Created by Jason on 12/21/2017.
 *
 * Enables injection of mock implementations of [DataRepository] at compile time.
 */
object Injection {

    @JvmStatic fun provideDataRepository(context: Context): DataSource {
        return FakeDataRepository.getInstance()
    }
}