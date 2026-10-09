package ru.health.stream.source.local.healthconnect

import kotlinx.coroutines.flow.flow
import ru.health.stream.core.common.permission.PermissionManager
import ru.health.stream.core.common.permission.PermissionStatus
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.source.local.KeyValueSource
import ru.health.stream.source.local.healthconnect.record.MeasurementSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectManager @Inject internal constructor(
    private val keyValueSource: KeyValueSource,
    private val permissionManager: PermissionManager,
    private val healthConnectProvider: HealthConnectProvider,
    private val measurementsSources: List<@JvmSuppressWildcards MeasurementSource<Measurement>>,
) {

    val isActiveHealthConnect = flow {
        if (isHealthConnectSupported()) {
            keyValueSource.observe<Boolean>(HEALTH_CONNECT_KEY).collect { emit(it) }
        } else {
            emit(false)
        }
    }

    suspend fun enableHealthConnect() {
        val result = permissionManager.requestGroup(
            *measurementsSources.map { it.readPermission }.toTypedArray(),
            *measurementsSources.map { it.writePermission }.toTypedArray()
        )

        if (result.any { (_, status) -> status == PermissionStatus.Granted }) {
            keyValueSource.saveValue(HEALTH_CONNECT_KEY, true)
        }
    }

    suspend fun disableHealthConnect() {
        keyValueSource.saveValue(HEALTH_CONNECT_KEY, false)
    }

    fun isHealthConnectSupported(): Boolean = runCatching {
        healthConnectProvider.value
    }.isSuccess

    companion object {

        val HEALTH_CONNECT_KEY = "HealthConnectKey"
    }
}
