package ru.health.stream.core.monitor

import android.util.Log
import timber.log.Timber

inline fun logV(message: String, vararg arguments: Any?) =
    log(priority = Log.VERBOSE, message = message, throwable = null, arguments)

inline fun logD(message: String, vararg arguments: Any?) =
    log(priority = Log.DEBUG, message = message, throwable = null, arguments)

inline fun logI(message: String, vararg arguments: Any?) =
    log(priority = Log.INFO, message = message, throwable = null, arguments)

inline fun logW(message: String, throwable: Throwable? = null, vararg arguments: Any?) =
    log(priority = Log.WARN, message = message, throwable = throwable, arguments)

inline fun logE(throwable: Throwable? = null, message: String, vararg arguments: Any?) =
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
