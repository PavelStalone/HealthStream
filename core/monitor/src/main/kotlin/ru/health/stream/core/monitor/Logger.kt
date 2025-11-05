package ru.health.stream.core.monitor

import android.util.Log
import timber.log.Timber

object Logger {

    inline fun logv(message: String, vararg arguments: Any?) =
        log(priority = Log.VERBOSE, message = message, throwable = null, arguments)

    inline fun logd(message: String, vararg arguments: Any?) =
        log(priority = Log.DEBUG, message = message, throwable = null, arguments)

    inline fun logi(message: String, vararg arguments: Any?) =
        log(priority = Log.INFO, message = message, throwable = null, arguments)

    inline fun logw(message: String, throwable: Throwable? = null, vararg arguments: Any?) =
        log(priority = Log.WARN, message = message, throwable = throwable, arguments)

    inline fun loge(message: String, throwable: Throwable? = null, vararg arguments: Any?) =
        log(priority = Log.ERROR, message = message, throwable = throwable, arguments)

    inline fun log(priority: Int, message: String, vararg arguments: Any?) =
        log(priority = priority, message = message, throwable = null, arguments)

    inline fun log(
        priority: Int,
        message: String,
        throwable: Throwable? = null,
        vararg arguments: Any?
    ) {
        Timber.log(priority, throwable, message, arguments)
    }
}
