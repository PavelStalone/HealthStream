package ru.health.stream.data.vitals.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.health.stream.data.vitals.domain.estimation.MeasurementAssessor
import ru.health.stream.data.vitals.domain.estimation.assessor.BloodPressureAssessor
import ru.health.stream.data.vitals.domain.estimation.assessor.HeartRateAssessor
import ru.health.stream.data.vitals.model.measurement.Measurement

@Module
@InstallIn(SingletonComponent::class)
internal object EstimationModule {

    @Provides
    @Suppress("UNCHECKED_CAST")
    fun provideMeasurementAssessors(
        heartRateAssessor: HeartRateAssessor,
        bloodPressureAssessor: BloodPressureAssessor,
    ): List<MeasurementAssessor<Measurement>> = listOf(
        heartRateAssessor as MeasurementAssessor<Measurement>,
        bloodPressureAssessor as MeasurementAssessor<Measurement>,
    )
}
