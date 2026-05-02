package ru.health.stream.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import ru.health.stream.core.common.di.Dispatcher
import ru.health.stream.core.monitor.logD
import ru.health.stream.core.monitor.logV
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.data.vitals.usecase.SetEstimationForMeasurementUseCase
import ru.health.stream.source.local.PrimaryMeasurementSource

@HiltWorker
class MeasurementWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val primaryMeasurementSource: PrimaryMeasurementSource,
    private val setEstimationForMeasurementUseCase: SetEstimationForMeasurementUseCase,
    @Dispatcher(Dispatcher.IO) private val coroutineDispatcher: CoroutineDispatcher,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {
        logV("MeasurementWorker called")

        val measurements = primaryMeasurementSource
            .getMeasurementsWithoutEstimation(type = Measurement::class)

        logD("Found ${measurements.size} measurements without estimation")

        val estimatedMeasurements = measurements.chunked(size = 500)
            .mapIndexed { index, measurements ->
                async(context = coroutineDispatcher, start = CoroutineStart.LAZY) {
                    logD("Process estimations chunk $index")

                    measurements.map { measurement ->
                        setEstimationForMeasurementUseCase(params = measurement)
                    }
                }
            }
            .chunked(size = 5)
            .flatMap { chunkedAsync ->
                chunkedAsync.awaitAll()
            }

        estimatedMeasurements.forEachIndexed { index, chunk ->
            logD("Write estimations chunk $index")

            primaryMeasurementSource.writeMeasurements(chunk)
        }

        logD("MeasurementWorker finished")
        Result.success()
    }
}
