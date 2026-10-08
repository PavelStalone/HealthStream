package ru.health.stream.source.local.healthconnect

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    val value by lazy { HealthConnectClient.getOrCreate(context) }
}
