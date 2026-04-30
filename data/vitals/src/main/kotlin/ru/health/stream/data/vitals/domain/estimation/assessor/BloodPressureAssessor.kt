package ru.health.stream.data.vitals.domain.estimation.assessor

import androidx.collection.FloatFloatPair
import jakarta.inject.Inject
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ru.health.stream.data.personal.repository.UserRepository
import ru.health.stream.data.vitals.domain.estimation.MeasurementAssessor
import ru.health.stream.data.vitals.model.Estimation
import ru.health.stream.data.vitals.model.changeByPriority
import ru.health.stream.data.vitals.model.measurement.BloodPressure
import ru.health.stream.data.vitals.model.measurement.BodyWeight
import ru.health.stream.data.vitals.repository.MeasurementRepository
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.days

class BloodPressureAssessor @Inject constructor(
    private val userRepository: UserRepository,
    private val measurementRepository: MeasurementRepository,
) : MeasurementAssessor<BloodPressure> {

    override val type: KClass<BloodPressure> = BloodPressure::class

    suspend fun calculateNormalPressure(date: Instant, age: Int): FloatFloatPair {
        val bodyWeight = measurementRepository.getMeasurementsByRange(
            from = date.minus(20.days),
            to = date,
            type = BodyWeight::class
        ).firstOrNull()

        var normalSystolic: Float
        var normalDiastolic: Float

        if (bodyWeight != null) {
            // Формула Волынского для АД с учетом веса
            normalSystolic = 109 + (0.5f * age) + (0.1f * bodyWeight.weight.kg)
            normalDiastolic = 63 + (0.1f * age) + (0.15f * bodyWeight.weight.kg)
        } else {
            // Формула Шестакова для АД с учетом только возраста
            normalSystolic = 102 + (0.6f * age)
            normalDiastolic = 63 + (0.4f * age)
        }

        return FloatFloatPair(normalSystolic, normalDiastolic)
    }


    override suspend fun analyze(measurement: BloodPressure): Estimation? = runCatching {
        val levels = requireNotNull(levels(measurement.createdAt))
        val systolicLevel = levels.firstNotNullOf { (level, ranges) ->
            if (measurement.systolic in ranges.first()) level else null
        }
        val diastolicLevel = levels.firstNotNullOf { (level, ranges) ->
            if (measurement.diastolic in ranges.last()) level else null
        }

        val level = requireNotNull(systolicLevel.changeByPriority(diastolicLevel))
        Estimation(level = level)
    }.getOrNull()

    override suspend fun levels(
        date: Instant,
    ): Map<Estimation.Level, List<ClosedRange<Float>>> = runCatching {
        val user = requireNotNull(userRepository.getUser())
        val userAge = user.datePeriodAfterBirthday(
            localDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        ).years

        val normalPressure = calculateNormalPressure(date = date, age = userAge)

        println("Normal pressure: $normalPressure")

        val lowSystolic = normalPressure.first - 20f
        val highSystolic = normalPressure.first + 10f
        val extraHighSystolic = normalPressure.first + 25f

        val lowDiastolic = normalPressure.second - 20f
        val highDiastolic = normalPressure.second + 5f
        val extraHighDiastolic = normalPressure.second + 12f

        // Нормы АД по классификации ВОЗ
        return mapOf(
            Estimation.Level.LOW to listOf(
                0f..lowSystolic,
                0f..lowDiastolic,
            ),
            Estimation.Level.NORMAL to listOf(
                lowSystolic..highSystolic,
                lowDiastolic..highDiastolic,
            ),
            Estimation.Level.HIGH to listOf(
                highSystolic..extraHighSystolic,
                highDiastolic..extraHighDiastolic,
            ),
            Estimation.Level.EXTRA_HIGH to listOf(
                extraHighSystolic..Float.MAX_VALUE,
                extraHighDiastolic..Float.MAX_VALUE,
            ),
        )
    }.getOrElse { emptyMap() }
}
