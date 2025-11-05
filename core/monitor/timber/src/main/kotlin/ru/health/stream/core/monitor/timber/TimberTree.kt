package ru.health.stream.core.monitor.timber

import android.util.Log
import timber.log.Timber

class TimberTree: Timber.DebugTree() {

    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?
    ) {
        Log.println(priority, tag, message)
    }
}
