package ru.health.stream.data.report.usecase

import androidx.collection.FloatFloatPair
import kotlinx.datetime.Clock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.health.stream.data.vitals.model.EmptyMetadata
import ru.health.stream.data.vitals.model.Estimation
import ru.health.stream.data.vitals.model.MeasurementGroup
import ru.health.stream.data.vitals.model.Resource
import ru.health.stream.data.vitals.model.Weight
import ru.health.stream.data.vitals.model.measurement.BloodGlucose
import ru.health.stream.data.vitals.model.measurement.BloodPressure
import ru.health.stream.data.vitals.model.measurement.BodyWeight
import ru.health.stream.data.vitals.model.measurement.HeartRate
import ru.health.stream.data.vitals.model.measurement.OxygenSaturation
import ru.health.stream.data.vitals.model.measurement.RespirationRate
import kotlin.time.Duration.Companion.hours

class CalculateMeasurementSummaryUseCaseTest {

    private val useCase = CalculateMeasurementSummaryUseCase()

    @Test
    fun `invoke with empty list returns empty map`() {
        val result = useCase(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `invoke with HeartRate measurements`() {
        val now = Clock.System.now()
        val measurements = listOf(
            HeartRate(id = "1", createdAt = now, resource = Resource.Manual, pulse = 60),
            HeartRate(id = "2", createdAt = now + 1.hours, resource = Resource.Manual, pulse = 80)
        )

        val result = useCase(measurements)

        assertEquals(1, result.size)
        val summary = result[HeartRate::class]!!
        assertEquals(2, summary.counts)
        val group = summary.group as MeasurementGroup.HeartRate
        assertEquals(60, group.range.start)
        assertEquals(80, group.range.endInclusive)
        assertEquals(now..(now + 1.hours), group.dateRange)
    }

    @Test
    fun `invoke with BloodPressure measurements`() {
        val now = Clock.System.now()
        val measurements = listOf(
            BloodPressure(id = "1", createdAt = now, resource = Resource.Manual, systolic = 120f, diastolic = 80f),
            BloodPressure(id = "2", createdAt = now + 1.hours, resource = Resource.Manual, systolic = 140f, diastolic = 90f),
            BloodPressure(id = "3", createdAt = now + 2.hours, resource = Resource.Manual, systolic = 110f, diastolic = 75f)
        )

        val result = useCase(measurements)

        val summary = result[BloodPressure::class]!!
        assertEquals(3, summary.counts)
        val group = summary.group as MeasurementGroup.BloodPressure
        assertEquals(110f, group.systolicRange.start)
        assertEquals(140f, group.systolicRange.endInclusive)
        assertEquals(75f, group.diastolicRange.start)
        assertEquals(90f, group.diastolicRange.endInclusive)
        assertEquals(now..(now + 2.hours), group.dateRange)
    }

    @Test
    fun `BloodPressure min and max BP by difference`() {
        val now = Clock.System.now()
        val measurements = listOf(
            BloodPressure(id = "1", createdAt = now, resource = Resource.Manual, systolic = 120f, diastolic = 80f), // Diff 40
            BloodPressure(id = "2", createdAt = now + 1.hours, resource = Resource.Manual, systolic = 150f, diastolic = 80f), // Diff 70
            BloodPressure(id = "3", createdAt = now + 2.hours, resource = Resource.Manual, systolic = 110f, diastolic = 80f) // Diff 30
        )

        val result = useCase(measurements)

        val group = result[BloodPressure::class]!!.group as MeasurementGroup.BloodPressure
        assertEquals(FloatFloatPair(110f, 80f), group.minBpByDifference)
        assertEquals(FloatFloatPair(150f, 80f), group.maxBpByDifference)
    }

    @Test
    fun `invoke with BodyWeight measurements`() {
        val now = Clock.System.now()
        val measurements = listOf(
            BodyWeight(id = "1", createdAt = now, resource = Resource.Manual, weight = Weight(70f)),
            BodyWeight(id = "2", createdAt = now + 1.hours, resource = Resource.Manual, weight = Weight(75f))
        )

        val result = useCase(measurements)

        val group = result[BodyWeight::class]!!.group as MeasurementGroup.BodyWeight
        assertEquals(70f, group.range.start)
        assertEquals(75f, group.range.endInclusive)
    }

    @Test
    fun `invoke with BloodGlucose measurements`() {
        val now = Clock.System.now()
        val measurements = listOf(
            BloodGlucose(id = "1", createdAt = now, resource = Resource.Manual, level = 5.5),
            BloodGlucose(id = "2", createdAt = now + 1.hours, resource = Resource.Manual, level = 6.5)
        )

        val result = useCase(measurements)

        val group = result[BloodGlucose::class]!!.group as MeasurementGroup.BloodGlucose
        assertEquals(5.5, group.range.start, 0.0)
        assertEquals(6.5, group.range.endInclusive, 0.0)
    }

    @Test
    fun `invoke with OxygenSaturation measurements`() {
        val now = Clock.System.now()
        val measurements = listOf(
            OxygenSaturation(id = "1", createdAt = now, resource = Resource.Manual, saturation = 95f),
            OxygenSaturation(id = "2", createdAt = now + 1.hours, resource = Resource.Manual, saturation = 98f)
        )

        val result = useCase(measurements)

        val group = result[OxygenSaturation::class]!!.group as MeasurementGroup.OxygenSaturation
        assertEquals(95f, group.range.start)
        assertEquals(98f, group.range.endInclusive)
    }

    @Test
    fun `invoke with RespirationRate measurements`() {
        val now = Clock.System.now()
        val measurements = listOf(
            RespirationRate(id = "1", createdAt = now, resource = Resource.Manual, rate = 12.0),
            RespirationRate(id = "2", createdAt = now + 1.hours, resource = Resource.Manual, rate = 16.0)
        )

        val result = useCase(measurements)

        val group = result[RespirationRate::class]!!.group as MeasurementGroup.RespirationRate
        assertEquals(12.0, group.range.start, 0.0)
        assertEquals(16.0, group.range.endInclusive, 0.0)
    }

    @Test
    fun `estimationsCount calculation`() {
        val now = Clock.System.now()
        val normalEstimation = Estimation(level = Estimation.Level.NORMAL)
        val highEstimation = Estimation(level = Estimation.Level.HIGH)

        val measurements = listOf(
            HeartRate(id = "1", createdAt = now, resource = Resource.Manual, pulse = 70, metadata = EmptyMetadata + normalEstimation),
            HeartRate(id = "2", createdAt = now + 1.hours, resource = Resource.Manual, pulse = 100, metadata = EmptyMetadata + highEstimation),
            HeartRate(id = "3", createdAt = now + 2.hours, resource = Resource.Manual, pulse = 72, metadata = EmptyMetadata + normalEstimation)
        )

        val result = useCase(measurements)

        val summary = result[HeartRate::class]!!
        assertEquals(
            mapOf(
                Estimation.Level.NORMAL to 2,
                Estimation.Level.HIGH to 1
            ),
            summary.estimationsCount
        )
    }

    @Test
    fun `multiple types of measurements`() {
        val now = Clock.System.now()
        val measurements = listOf(
            HeartRate(id = "1", createdAt = now, resource = Resource.Manual, pulse = 70),
            BodyWeight(id = "2", createdAt = now, resource = Resource.Manual, weight = Weight(70f))
        )

        val result = useCase(measurements)

        assertEquals(2, result.size)
        assertTrue(result.containsKey(HeartRate::class))
        assertTrue(result.containsKey(BodyWeight::class))
    }
}
