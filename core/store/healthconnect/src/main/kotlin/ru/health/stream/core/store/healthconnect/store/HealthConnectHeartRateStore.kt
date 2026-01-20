package ru.health.stream.core.store.healthconnect.store

import android.content.Context
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.records.metadata.Metadata.Companion.RECORDING_METHOD_MANUAL_ENTRY
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import ru.health.stream.core.monitor.logV
import ru.health.stream.core.monitor.logW
import ru.health.stream.core.store.healthconnect.HealthConnectManager
import ru.health.stream.core.store.measurement.HeartRateStore
import ru.health.stream.feature.vitals.data.model.Resource
import ru.health.stream.feature.vitals.data.model.Resource.WithManufacturer.BloodPressure
import ru.health.stream.feature.vitals.data.model.Resource.WithManufacturer.PulseOximeter
import ru.health.stream.feature.vitals.data.model.Resource.WithManufacturer.WeightScale
import ru.health.stream.feature.vitals.source.local.model.HeartRate
import javax.inject.Inject
import kotlin.time.Duration

internal class HealthConnectHeartRateStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val healthConnectManager: HealthConnectManager,
) : HeartRateStore {

    private val PERMISSIONS = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getWritePermission(HeartRateRecord::class),
    )

    override suspend fun isActive(): Boolean = healthConnectManager.hasAllPermissions(PERMISSIONS)

    override suspend fun getHeartRateByRange(
        start: Instant,
        end: Instant,
    ): List<HeartRate> = runCatching {
        logV("getHeartRateByRange called: start=$start, end=$end")

        val response = healthConnectManager.healthConnectClient.readRecords(
            ReadRecordsRequest(
                HeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(
                    start.toJavaInstant(),
                    end.toJavaInstant()
                )
            )
        )

        logV("Founded heart rate records: ${response.records}")

        response.records.map { record -> record.metadata to record.samples }
            .flatMap { (metadata, records) ->
                val packageName = metadata.dataOrigin.packageName
                val resource = if (packageName.equals(context.packageName, ignoreCase = true)) {
                    runCatching {
                        require(metadata.recordingMethod != RECORDING_METHOD_MANUAL_ENTRY)

                        val device = requireNotNull(metadata.device)
                        val model = requireNotNull(device.model)

                        when (model) {
                            WeightScale::class.simpleName -> WeightScale(manufacturer = device.manufacturer)
                            BloodPressure::class.simpleName -> BloodPressure(manufacturer = device.manufacturer)
                            PulseOximeter::class.simpleName -> PulseOximeter(manufacturer = device.manufacturer)
                            else -> error("Model not found")
                        }
                    }.getOrElse { Resource.Manual }
                } else {
                    Resource.FromApp(packageName = packageName)
                }

                records.map { record ->
                    HeartRate(
                        resource = resource,
                        pulse = record.beatsPerMinute.toInt(),
                        createdAt = record.time.toKotlinInstant(),
                    )
                }
            }
    }.onFailure { exception ->
        logW("Error while getHeartRateByRange running", exception)
    }.getOrElse { emptyList() }

    override suspend fun getHeartRateByDuration(duration: Duration): List<HeartRate> {
        logV("getHeartRateByDuration called: duration=$duration")

        val now = Clock.System.now()

        return getHeartRateByRange(start = now.minus(duration), end = now)
    }

    override suspend fun writeHeartRate(heartRate: HeartRate): Result<HeartRate> = runCatching {
        logV("writeHeartRate called: heartRate=$heartRate")

        val metadata = when (val res = heartRate.resource) {
            is Resource.Manual -> Metadata.manualEntry(Device(type = Device.TYPE_PHONE))
            is Resource.WithManufacturer -> Metadata.autoRecorded(
                Device(
                    type = Device.TYPE_UNKNOWN,
                    model = res.javaClass.simpleName,
                    manufacturer = res.manufacturer
                )
            )

            is Resource.FromApp -> throw IllegalArgumentException("Write data from other app not supported")
        }
        val instant = heartRate.createdAt.toJavaInstant()
        val heartRateRecord = HeartRateRecord(
            startTime = instant,
            samples = listOf(
                HeartRateRecord.Sample(
                    time = instant,
                    beatsPerMinute = heartRate.pulse.toLong()
                )
            ),
            endTime = instant,
            startZoneOffset = null,
            endZoneOffset = null,
            metadata = metadata,
        )

        healthConnectManager.healthConnectClient.insertRecords(records = listOf(heartRateRecord))
        heartRate
    }.onFailure { exception ->
        logW("Error while writeHeartRate running", exception)
    }
}
