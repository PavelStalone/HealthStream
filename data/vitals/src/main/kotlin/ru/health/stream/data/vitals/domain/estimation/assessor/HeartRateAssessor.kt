package ru.health.stream.data.vitals.domain.estimation.assessor

import jakarta.inject.Inject
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ru.health.stream.data.personal.repository.UserRepository
import ru.health.stream.data.vitals.domain.estimation.MeasurementAssessor
import ru.health.stream.data.vitals.model.Estimation
import ru.health.stream.data.vitals.model.measurement.HeartRate
import ru.health.stream.data.vitals.repository.MeasurementRepository
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.days

class HeartRateAssessor @Inject constructor(
    private val userRepository: UserRepository,
    private val measurementRepository: MeasurementRepository,
) : MeasurementAssessor<HeartRate> {

    private val actualDuration = 20.days
    private val restingHrDuration = 10.days

    override val type: KClass<HeartRate> = HeartRate::class

    private var restingHR: Float? = null
    private var writeDate: Instant? = null

    suspend fun calculateRestingHR(start: Instant): Float {
        val buffer = restingHR
        val isActual = writeDate?.minus(start)
            ?.let { duration -> duration <= actualDuration }
            ?: false

        if (buffer != null && isActual) return buffer

        val currentRestingHR = measurementRepository.getMeasurementsByRange(
            from = start.minus(restingHrDuration),
            to = start,
            type = HeartRate::class,
        ).map { heartRate -> heartRate.pulse }
            .filter { pulse -> pulse in 40..100 }
            .average()
            .toFloat()

        if (!currentRestingHR.isNaN()) {
            restingHR = currentRestingHR
            writeDate = start
            return currentRestingHR
        }

        return 70f
    }

    // Метод Карвонена
    override suspend fun analyze(measurement: HeartRate): Estimation? = runCatching {
        val levels = requireNotNull(levels(measurement.createdAt))
        val level = levels.firstNotNullOf { (level, ranges) ->
            if (ranges.any { range -> measurement.pulse.toFloat() in range }) level else null
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

        val resting = calculateRestingHR(start = date)

        val maxHR = if (user.gender) {
            208 - (0.7 * userAge) // Формула Танака
        } else {
            206 - (0.88 * userAge) // Формула Марты Гулати
        }.toFloat()

        val pulseByIntensity = { intensity: Float -> (maxHR - resting) * intensity + resting }

        val lowHR = (resting * 0.8f)

        return mapOf(
            Estimation.Level.LOW to listOf(0f..lowHR),
            Estimation.Level.NORMAL to listOf(lowHR..pulseByIntensity(0.5f)),
            Estimation.Level.HIGH to listOf(pulseByIntensity(0.5f)..pulseByIntensity(0.8f)),
            Estimation.Level.CRITICAL to listOf(pulseByIntensity(0.8f)..Float.MAX_VALUE),
        )
    }.getOrElse { emptyMap() }
}
