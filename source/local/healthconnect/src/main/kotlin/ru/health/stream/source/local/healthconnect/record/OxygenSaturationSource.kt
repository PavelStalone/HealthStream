package ru.health.stream.source.local.healthconnect.record

import android.content.Context
import android.health.connect.datatypes.Metadata.RECORDING_METHOD_MANUAL_ENTRY
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Percentage
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import ru.health.stream.core.monitor.logV
import ru.health.stream.core.monitor.logW
import ru.health.stream.data.vitals.model.Device
import ru.health.stream.data.vitals.model.Resource
import ru.health.stream.data.vitals.model.measurement.OxygenSaturation
import ru.health.stream.source.infrastructure.source.local.LocalDeviceSource
import ru.health.stream.source.local.healthconnect.HealthConnectManager
import kotlin.reflect.KClass
import kotlin.uuid.ExperimentalUuidApi
import androidx.health.connect.client.records.metadata.Device as DeviceData

@OptIn(ExperimentalUuidApi::class)
internal class OxygenSaturationSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val localDeviceSource: LocalDeviceSource,
    private val healthConnectManager: HealthConnectManager,
) : MeasurementSource<OxygenSaturation>() {

    override val type: KClass<OxygenSaturation> = OxygenSaturation::class

    override suspend fun getMeasurementByRange(
        start: Instant,
        end: Instant,
    ): List<OxygenSaturation> = runCatching {
        logV("getMeasurementByRange called: start=$start, end=$end")

        val response = healthConnectManager.healthConnectClient.readRecords(
            ReadRecordsRequest(
                OxygenSaturationRecord::class,
                timeRangeFilter = TimeRangeFilter.between(
                    start.toJavaInstant(),
                    end.toJavaInstant()
                )
            )
        )

        response.records.map { record -> record.metadata to record }
            .map { (metadata, record) ->
                val packageName = metadata.dataOrigin.packageName
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

                OxygenSaturation(
                    id = metadata.clientRecordId ?: metadata.id,
                    saturation = record.percentage.value.toFloat(),
                    createdAt = record.time.toKotlinInstant(),
                    resource = resource,
                )
            }
    }.onFailure { exception ->
        logW("Error while getMeasurementByRange running", exception)
    }.getOrElse { emptyList() }

    override suspend fun deleteMeasurement(measurement: OxygenSaturation): Result<OxygenSaturation> =
        runCatching {
            logV("deleteMeasurement called: measurement=$measurement")

            healthConnectManager.healthConnectClient.deleteRecords(
                recordType = OxygenSaturationRecord::class,
                recordIdsList = emptyList(),
                clientRecordIdsList = listOf(measurement.id)
            )

            measurement
        }.onFailure { exception ->
            logW("Error while deleteMeasurement running", exception)
        }

    override suspend fun writeMeasurement(
        measurement: OxygenSaturation,
    ): Result<OxygenSaturation> = runCatching {
        logV("writeMeasurement called: measurement=$measurement")

        writeMeasurements(listOf(measurement)).getOrThrow()
        measurement
    }.onFailure { exception ->
        logW("Error while writeMeasurement running", exception)
    }

    override suspend fun writeMeasurements(measurements: List<OxygenSaturation>): Result<List<OxygenSaturation>> =
        runCatching {
            logV("writeMeasurements called: measurements=$measurements")

            val records = measurements.map { measurement ->
                val metadata = when (val res = measurement.resource) {
                    is Resource.Manual -> Metadata.manualEntry(
                        clientRecordId = measurement.id,
                        device = DeviceData(type = DeviceData.TYPE_PHONE)
                    )

                    is Device -> Metadata.autoRecorded(
                        clientRecordId = measurement.id,
                        device = DeviceData(
                            type = DeviceData.TYPE_UNKNOWN,
                            model = res.id,
                        ),
                    )

                    is Resource.App -> throw IllegalArgumentException("Write data from other app not supported")
                }
                val instant = measurement.createdAt.toJavaInstant()

                OxygenSaturationRecord(
                    time = instant,
                    zoneOffset = null,
                    metadata = metadata,
                    percentage = Percentage(measurement.saturation.toDouble())
                )
            }

            healthConnectManager.healthConnectClient.insertRecords(records = records)
            measurements
        }.onFailure { exception ->
            logW("Error while writeMeasurements running", exception)
        }
}
