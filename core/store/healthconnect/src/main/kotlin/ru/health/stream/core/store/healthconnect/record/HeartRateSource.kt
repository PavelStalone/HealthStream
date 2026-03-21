package ru.health.stream.core.store.healthconnect.record

import android.content.Context
import android.health.connect.datatypes.Metadata.RECORDING_METHOD_MANUAL_ENTRY
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import ru.health.stream.core.monitor.logV
import ru.health.stream.core.monitor.logW
import ru.health.stream.core.store.healthconnect.HealthConnectManager
import ru.health.stream.feature.vitals.data.model.Device
import ru.health.stream.feature.vitals.data.model.Resource
import ru.health.stream.feature.vitals.data.model.measurement.HeartRate
import ru.health.stream.feature.vitals.source.local.LocalDeviceSource
import kotlin.reflect.KClass
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import androidx.health.connect.client.records.metadata.Device as DeviceData

@OptIn(ExperimentalUuidApi::class)
internal class HeartRateSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val localDeviceSource: LocalDeviceSource,
    private val healthConnectManager: HealthConnectManager,
) : MeasurementSource<HeartRate>() {

    override val type: KClass<HeartRate> = HeartRate::class

    override suspend fun getMeasurementByRange(
        start: Instant,
        end: Instant,
    ): List<HeartRate> = runCatching {
        logV("getMeasurementByRange called: start=$start, end=$end")

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
                val recordId = Uuid.parse(metadata.id).toLongs(Long::to)
                val resource = if (packageName.equals(context.packageName, ignoreCase = true)) {
                    runCatching {
                        require(metadata.recordingMethod != RECORDING_METHOD_MANUAL_ENTRY)

                        val device = requireNotNull(metadata.device)
                        val model = requireNotNull(device.model)

                        localDeviceSource.getDeviceById(model).getOrThrow()
                    }.getOrElse { Resource.Manual }
                } else {
                    Resource.App(packageName = packageName)
                }

                records.mapIndexed { index, record ->
                    val (mostSignificantBits, leastSignificantBits) = recordId

                    HeartRate(
                        id = Uuid.fromLongs(mostSignificantBits, leastSignificantBits + index)
                            .toString(),
                        createdAt = record.time.toKotlinInstant(),
                        pulse = record.beatsPerMinute.toInt(),
                        resource = resource,
                    )
                }
            }
    }.onFailure { exception ->
        logW("Error while getHeartRateByRange running", exception)
    }.getOrElse { emptyList() }

    override suspend fun writeMeasurement(
        measurement: HeartRate,
    ): Result<HeartRate> = runCatching {
        logV("writeHeartRate called: measurement=$measurement")

        val metadata = when (val res = measurement.resource) {
            is Resource.Manual -> Metadata.manualEntryWithId(
                id = measurement.id,
                device = DeviceData(type = DeviceData.TYPE_PHONE)
            )

            is Device -> Metadata.autoRecordedWithId(
                id = measurement.id,
                device = DeviceData(
                    type = DeviceData.TYPE_UNKNOWN,
                    model = res.id,
                ),
            )

            is Resource.App -> throw IllegalArgumentException("Write data from other app not supported")
        }
        val instant = measurement.createdAt.toJavaInstant()
        val heartRateRecord = HeartRateRecord(
            startTime = instant,
            samples = listOf(
                HeartRateRecord.Sample(
                    time = instant,
                    beatsPerMinute = measurement.pulse.toLong()
                )
            ),
            endTime = instant,
            startZoneOffset = null,
            endZoneOffset = null,
            metadata = metadata,
        )

        healthConnectManager.healthConnectClient.insertRecords(records = listOf(heartRateRecord))
        measurement
    }.onFailure { exception ->
        logW("Error while writeHeartRate running", exception)
    }
}
