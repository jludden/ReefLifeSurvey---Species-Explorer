package me.jludden.reeflifesurvey

import android.content.Context
import me.jludden.reeflifesurvey.data.DataRepository
import me.jludden.reeflifesurvey.data.DataSource

/**
 * Created by Jason on 12/21/2017.
 *
 * Enables injection of production implementations of [DataRepository] at compile time
 */

object Injection {

    @JvmStatic fun provideDataRepository(context: Context) : DataSource =
            DataRepository.getInstance(context)
}
