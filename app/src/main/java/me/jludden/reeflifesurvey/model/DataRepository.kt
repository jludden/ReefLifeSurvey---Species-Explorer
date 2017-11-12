package me.jludden.reeflifesurvey.model

import android.content.Context

/**
 * Created by Jason on 11/11/2017.
 *
 * Singleton Data Source that should be accessible across activities and threads
 */
class DataRepository private constructor(context: Context) {

    companion object : SingletonHolder<DataRepository, Context>(::DataRepository)

    init {

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