package ru.health.stream.data.vitals.domain.estimation.assessor

import jakarta.inject.Inject
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ru.health.stream.data.personal.repository.UserRepository
import ru.health.stream.data.vitals.domain.estimation.MeasurementAssessor
import ru.health.stream.data.vitals.model.Estimation
import ru.health.stream.data.vitals.model.measurement.OxygenSaturation
import kotlin.reflect.KClass

class OxygenSaturationAssessor @Inject constructor(
    private val userRepository: UserRepository,
) : MeasurementAssessor<OxygenSaturation> {

    override val type: KClass<OxygenSaturation> = OxygenSaturation::class

    override suspend fun analyze(measurement: OxygenSaturation): Estimation? = runCatching {
        val levels = requireNotNull(levels(measurement.createdAt))
        val level = levels.firstNotNullOf { (level, ranges) ->
            if (ranges.any { range -> measurement.saturation in range }) level else null
        }

        Estimation(level = level)
    }.getOrNull()

    override suspend fun levels(
        date: Instant,
    ): Map<Estimation.Level, List<ClosedRange<Float>>> = runCatching {
        val user = requireNotNull(userRepository.getUser())
        val userAge = user.datePeriodAfterBirthday(
            localDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        ).years

        val lowSaturation = if (userAge > 70) 93f else 95f
        val extraLowSaturation = lowSaturation - 3f

        // Нормы SpO2 по нормативам
        return mapOf(
            Estimation.Level.CRITICAL to listOf(0f..extraLowSaturation),
            Estimation.Level.LOW to listOf(extraLowSaturation..lowSaturation),
            Estimation.Level.NORMAL to listOf(lowSaturation..100f),
        )
    }.getOrElse { emptyMap() }
}
