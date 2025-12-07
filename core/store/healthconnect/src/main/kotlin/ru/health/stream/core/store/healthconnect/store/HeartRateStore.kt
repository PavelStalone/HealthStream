package ru.health.stream.core.store.healthconnect.store

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import kotlinx.datetime.Instant
import ru.health.stream.core.store.heartrate.HeartRateStore
import ru.health.stream.feature.vitals.source.local.model.HeartRate
import javax.inject.Inject
import kotlin.time.Duration

class HeartRateStore @Inject constructor(
    private val healthConnectClient: HealthConnectClient
) : HeartRateStore {

    private val PERMISSIONS = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getWritePermission(HeartRateRecord::class),
    )

    override suspend fun isActive(): Boolean {
        val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()

        return grantedPermissions.containsAll(PERMISSIONS)
    }

    override suspend fun getHeartRateByRange(
        from: Instant,
        to: Instant,
    ): List<HeartRate> {
        TODO("Not yet implemented")
    }

    override suspend fun getHeartRateByDuration(duration: Duration): List<HeartRate> {
        TODO("Not yet implemented")
    }

    override suspend fun writeHeartRate(heartRate: HeartRate): Result<HeartRate> {
        TODO("Not yet implemented")
    }
}
