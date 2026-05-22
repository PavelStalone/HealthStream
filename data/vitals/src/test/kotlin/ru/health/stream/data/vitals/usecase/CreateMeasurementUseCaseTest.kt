package ru.health.stream.data.vitals.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.health.stream.core.test.TestLoggerRule
import ru.health.stream.data.vitals.model.Resource
import ru.health.stream.data.vitals.model.measurement.HeartRate
import ru.health.stream.data.vitals.repository.MeasurementRepository
import kotlin.uuid.Uuid

class CreateMeasurementUseCaseTest {

    private val measurementRepository: MeasurementRepository = mockk()
    private val setEstimationForMeasurementUseCase: SetEstimationForMeasurementUseCase = mockk()

    private val useCase = CreateMeasurementUseCase(
        measurementRepository,
        setEstimationForMeasurementUseCase
    )

    @get:Rule
    val logger = TestLoggerRule()

    @Test
    fun `invoke validates HeartRate and saves it`() = runTest {
        val heartRate = HeartRate(
            id = Uuid.random().toString(),
            pulse = 70,
            createdAt = Instant.parse("2024-05-22T10:00:00Z"),
            resource = Resource.Manual
        )

        coEvery { setEstimationForMeasurementUseCase(heartRate) } returns heartRate
        coEvery { measurementRepository.createMeasurement(heartRate) } returns Result.success(
            heartRate
        )

        val result = useCase(heartRate)

        println(">>> Measurement: $heartRate")
        println(">>> Result Success: ${result.isSuccess}")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke fails if UUID is invalid`() = runTest {
        val heartRate = HeartRate(
            id = "invalid-uuid",
            pulse = 70,
            createdAt = Instant.parse("2024-05-22T10:00:00Z"),
            resource = Resource.Manual
        )

        val result = useCase(heartRate)

        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke fails if validation fails`() = runTest {
        val heartRate = HeartRate(
            id = Uuid.random().toString(),
            pulse = -10, // Invalid
            createdAt = Instant.parse("2024-05-22T10:00:00Z"),
            resource = Resource.Manual
        )

        val result = useCase(heartRate)

        assertTrue(result.isFailure)
    }
}
