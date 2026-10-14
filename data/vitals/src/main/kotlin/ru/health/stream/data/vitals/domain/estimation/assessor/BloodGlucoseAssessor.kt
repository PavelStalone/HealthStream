package ru.health.stream.data.vitals.domain.estimation.assessor

import jakarta.inject.Inject
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ru.health.stream.data.personal.repository.UserRepository
import ru.health.stream.data.vitals.domain.estimation.MeasurementAssessor
import ru.health.stream.data.vitals.model.Estimation
import ru.health.stream.data.vitals.model.measurement.BloodGlucose
import kotlin.reflect.KClass

class BloodGlucoseAssessor @Inject constructor(
    private val userRepository: UserRepository,
) : MeasurementAssessor<BloodGlucose> {

    override val type: KClass<BloodGlucose> = BloodGlucose::class

    override suspend fun analyze(measurement: BloodGlucose): Estimation? = runCatching {
        val levels = requireNotNull(levels(measurement.createdAt))
        val level = levels.firstNotNullOf { (level, ranges) ->
            if (ranges.any { range -> measurement.level.toFloat() in range }) level else null
        }

        Estimation(level = level)
    }.getOrNull()

    override suspend fun levels(
        date: Instant,
    ): Map<Estimation.Level, List<ClosedRange<Float>>> = runCatching {
        val user = requireNotNull(userRepository.getUser())
        val userAge = user.datePeriodAfterBirthday(
            localDate = date.toLocalDateTime(TimeZone.currentSystemDefault()).date
        ).years

        val severeHypoglycemia = if (userAge < CHILD_AGE) 2.8f else 3.0f
        val hypoglycemia = if (userAge < ELDERLY_AGE) 3.3f else 4.6f
        val normalUpperBound = when {
            userAge < CHILD_AGE -> 5.6f
            userAge >= ELDERLY_AGE -> 6.0f
            else -> 5.5f
        }
        val hyperglycemicCrisis = when {
            userAge < CHILD_AGE -> 8.5f
            userAge >= ELDERLY_AGE -> 9.4f
            else -> 8.5f
        }

        return mapOf(
            Estimation.Level.CRITICAL to listOf(
                0f..severeHypoglycemia,
                hyperglycemicCrisis..Float.MAX_VALUE,
            ),
            Estimation.Level.LOW to listOf(severeHypoglycemia..hypoglycemia),
            Estimation.Level.HIGH to listOf(normalUpperBound..hyperglycemicCrisis),
            Estimation.Level.NORMAL to listOf(hypoglycemia..normalUpperBound),
        )
    }.getOrElse { emptyMap() }

    companion object {
        private const val CHILD_AGE = 18
        private const val ELDERLY_AGE = 50
    }
}
