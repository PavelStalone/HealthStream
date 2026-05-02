package ru.health.stream.data.vitals.domain.estimation.assessor

import jakarta.inject.Inject
import kotlinx.datetime.Instant
import ru.health.stream.data.personal.repository.UserRepository
import ru.health.stream.data.vitals.domain.estimation.MeasurementAssessor
import ru.health.stream.data.vitals.model.Estimation
import ru.health.stream.data.vitals.model.measurement.BodyWeight
import kotlin.reflect.KClass

class BodyWeightAssessor @Inject constructor(
    private val userRepository: UserRepository,
) : MeasurementAssessor<BodyWeight> {

    override val type: KClass<BodyWeight> = BodyWeight::class

    override suspend fun analyze(measurement: BodyWeight): Estimation? = runCatching {
        val levels = requireNotNull(levels(measurement.createdAt))
        val level = levels.firstNotNullOf { (level, ranges) ->
            if (ranges.any { range -> measurement.weight.kg in range }) level else null
        }

        Estimation(level = level)
    }.getOrNull()

    override suspend fun levels(
        date: Instant,
    ): Map<Estimation.Level, List<ClosedRange<Float>>> = runCatching {
        val user = requireNotNull(userRepository.getUser())
        val userHeight = user.height.meters

        val lowWeight = massByIndex(20f, userHeight)
        val highWeight = massByIndex(25f, userHeight)
        val criticalWeight = massByIndex(30f, userHeight)

        // Нормы Веса по ИМТ
        return mapOf(
            Estimation.Level.LOW to listOf(0f..lowWeight),
            Estimation.Level.NORMAL to listOf(lowWeight..highWeight),
            Estimation.Level.HIGH to listOf(highWeight..criticalWeight),
            Estimation.Level.CRITICAL to listOf(criticalWeight..Float.MAX_VALUE),
        )
    }.getOrElse { emptyMap() }

    private fun massByIndex(imt: Float, height: Double): Float {
        return (imt * height * height).toFloat()
    }
}
