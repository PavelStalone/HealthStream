package ru.health.stream.data.vitals.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import ru.health.stream.core.test.TestLoggerRule
import ru.health.stream.data.personal.model.Email
import ru.health.stream.data.personal.model.User
import ru.health.stream.data.personal.model.cm
import ru.health.stream.data.personal.repository.UserRepository
import ru.health.stream.data.vitals.domain.estimation.MeasurementAnalyzer
import ru.health.stream.data.vitals.domain.estimation.MeasurementAssessor
import ru.health.stream.data.vitals.domain.estimation.assessor.BloodPressureAssessor
import ru.health.stream.data.vitals.domain.estimation.assessor.BodyWeightAssessor
import ru.health.stream.data.vitals.domain.estimation.assessor.HeartRateAssessor
import ru.health.stream.data.vitals.model.Estimation
import ru.health.stream.data.vitals.model.Resource
import ru.health.stream.data.vitals.model.kg
import ru.health.stream.data.vitals.model.measurement.BloodPressure
import ru.health.stream.data.vitals.model.measurement.BodyWeight
import ru.health.stream.data.vitals.model.measurement.HeartRate
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.data.vitals.repository.MeasurementRepository

class SetEstimationForMeasurementUseCaseTest {

    private val userRepository: UserRepository = mockk()
    private val measurementRepository: MeasurementRepository = mockk()

    private val heartRateAssessor = HeartRateAssessor(userRepository, measurementRepository)
    private val bloodPressureAssessor = BloodPressureAssessor(userRepository, measurementRepository)
    private val bodyWeightAssessor = BodyWeightAssessor(userRepository)

    @Suppress("UNCHECKED_CAST")
    private val measurementAnalyzer = MeasurementAnalyzer(
        listOf(
            heartRateAssessor,
            bloodPressureAssessor,
            bodyWeightAssessor
        ) as List<MeasurementAssessor<Measurement>>
    )

    private val useCase = SetEstimationForMeasurementUseCase(measurementAnalyzer)

    @get:Rule
    val logger = TestLoggerRule()

    private val testUser = User(
        email = Email("test@example.ru"),
        height = 180.cm,
        gender = true, // Male
        firstName = "Jay",
        lastName = "Doe",
        birthday = LocalDate(1990, 1, 1)
    )

    @Test
    fun `invoke attaches estimation for HeartRate`() = runTest {
        coEvery { userRepository.getUser() } returns testUser
        coEvery {
            measurementRepository.getMeasurementsByRange(
                any(),
                any(),
                HeartRate::class
            )
        } returns emptyList()

        val heartRate = HeartRate(
            pulse = 70,
            createdAt = Instant.parse("2024-05-22T10:00:00Z"),
            resource = Resource.Manual
        )

        val result = useCase(heartRate)

        println(">>> Input Pulse: ${heartRate.pulse}")
        println(">>> Calculated Estimation: ${result[Estimation]}")

        assertNotNull(result[Estimation])
        assertEquals(Estimation.Level.NORMAL, result[Estimation]?.level)
    }

    @Test
    fun `invoke attaches estimation for BloodPressure`() = runTest {
        coEvery { userRepository.getUser() } returns testUser
        coEvery {
            measurementRepository.getMeasurementsByRange(
                any(),
                any(),
                BodyWeight::class
            )
        } returns emptyList()

        val bp = BloodPressure(
            id = "test-id",
            systolic = 120f,
            diastolic = 80f,
            createdAt = Instant.parse("2024-05-22T10:00:00Z"),
            resource = Resource.Manual
        )

        val result = useCase(bp)

        println(">>> Input Blood Pressure: ${bp.systolic}/${bp.diastolic}")
        println(">>> Calculated Estimation: ${result[Estimation]}")

        assertNotNull(result[Estimation])
        assertEquals(Estimation.Level.NORMAL, result[Estimation]?.level)
    }

    @Test
    fun `invoke does not change measurement if estimation already exists`() = runTest {
        val existingEstimation = Estimation(level = Estimation.Level.HIGH)
        val heartRate = HeartRate(
            pulse = 150,
            createdAt = Instant.parse("2024-05-22T10:00:00Z"),
            resource = Resource.Manual,
            metadata = existingEstimation
        )

        val result = useCase(heartRate)

        assertEquals(existingEstimation, result[Estimation])
    }

    @Test
    fun `heart rate estimation for female user follows Gulati formula`() = runTest {
        val femaleUser = testUser.copy(gender = false)
        coEvery { userRepository.getUser() } returns femaleUser
        coEvery {
            measurementRepository.getMeasurementsByRange(
                any(),
                any(),
                HeartRate::class
            )
        } returns emptyList()

        val heartRate = HeartRate(
            pulse = 160,
            createdAt = Instant.parse("2024-05-22T10:00:00Z"),
            resource = Resource.Manual
        )

        val result = useCase(heartRate)

        println(">>> Gender: Female, Pulse: ${heartRate.pulse}")
        println(">>> Calculated Estimation: ${result[Estimation]}")

        assertEquals(Estimation.Level.CRITICAL, result[Estimation]?.level)
    }

    @Test
    fun `heart rate estimation boundaries for male user`() = runTest {
        coEvery { userRepository.getUser() } returns testUser
        coEvery {
            measurementRepository.getMeasurementsByRange(
                any(),
                any(),
                HeartRate::class
            )
        } returns emptyList()

        println("--- Case 1: Default resting HR (70) ---")
        val testCases70 = listOf(
            55 to Estimation.Level.LOW,
            57 to Estimation.Level.NORMAL,
            127 to Estimation.Level.NORMAL,
            128 to Estimation.Level.HIGH,
            161 to Estimation.Level.HIGH,
            162 to Estimation.Level.CRITICAL
        )

        testCases70.forEach { (pulse, expectedLevel) ->
            val hr = HeartRate(
                pulse = pulse,
                createdAt = Instant.parse("2024-05-22T10:00:00Z"),
                resource = Resource.Manual
            )
            val result = useCase(hr)
            assertEquals(
                "Failed for pulse $pulse (Resting 70)",
                expectedLevel,
                result[Estimation]?.level
            )
        }

        println("--- Case 2: Custom resting HR (60) ---")
        val history = listOf(
            HeartRate(
                pulse = 60,
                createdAt = Instant.parse("2024-05-21T10:00:00Z"),
                resource = Resource.Manual
            )
        )
        coEvery {
            measurementRepository.getMeasurementsByRange(
                any(),
                any(),
                HeartRate::class
            )
        } returns history

        val testCases60 = listOf(
            47 to Estimation.Level.LOW,
            49 to Estimation.Level.NORMAL,
            122 to Estimation.Level.NORMAL,
            123 to Estimation.Level.HIGH,
            159 to Estimation.Level.HIGH,
            160 to Estimation.Level.CRITICAL
        )

        testCases60.forEach { (pulse, expectedLevel) ->
            val hr = HeartRate(
                pulse = pulse,
                createdAt = Instant.parse("2024-05-22T10:00:00Z"),
                resource = Resource.Manual
            )
            val result = useCase(hr)
            assertEquals(
                "Failed for pulse $pulse (Resting 60)",
                expectedLevel,
                result[Estimation]?.level
            )
        }
    }

    @Test
    fun `estimation boundaries for female user for all types`() = runTest {
        val femaleUser = testUser.copy(gender = false)
        coEvery { userRepository.getUser() } returns femaleUser

        coEvery {
            measurementRepository.getMeasurementsByRange(
                any(),
                any(),
                HeartRate::class
            )
        } returns emptyList()
        val hr = HeartRate(
            id = "f-hr",
            pulse = 130,
            createdAt = Instant.parse("2024-05-22T10:00:00Z"),
            resource = Resource.Manual
        )
        assertEquals(Estimation.Level.HIGH, useCase(hr)[Estimation]?.level)

        coEvery {
            measurementRepository.getMeasurementsByRange(
                any(),
                any(),
                BodyWeight::class
            )
        } returns emptyList()
        val bp = BloodPressure(
            id = "f-bp",
            systolic = 135f,
            diastolic = 85f,
            createdAt = Instant.parse("2024-05-22T10:00:00Z"),
            resource = Resource.Manual
        )
        assertEquals(Estimation.Level.HIGH, useCase(bp)[Estimation]?.level)

        val bw = BodyWeight(
            id = "f-bw",
            weight = 70.kg,
            createdAt = Instant.parse("2024-05-22T10:00:00Z"),
            resource = Resource.Manual
        )
        assertEquals(Estimation.Level.NORMAL, useCase(bw)[Estimation]?.level)
    }

    @Test
    fun `blood pressure estimation with body weight history`() = runTest {
        coEvery { userRepository.getUser() } returns testUser

        val weightHistory = listOf(
            BodyWeight(
                id = "h-bw",
                weight = 100.kg,
                createdAt = Instant.parse("2024-05-21T10:00:00Z"),
                resource = Resource.Manual
            )
        )
        coEvery {
            measurementRepository.getMeasurementsByRange(
                any(),
                any(),
                BodyWeight::class
            )
        } returns weightHistory

        val bpNormal = BloodPressure(
            id = "bp-v",
            systolic = 136f,
            diastolic = 81f,
            createdAt = Instant.parse("2024-05-22T10:00:00Z"),
            resource = Resource.Manual
        )
        assertEquals(Estimation.Level.NORMAL, useCase(bpNormal)[Estimation]?.level)

        val bpHigh = BloodPressure(
            id = "bp-v-h",
            systolic = 150f,
            diastolic = 81f,
            createdAt = Instant.parse("2024-05-22T10:00:00Z"),
            resource = Resource.Manual
        )
        assertEquals(Estimation.Level.HIGH, useCase(bpHigh)[Estimation]?.level)
    }

    @Test
    fun `blood pressure estimation boundaries`() = runTest {
        coEvery { userRepository.getUser() } returns testUser // 34yo Male
        coEvery {
            measurementRepository.getMeasurementsByRange(
                any(),
                any(),
                BodyWeight::class
            )
        } returns emptyList()

        val testCases = listOf(
            (100f to 70f) to Estimation.Level.LOW,
            (110f to 70f) to Estimation.Level.NORMAL,
            (140f to 70f) to Estimation.Level.HIGH,
            (150f to 70f) to Estimation.Level.CRITICAL,

            (120f to 50f) to Estimation.Level.NORMAL,
            (120f to 85f) to Estimation.Level.HIGH,
            (120f to 90f) to Estimation.Level.CRITICAL,

            (150f to 60f) to Estimation.Level.CRITICAL
        )

        testCases.forEach { (values, expectedLevel) ->
            val bp = BloodPressure(
                id = "bp-${values.first}",
                systolic = values.first,
                diastolic = values.second,
                createdAt = Instant.parse("2024-05-22T10:00:00Z"),
                resource = Resource.Manual
            )
            val result = useCase(bp)

            println(">>> BP: ${values.first}/${values.second}, Expected: $expectedLevel, Actual: ${result[Estimation]?.level}")

            assertEquals(
                "Failed for BP ${values.first}/${values.second}",
                expectedLevel,
                result[Estimation]?.level
            )
        }
    }

    @Test
    fun `body weight estimation boundaries based on BMI`() = runTest {
        coEvery { userRepository.getUser() } returns testUser

        val testCases = listOf(
            60f to Estimation.Level.LOW,
            70f to Estimation.Level.NORMAL,
            85f to Estimation.Level.HIGH,
            100f to Estimation.Level.CRITICAL
        )

        testCases.forEach { (weight, expectedLevel) ->
            val bw = BodyWeight(
                id = "bw-$weight",
                weight = weight.kg,
                createdAt = Instant.parse("2024-05-22T10:00:00Z"),
                resource = Resource.Manual
            )
            val result = useCase(bw)
            println(">>> Weight: $weight kg, Expected: $expectedLevel, Actual: ${result[Estimation]?.level}")
            assertEquals("Failed for weight $weight", expectedLevel, result[Estimation]?.level)
        }
    }
}
