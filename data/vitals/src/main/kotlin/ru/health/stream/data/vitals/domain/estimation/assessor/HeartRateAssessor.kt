package ru.health.stream.data.vitals.domain.estimation.assessor

import jakarta.inject.Inject
import kotlinx.datetime.Clock
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

    override val type: KClass<HeartRate> = HeartRate::class

    private var restingHR: Double? = null

    suspend fun calculateRestingHR(start: Instant): Double {
        val buffer = restingHR

        if (buffer != null) return buffer

        val currentRestingHR = measurementRepository.getMeasurementsByRange(
            from = start.minus(10.days),
            to = start,
            type = HeartRate::class,
        ).map { heartRate -> heartRate.pulse }
            .filter { pulse -> pulse in 40..100 }
            .average()

        if (!currentRestingHR.isNaN()) {
            restingHR = currentRestingHR
            return currentRestingHR
        }

        return 70.0
    }

    // Метод Карвонена
    override suspend fun analyze(measurement: HeartRate): Estimation? = runCatching {
        val user = requireNotNull(userRepository.getUser())
        val userAge = user.datePeriodAfterBirthday(
            localDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        ).years

        val resting = calculateRestingHR(start = measurement.createdAt)
        val maxHR = if (user.gender) {
            208 - (0.7 * userAge) // Формула Танака
        } else {
            206 - (0.88 * userAge) // Формула Марты Гулати
        }

        val loadIntensity = (measurement.pulse - resting) / (maxHR - resting)

        val level = when (loadIntensity) {
            in 0.00..0.60 -> Estimation.Level.NORMAL
            in 0.60..0.80 -> Estimation.Level.HIGH
            in 0.80..2.00 -> Estimation.Level.EXTRA_HIGH
            else -> Estimation.Level.LOW
        }

        Estimation(level = level)
    }.getOrNull()
}
