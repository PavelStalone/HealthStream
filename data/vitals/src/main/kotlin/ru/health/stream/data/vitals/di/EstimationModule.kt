package ru.health.stream.data.vitals.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.health.stream.data.vitals.domain.estimation.MeasurementAssessor
import ru.health.stream.data.vitals.domain.estimation.assessor.BloodPressureAssessor
import ru.health.stream.data.vitals.domain.estimation.assessor.BodyWeightAssessor
import ru.health.stream.data.vitals.domain.estimation.assessor.HeartRateAssessor
import ru.health.stream.data.vitals.domain.estimation.assessor.OxygenSaturationAssessor
import ru.health.stream.data.vitals.model.measurement.Measurement

@Module
@InstallIn(SingletonComponent::class)
internal object EstimationModule {

    @Provides
    @Suppress("UNCHECKED_CAST")
    fun provideMeasurementAssessors(
        heartRateAssessor: HeartRateAssessor,
        bodyWeightAssessor: BodyWeightAssessor,
        bloodPressureAssessor: BloodPressureAssessor,
        oxygenSaturationAssessor: OxygenSaturationAssessor,
    ): List<MeasurementAssessor<Measurement>> = listOf(
        heartRateAssessor as MeasurementAssessor<Measurement>,
        bodyWeightAssessor as MeasurementAssessor<Measurement>,
        bloodPressureAssessor as MeasurementAssessor<Measurement>,
        oxygenSaturationAssessor as MeasurementAssessor<Measurement>,
    )
}
