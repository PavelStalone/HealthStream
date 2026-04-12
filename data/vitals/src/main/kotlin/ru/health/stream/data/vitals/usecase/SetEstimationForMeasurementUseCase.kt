package ru.health.stream.data.vitals.usecase

import jakarta.inject.Inject
import jakarta.inject.Singleton
import ru.health.stream.core.common.usecase.UseCaseWithParams
import ru.health.stream.data.vitals.domain.estimation.MeasurementAnalyzer
import ru.health.stream.data.vitals.model.Estimation
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.data.vitals.model.measurement.copy

@Singleton
class SetEstimationForMeasurementUseCase @Inject constructor(
    private val measurementAnalyzer: MeasurementAnalyzer,
) : UseCaseWithParams<Measurement, Measurement>() {

    override suspend fun invoke(params: Measurement): Measurement = runCatching {
        require(params[Estimation] == null)
        val estimation = requireNotNull(measurementAnalyzer.analyze(params))

        params.copy(metadata = params.metadata + estimation)
    }.getOrDefault(params)
}
