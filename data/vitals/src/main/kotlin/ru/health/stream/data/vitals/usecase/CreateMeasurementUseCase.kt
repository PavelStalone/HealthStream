package ru.health.stream.data.vitals.usecase

import ru.health.stream.core.common.usecase.UseCaseWithParams
import ru.health.stream.data.vitals.model.measurement.BloodGlucose
import ru.health.stream.data.vitals.model.measurement.BloodPressure
import ru.health.stream.data.vitals.model.measurement.BodyWeight
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.data.vitals.model.measurement.HeartRate
import ru.health.stream.data.vitals.model.measurement.OxygenSaturation
import ru.health.stream.data.vitals.model.measurement.RespirationRate
import ru.health.stream.data.vitals.model.measurement.check
import ru.health.stream.data.vitals.repository.MeasurementRepository
import javax.inject.Inject
import kotlin.uuid.Uuid

class CreateMeasurementUseCase @Inject constructor(
    private val measurementRepository: MeasurementRepository
) : UseCaseWithParams<Measurement, Result<Measurement>>() {

    override suspend fun invoke(params: Measurement): Result<Measurement> = runCatching {
        Uuid.parse(params.id) // Check uuid format

        when (params) {
            is BloodGlucose -> params.check()
            is BloodPressure -> params.check()
            is BodyWeight -> params.check()
            is HeartRate -> params.check()
            is OxygenSaturation -> params.check()
            is RespirationRate -> params.check()
            else -> {
                /* Do nothing */
            }
        }

        measurementRepository.createMeasurement(params).getOrThrow()
    }
}
