package ru.health.stream.data.vitals.usecase

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import ru.health.stream.core.test.TestLoggerRule
import ru.health.stream.data.vitals.model.Period

class DateTransformerUseCaseTest {

    @get:Rule
    val logger = TestLoggerRule()

    private val timeZone = TimeZone.UTC

    @Test
    fun `invoke returns 0 for start of period`() {
        val start = Instant.parse("2024-05-22T00:00:00Z")
        val end = Instant.parse("2024-05-22T23:59:59.999999999Z")
        val useCase = DateTransformerUseCase(
            timeZone = timeZone,
            period = Period.Day,
            dateRange = start..end
        )

        val result = useCase(start)

        println(">>> Date: $start, Position: $result")

        assertEquals(0f, result, 0.0001f)
    }

    @Test
    fun `invoke returns 1 for end of period`() {
        val start = Instant.parse("2024-05-22T00:00:00Z")
        val end = Instant.parse("2024-05-22T23:59:59Z")
        val useCase = DateTransformerUseCase(
            timeZone = timeZone,
            period = Period.Day,
            dateRange = start..end
        )

        val periodEnd = Instant.parse("2024-05-23T00:00:00Z")
        val result = useCase(periodEnd)

        println(">>> Date (End): $periodEnd, Position: $result")

        assertEquals(1f, result, 0.0001f)
    }

    @Test
    fun `invoke returns 0_5 for middle of period`() {
        val start = Instant.parse("2024-05-22T00:00:00Z")
        val end = Instant.parse("2024-05-22T23:59:59Z")
        val useCase = DateTransformerUseCase(
            timeZone = timeZone,
            period = Period.Day,
            dateRange = start..end
        )

        val middle = Instant.parse("2024-05-22T12:00:00Z")
        val result = useCase(middle)

        assertEquals(0.5f, result, 0.0001f)
    }

    @Test
    fun `invoke works correctly for OneHour period`() {
        val date = Instant.parse("2024-05-22T10:30:00Z")
        val useCase = DateTransformerUseCase(
            timeZone = timeZone,
            period = Period.OneHour,
            dateRange = date..date
        )

        assertEquals(0f, useCase(Instant.parse("2024-05-22T10:00:00Z")), 0.0001f)
        assertEquals(0.5f, useCase(Instant.parse("2024-05-22T10:30:00Z")), 0.0001f)
        assertEquals(1f, useCase(Instant.parse("2024-05-22T11:00:00Z")), 0.0001f)
    }
}
