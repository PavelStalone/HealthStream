package ru.health.stream.data.vitals.usecase

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.health.stream.core.test.TestLoggerRule
import ru.health.stream.data.vitals.model.MeasurementGroup
import ru.health.stream.data.vitals.model.Period
import ru.health.stream.data.vitals.model.Resource
import ru.health.stream.data.vitals.model.kg
import ru.health.stream.data.vitals.model.measurement.BloodGlucose
import ru.health.stream.data.vitals.model.measurement.BloodPressure
import ru.health.stream.data.vitals.model.measurement.BodyWeight
import ru.health.stream.data.vitals.model.measurement.HeartRate
import ru.health.stream.data.vitals.model.measurement.OxygenSaturation
import ru.health.stream.data.vitals.model.measurement.RespirationRate
import java.time.DayOfWeek.MONDAY

class GroupMeasurementByPeriodUseCaseTest {

    private val useCase = GroupMeasurementByPeriodUseCase()
    private val timeZone = TimeZone.UTC

    @get:Rule
    val logger = TestLoggerRule()

    @Test
    fun `invoke groups measurements and calculates precise arithmetic mean`() {
        val measurements = listOf(
            HeartRate(
                pulse = 60,
                createdAt = Instant.parse("2024-05-22T10:00:00Z"),
                resource = Resource.Manual
            ),
            HeartRate(
                pulse = 60,
                createdAt = Instant.parse("2024-05-22T11:00:00Z"),
                resource = Resource.Manual
            ),
            HeartRate(
                pulse = 100,
                createdAt = Instant.parse("2024-05-22T12:00:00Z"),
                resource = Resource.Manual
            )
        )

        val result = useCase(
            period = Period.Day,
            timeZone = timeZone,
            measurements = measurements
        )

        val group = result[HeartRate::class]!![0] as MeasurementGroup.HeartRate
        println(">>> Arithmetic Mean: ${group.mean.value}")
        println(">>> Range: ${group.range.start}..${group.range.endInclusive}")

        assertEquals((60 + 60 + 100) / 3.0, group.mean.value, 0.001)
        assertEquals(3, group.mean.count)
        assertEquals(60, group.range.start)
        assertEquals(100, group.range.endInclusive)
    }

    @Test
    fun `invoke groups by OneHour period correctly`() {
        val baseTime = Instant.parse("2024-05-22T10:30:00Z")
        val measurements = listOf(
            HeartRate(pulse = 70, createdAt = baseTime, resource = Resource.Manual),
            HeartRate(
                pulse = 80,
                createdAt = baseTime.plus(10, DateTimeUnit.MINUTE),
                resource = Resource.Manual
            ),
            HeartRate(
                pulse = 90,
                createdAt = baseTime.plus(1, DateTimeUnit.HOUR, timeZone),
                resource = Resource.Manual
            )
        )

        val result =
            useCase(period = Period.OneHour, timeZone = timeZone, measurements = measurements)
        val groups = result[HeartRate::class]!!

        assertEquals(2, groups.size)

        val g1 = groups[0] as MeasurementGroup.HeartRate
        assertEquals(75.0, g1.mean.value, 0.001)
        assertEquals(Instant.parse("2024-05-22T10:00:00Z"), g1.dateRange.start)
        assertEquals(Instant.parse("2024-05-22T11:00:00Z"), g1.dateRange.endInclusive)

        val g2 = groups[1] as MeasurementGroup.HeartRate
        assertEquals(90.0, g2.mean.value, 0.001)
        assertEquals(Instant.parse("2024-05-22T11:00:00Z"), g2.dateRange.start)
    }

    @Test
    fun `invoke groups by SixHour period correctly`() {
        val baseTime = Instant.parse("2024-05-22T01:00:00Z")
        val measurements = listOf(
            HeartRate(pulse = 70, createdAt = baseTime, resource = Resource.Manual),
            HeartRate(
                pulse = 90,
                createdAt = baseTime.plus(3, DateTimeUnit.HOUR, timeZone),
                resource = Resource.Manual
            )
        )

        val result =
            useCase(period = Period.SixHour, timeZone = timeZone, measurements = measurements)
        val groups = result[HeartRate::class]!!

        assertEquals(1, groups.size)
        val group = groups[0] as MeasurementGroup.HeartRate
        assertEquals(80.0, group.mean.value, 0.001)
        assertEquals(Instant.parse("2024-05-22T00:00:00Z"), group.dateRange.start)
        assertEquals(Instant.parse("2024-05-22T06:00:00Z"), group.dateRange.endInclusive)
    }

    @Test
    fun `invoke groups by Day period correctly`() {
        val baseTime = Instant.parse("2024-05-22T10:00:00Z")
        val measurements = listOf(
            HeartRate(pulse = 70, createdAt = baseTime, resource = Resource.Manual),
            HeartRate(
                pulse = 90,
                createdAt = baseTime.plus(1, DateTimeUnit.DAY, timeZone),
                resource = Resource.Manual
            )
        )

        val result = useCase(period = Period.Day, timeZone = timeZone, measurements = measurements)
        val groups = result[HeartRate::class]!!

        assertEquals(2, groups.size)
        assertEquals(Instant.parse("2024-05-22T00:00:00Z"), groups[0].dateRange.start)
        assertEquals(Instant.parse("2024-05-23T00:00:00Z"), groups[0].dateRange.endInclusive)
    }

    @Test
    fun `invoke groups by Week period correctly`() {
        val baseTime = Instant.parse("2024-05-22T10:00:00Z") // Wednesday
        val measurements = listOf(
            HeartRate(pulse = 70, createdAt = baseTime, resource = Resource.Manual),
            HeartRate(
                pulse = 90,
                createdAt = baseTime.plus(7, DateTimeUnit.DAY, timeZone),
                resource = Resource.Manual
            )
        )

        val result = useCase(
            period = Period.Week(MONDAY),
            timeZone = timeZone,
            measurements = measurements
        )
        val groups = result[HeartRate::class]!!

        assertEquals(2, groups.size)
        // 2024-05-22 is Wednesday. Monday was 2024-05-20.
        assertEquals(Instant.parse("2024-05-20T00:00:00Z"), groups[0].dateRange.start)
        assertEquals(Instant.parse("2024-05-27T00:00:00Z"), groups[0].dateRange.endInclusive)
    }

    @Test
    fun `invoke groups by Month period correctly`() {
        val baseTime = Instant.parse("2024-05-22T10:00:00Z")
        val measurements = listOf(
            HeartRate(pulse = 70, createdAt = baseTime, resource = Resource.Manual),
            HeartRate(
                pulse = 90,
                createdAt = baseTime.plus(1, DateTimeUnit.MONTH, timeZone),
                resource = Resource.Manual
            )
        )

        val result =
            useCase(period = Period.Month, timeZone = timeZone, measurements = measurements)
        val groups = result[HeartRate::class]!!

        assertEquals(2, groups.size)
        assertEquals(Instant.parse("2024-05-01T00:00:00Z"), groups[0].dateRange.start)
        assertEquals(Instant.parse("2024-06-01T00:00:00Z"), groups[0].dateRange.endInclusive)
    }

    @Test
    fun `invoke groups by Year period correctly`() {
        val baseTime = Instant.parse("2024-05-22T10:00:00Z")
        val measurements = listOf(
            HeartRate(pulse = 70, createdAt = baseTime, resource = Resource.Manual),
            HeartRate(
                pulse = 90,
                createdAt = baseTime.plus(1, DateTimeUnit.YEAR, timeZone),
                resource = Resource.Manual
            )
        )

        val result = useCase(period = Period.Year, timeZone = timeZone, measurements = measurements)
        val groups = result[HeartRate::class]!!

        assertEquals(2, groups.size)
        assertEquals(Instant.parse("2024-01-01T00:00:00Z"), groups[0].dateRange.start)
        assertEquals(Instant.parse("2025-01-01T00:00:00Z"), groups[0].dateRange.endInclusive)
    }

    @Test
    fun `invoke creates separate groups for different periods`() {
        val measurements = listOf(
            HeartRate(
                pulse = 70,
                createdAt = Instant.parse("2024-05-22T10:00:00Z"),
                resource = Resource.Manual
            ),
            HeartRate(
                pulse = 80,
                createdAt = Instant.parse("2024-05-22T11:05:00Z"),
                resource = Resource.Manual
            )
        )

        val result = useCase(
            period = Period.OneHour,
            timeZone = timeZone,
            measurements = measurements
        )

        val groups = result[HeartRate::class]!!
        println(">>> OneHour Period Groups: ${groups.size}")
        assertEquals(2, groups.size)
    }

    @Test
    fun `invoke groups multiple measurement types together`() {
        val baseTime = Instant.parse("2024-05-22T10:00:00Z")
        val measurements = listOf(
            HeartRate(pulse = 70, createdAt = baseTime, resource = Resource.Manual),
            BloodPressure(
                id = "bp1",
                systolic = 120f,
                diastolic = 80f,
                createdAt = baseTime,
                resource = Resource.Manual
            ),
            BodyWeight(
                id = "bw1",
                weight = 80.kg,
                createdAt = baseTime,
                resource = Resource.Manual
            ),
            HeartRate(
                pulse = 80,
                createdAt = baseTime.plus(1, DateTimeUnit.HOUR, timeZone),
                resource = Resource.Manual
            )
        )

        val result = useCase(
            period = Period.Day,
            timeZone = timeZone,
            measurements = measurements
        )

        assertEquals(3, result.size)
        assertTrue(result.containsKey(HeartRate::class))
        assertTrue(result.containsKey(BloodPressure::class))
        assertTrue(result.containsKey(BodyWeight::class))

        val hrGroups = result[HeartRate::class]!!
        assertEquals(1, hrGroups.size)
        assertEquals(75.0, (hrGroups[0] as MeasurementGroup.HeartRate).mean.value, 0.001)

        val bpGroups = result[BloodPressure::class]!!
        assertEquals(1, bpGroups.size)
        assertEquals(
            120.0,
            (bpGroups[0] as MeasurementGroup.BloodPressure).systolicMean.value,
            0.001
        )

        val bwGroups = result[BodyWeight::class]!!
        assertEquals(1, bwGroups.size)
        assertEquals(80.0, (bwGroups[0] as MeasurementGroup.BodyWeight).mean.value, 0.001)
    }

    @Test
    fun `invoke groups all supported measurement types together`() {
        val baseTime = Instant.parse("2024-05-22T10:00:00Z")
        val measurements = listOf(
            HeartRate(pulse = 70, createdAt = baseTime, resource = Resource.Manual),
            BloodPressure(
                id = "bp1",
                systolic = 120f,
                diastolic = 80f,
                createdAt = baseTime,
                resource = Resource.Manual
            ),
            BodyWeight(
                id = "bw1",
                weight = 80.kg,
                createdAt = baseTime,
                resource = Resource.Manual
            ),
            BloodGlucose(id = "bg1", level = 5.5, createdAt = baseTime, resource = Resource.Manual),
            OxygenSaturation(
                id = "os1",
                saturation = 98f,
                createdAt = baseTime,
                resource = Resource.Manual
            ),
            RespirationRate(
                id = "rr1",
                rate = 16.0,
                createdAt = baseTime,
                resource = Resource.Manual
            )
        )

        val result = useCase(
            period = Period.Day,
            timeZone = timeZone,
            measurements = measurements
        )

        assertEquals(6, result.size)
        assertTrue(result.containsKey(HeartRate::class))
        assertTrue(result.containsKey(BloodPressure::class))
        assertTrue(result.containsKey(BodyWeight::class))
        assertTrue(result.containsKey(BloodGlucose::class))
        assertTrue(result.containsKey(OxygenSaturation::class))
        assertTrue(result.containsKey(RespirationRate::class))

        println(">>> Grouped 6 different measurement types successfully")
    }

    @Test
    fun `invoke returns empty map for empty list`() {
        val result = useCase(
            period = Period.Day,
            timeZone = timeZone,
            measurements = emptyList()
        )

        assertTrue(result.isEmpty())
    }
}
