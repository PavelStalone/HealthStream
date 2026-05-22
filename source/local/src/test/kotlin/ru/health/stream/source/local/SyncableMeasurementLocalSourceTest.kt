package ru.health.stream.source.local

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import ru.health.stream.core.test.TestLoggerRule
import ru.health.stream.data.vitals.model.Resource
import ru.health.stream.data.vitals.model.measurement.HeartRate
import kotlin.reflect.KClass

@OptIn(ExperimentalCoroutinesApi::class)
class SyncableMeasurementLocalSourceTest {

    @get:Rule
    val logger = TestLoggerRule()

    private val primarySource: PrimaryMeasurementSource = mockk()
    private val externalSource: ExternalMeasurementSource = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val syncSource = SyncableMeasurementLocalSource(
        primarySource = primarySource,
        externalSources = setOf(externalSource),
        ioDispatcher = testDispatcher
    )

    @Test
    fun `getMeasurementsWithoutEstimation delegates to primarySource`() = runTest {
        val type = HeartRate::class
        val expected = listOf(
            HeartRate(pulse = 70, createdAt = Instant.parse("2024-05-22T10:00:00Z"), resource = Resource.Manual)
        )
        coEvery { primarySource.getMeasurementsWithoutEstimation(type) } returns expected

        val result = syncSource.getMeasurementsWithoutEstimation(type)

        assertEquals(expected, result)
        coVerify(exactly = 1) { primarySource.getMeasurementsWithoutEstimation(type) }
    }

    @Test
    fun `getMeasurementsByRange triggers synchronization and returns primary data`() = runTest {
        val start = Instant.parse("2024-05-22T00:00:00Z")
        val end = Instant.parse("2024-05-22T23:59:59Z")
        val type = HeartRate::class

        val externalData = listOf(
            HeartRate(pulse = 70, createdAt = Instant.parse("2024-05-22T10:00:00Z"), resource = Resource.Manual)
        )
        val primaryData = listOf(
            HeartRate(pulse = 75, createdAt = Instant.parse("2024-05-22T11:00:00Z"), resource = Resource.Manual)
        )

        coEvery { externalSource.getMeasurementsByRange(start, end, type) } returns externalData
        coEvery { primarySource.getAllMeasurementsByRange(start, end, type) } returns primaryData
        coEvery { primarySource.writeMeasurements(any<List<HeartRate>>()) } returns Result.success(emptyList())
        coEvery { primarySource.getMeasurementsByRange(start, end, type) } returns primaryData

        val result = syncSource.getMeasurementsByRange(start, end, type)

        println(">>> External Data Size: ${externalData.size}")
        println(">>> Primary Data Size (Before Sync): ${primaryData.size}")
        
        coVerify { primarySource.writeMeasurements<HeartRate>(match { it.size == 1 && it[0].pulse == 70 }) }
        assertEquals(primaryData, result)
    }

    @Test
    fun `syncExternalToPrimary handles duplicates by createdAt`() = runTest {
        val start = Instant.parse("2024-05-22T00:00:00Z")
        val end = Instant.parse("2024-05-22T23:59:59Z")
        val type = HeartRate::class
        val time = Instant.parse("2024-05-22T10:00:00Z")

        val externalData = listOf(
            HeartRate(pulse = 70, createdAt = time, resource = Resource.Manual)
        )
        val primaryData = listOf(
            HeartRate(pulse = 70, createdAt = time, resource = Resource.Manual)
        )

        coEvery { externalSource.getMeasurementsByRange(start, end, type) } returns externalData
        coEvery { primarySource.getAllMeasurementsByRange(start, end, type) } returns primaryData
        coEvery { primarySource.getMeasurementsByRange(start, end, type) } returns primaryData

        syncSource.getMeasurementsByRange(start, end, type)

        println(">>> Duplicate detected by time. Should NOT write.")
        coVerify(exactly = 0) { primarySource.writeMeasurements<HeartRate>(any()) }
    }

    @Test
    fun `getMeasurementsFlowByRange performs background sync`() = runTest {
        val start = Instant.parse("2024-05-22T00:00:00Z")
        val end = Instant.parse("2024-05-22T23:59:59Z")
        val type = HeartRate::class

        val externalData = listOf(
            HeartRate(pulse = 70, createdAt = Instant.parse("2024-05-22T10:00:00Z"), resource = Resource.Manual)
        )
        val primaryData = listOf(
            HeartRate(pulse = 75, createdAt = Instant.parse("2024-05-22T11:00:00Z"), resource = Resource.Manual)
        )

        every { externalSource.getMeasurementsFlowByRange(start, end, type) } returns flowOf(externalData)
        every { primarySource.getMeasurementsFlowByRange(start, end, type) } returns flowOf(primaryData)
        coEvery { primarySource.getAllMeasurementsByRange(start, end, type) } returns emptyList()
        coEvery { primarySource.writeMeasurements(any<List<HeartRate>>()) } returns Result.success(emptyList())

        syncSource.getMeasurementsFlowByRange(start, end, type).test {
            val result = awaitItem()
            assertEquals(primaryData, result)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { primarySource.writeMeasurements<HeartRate>(match { it.size == 1 }) }
        println(">>> Background sync verified via Flow")
    }

    @Test
    fun `syncExternalToPrimary handles id collision by generating new uuid`() = runTest {
        val start = Instant.parse("2024-05-22T00:00:00Z")
        val end = Instant.parse("2024-05-22T23:59:59Z")
        val type = HeartRate::class
        val id = "collision-id"

        val externalData = listOf(
            HeartRate(id = id, pulse = 70, createdAt = Instant.parse("2024-05-22T10:00:00Z"), resource = Resource.Manual)
        )
        // Primary has different time but SAME ID
        val primaryData = listOf(
            HeartRate(id = id, pulse = 80, createdAt = Instant.parse("2024-05-22T09:00:00Z"), resource = Resource.Manual)
        )

        coEvery { externalSource.getMeasurementsByRange(start, end, type) } returns externalData
        coEvery { primarySource.getAllMeasurementsByRange(start, end, type) } returns primaryData
        coEvery { primarySource.writeMeasurements<HeartRate>(any()) } returns Result.success(emptyList())
        coEvery { primarySource.getMeasurementsByRange(start, end, type) } returns primaryData

        syncSource.getMeasurementsByRange(start, end, type)

        println(">>> ID Collision detected. Should write with NEW ID.")
        coVerify { primarySource.writeMeasurements<HeartRate>(match { it.size == 1 && it[0].id != id }) }
    }

    @Test
    fun `syncExternalToPrimary handles multiple external sources`() = runTest {
        val start = Instant.parse("2024-05-22T00:00:00Z")
        val end = Instant.parse("2024-05-22T23:59:59Z")
        val type = HeartRate::class

        val externalSource2: ExternalMeasurementSource = mockk()
        val multiSyncSource = SyncableMeasurementLocalSource(
            primarySource = primarySource,
            externalSources = setOf(externalSource, externalSource2),
            ioDispatcher = testDispatcher
        )

        val data1 = listOf(HeartRate(pulse = 70, createdAt = Instant.parse("2024-05-22T10:00:00Z"), resource = Resource.Manual))
        val data2 = listOf(HeartRate(pulse = 80, createdAt = Instant.parse("2024-05-22T11:00:00Z"), resource = Resource.Manual))

        coEvery { externalSource.getMeasurementsByRange(start, end, type) } returns data1
        coEvery { externalSource2.getMeasurementsByRange(start, end, type) } returns data2
        coEvery { primarySource.getAllMeasurementsByRange(start, end, type) } returns emptyList()
        coEvery { primarySource.writeMeasurements<HeartRate>(any()) } returns Result.success(emptyList())
        coEvery { primarySource.getMeasurementsByRange(start, end, type) } returns emptyList()

        multiSyncSource.getMeasurementsByRange(start, end, type)

        coVerify { primarySource.writeMeasurements<HeartRate>(match { it.size == 2 }) }
    }

    @Test
    fun `deleteMeasurement propagates to all sources`() = runTest {
        val hr = HeartRate(pulse = 70, createdAt = Instant.parse("2024-05-22T10:00:00Z"), resource = Resource.Manual)
        
        coEvery { externalSource.deleteMeasurement(hr) } returns Result.success(hr)
        coEvery { primarySource.deleteMeasurement(hr) } returns Result.success(hr)

        val result = syncSource.deleteMeasurement(hr)
        
        coVerify { externalSource.deleteMeasurement(hr) }
        coVerify { primarySource.deleteMeasurement(hr) }
        assertEquals(hr, result.getOrNull())
    }

    @Test
    fun `writeMeasurement propagates to all sources`() = runTest {
        val hr = HeartRate(pulse = 70, createdAt = Instant.parse("2024-05-22T10:00:00Z"), resource = Resource.Manual)
        
        coEvery { externalSource.writeMeasurement(hr) } returns Result.success(hr)
        coEvery { primarySource.writeMeasurement(hr) } returns Result.success(hr)

        val result = syncSource.writeMeasurement(hr)

        coVerify { externalSource.writeMeasurement(hr) }
        coVerify { primarySource.writeMeasurement(hr) }
        assertEquals(hr, result.getOrNull())
    }

    @Test
    fun `writeMeasurements propagates to all sources`() = runTest {
        val measurements = listOf(
            HeartRate(pulse = 70, createdAt = Instant.parse("2024-05-22T10:00:00Z"), resource = Resource.Manual),
            HeartRate(pulse = 80, createdAt = Instant.parse("2024-05-22T11:00:00Z"), resource = Resource.Manual)
        )
        
        coEvery { externalSource.writeMeasurements(measurements) } returns Result.success(measurements)
        coEvery { primarySource.writeMeasurements(measurements) } returns Result.success(measurements)

        val result = syncSource.writeMeasurements(measurements)

        coVerify { externalSource.writeMeasurements(measurements) }
        coVerify { primarySource.writeMeasurements(measurements) }
        assertEquals(measurements, result.getOrNull())
    }
}
