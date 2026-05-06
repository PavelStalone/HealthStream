package ru.health.stream.source.local.healthconnect.record

import android.content.Context
import android.health.connect.datatypes.Metadata.RECORDING_METHOD_MANUAL_ENTRY
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Pressure
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import ru.health.stream.core.monitor.logV
import ru.health.stream.core.monitor.logW
import ru.health.stream.data.vitals.model.Device
import ru.health.stream.data.vitals.model.Resource
import ru.health.stream.data.vitals.model.measurement.BloodPressure
import ru.health.stream.source.infrastructure.source.local.LocalDeviceSource
import ru.health.stream.source.local.healthconnect.HealthConnectManager
import kotlin.reflect.KClass
import kotlin.uuid.ExperimentalUuidApi
import androidx.health.connect.client.records.metadata.Device as DeviceData

@OptIn(ExperimentalUuidApi::class)
internal class BloodPressureSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val localDeviceSource: LocalDeviceSource,
    private val healthConnectManager: HealthConnectManager,
) : MeasurementSource<BloodPressure>() {

    override val type: KClass<BloodPressure> = BloodPressure::class

    override suspend fun getMeasurementByRange(
        start: Instant,
        end: Instant,
    ): List<BloodPressure> = runCatching {
        logV("getMeasurementByRange called: start=$start, end=$end")

        val response = healthConnectManager.healthConnectClient.readRecords(
            ReadRecordsRequest(
                BloodPressureRecord::class,
                timeRangeFilter = TimeRangeFilter.between(
                    start.toJavaInstant(),
                    end.toJavaInstant()
                )
            )
        )

        logV("Founded records: ${response.records}")

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

                BloodPressure(
                    id = metadata.clientRecordId ?: metadata.id,
                    systolic = record.systolic.inMillimetersOfMercury.toFloat(),
                    diastolic = record.diastolic.inMillimetersOfMercury.toFloat(),
                    createdAt = record.time.toKotlinInstant(),
                    resource = resource,
                )
            }
    }.onFailure { exception ->
        logW("Error while getMeasurementByRange running", exception)
    }.getOrElse { emptyList() }

    override suspend fun deleteMeasurement(measurement: BloodPressure): Result<BloodPressure> =
        runCatching {
            logV("deleteMeasurement called: measurement=$measurement")

            healthConnectManager.healthConnectClient.deleteRecords(
                recordType = BloodPressureRecord::class,
                recordIdsList = emptyList(),
                clientRecordIdsList = listOf(measurement.id)
            )

            measurement
        }.onFailure { exception ->
            logW("Error while deleteMeasurement running", exception)
        }

    override suspend fun writeMeasurement(
        measurement: BloodPressure,
    ): Result<BloodPressure> = runCatching {
        logV("writeMeasurement called: measurement=$measurement")

        writeMeasurements(listOf(measurement)).getOrThrow()
        measurement
    }.onFailure { exception ->
        logW("Error while writeMeasurement running", exception)
    }

    override suspend fun writeMeasurements(measurements: List<BloodPressure>): Result<List<BloodPressure>> =
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

                BloodPressureRecord(
                    time = instant,
                    zoneOffset = null,
                    metadata = metadata,
                    systolic = Pressure.millimetersOfMercury(measurement.systolic.toDouble()),
                    diastolic = Pressure.millimetersOfMercury(measurement.diastolic.toDouble()),
                )
            }

            healthConnectManager.healthConnectClient.insertRecords(records = records)
            measurements
        }.onFailure { exception ->
            logW("Error while writeMeasurements running", exception)
        }
}
