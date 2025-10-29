package ru.health.stream.core.starter

import android.app.Application
import javax.inject.Inject
import javax.inject.Provider

abstract class StarterApplication : Application() {

    @Inject
    lateinit var starters: Set<@JvmSuppressWildcards AppStarter>

    override fun onCreate() {
        super.onCreate()

        starters.forEach { starter -> starter.onCreate() }
    }

    override fun onTerminate() {
        super.onTerminate()

        starters.forEach { starter -> starter.onTerminate() }
    }
}
