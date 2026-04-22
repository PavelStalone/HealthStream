package ru.health.stream.feature.report.impl.presentation.viewmodel

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ru.health.stream.core.common.di.Dispatcher
import ru.health.stream.core.ui.model.UiMeasurement
import ru.health.stream.core.ui.model.asUi
import ru.health.stream.data.report.model.ReportFormat
import ru.health.stream.data.report.repository.ReportRepository
import ru.health.stream.data.vitals.model.measurement.BloodGlucose
import ru.health.stream.data.vitals.model.measurement.BloodPressure
import ru.health.stream.data.vitals.model.measurement.BodyWeight
import ru.health.stream.data.vitals.model.measurement.HeartRate
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.data.vitals.model.measurement.OxygenSaturation
import ru.health.stream.data.vitals.model.measurement.RespirationRate
import ru.health.stream.data.vitals.repository.MeasurementRepository
import javax.inject.Inject
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.days

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
internal class ReportViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val measurementRepository: MeasurementRepository,
    @Dispatcher(Dispatcher.IO) val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _reportFormat = MutableStateFlow(ReportFormat.PDF)
    val reportFormat = _reportFormat.asStateFlow()

    private val _selectedDataTypes = MutableStateFlow(setOf(UiMeasurement.Type.HEART_RATE))
    val selectedDataTypes = _selectedDataTypes.asStateFlow()

    private val _selectedDateRange: MutableStateFlow<ClosedRange<Instant>> =
        Clock.System.now().let { now ->
            MutableStateFlow(now.minus(7.days)..now)
        }
    val selectedDateRange = _selectedDateRange.asStateFlow()

    private val _expandedMeasurementGroup = MutableStateFlow<Set<String>>(emptySet())
    val expandedMeasurementGroup = _expandedMeasurementGroup.asStateFlow()

    private val _bannedMeasurements = MutableStateFlow<Set<String>>(emptySet())
    val bannedMeasurements = _bannedMeasurements.asStateFlow()

    private val _events = MutableSharedFlow<ReportUiEvent>()
    val events: SharedFlow<ReportUiEvent> = _events.asSharedFlow()

    private val measurements = combine(
        selectedDateRange,
        selectedDataTypes,
    ) { dateRange, dataTypes ->
        MeasurementQuery(
            from = dateRange.start,
            to = dateRange.endInclusive,
            types = dataTypes.map { type -> type.toMeasurementClass() }
        )
    }
        .distinctUntilChanged()
        .mapLatest { (to, from, types) ->
            types.asFlow()
                .flatMapMerge(concurrency = 3) { type ->
                    flow {
                        emit(
                            measurementRepository.getMeasurementsByRange(
                                to = to,
                                from = from,
                                type = type,
                            )
                        )
                    }.flowOn(ioDispatcher)
                }
                .toList()
                .flatten()
                .sortedByDescending { measurement -> measurement.createdAt }
        }
        .stateIn(
            scope = viewModelScope,
            initialValue = emptyList(),
            started = SharingStarted.WhileSubscribed(3000),
        )

    val measurementsGroup = measurements.mapLatest { measurements ->
        val timeZone = TimeZone.currentSystemDefault()
        val groupedByDate = measurements.groupBy { measurement ->
            measurement.createdAt.toLocalDateTime(timeZone).date
        }

        groupedByDate.map { (date, measurements) ->
            MeasurementGroup(
                date = date,
                id = date.toString(),
                measurements = measurements.map { measurement -> measurement.asUi() },
            )
        }
    }.stateIn(
        scope = viewModelScope,
        initialValue = emptyList(),
        started = SharingStarted.WhileSubscribed(3000),
    )

    fun expandMeasurementGroup(id: String) {
        _expandedMeasurementGroup.update { groups ->
            if (id in groups) groups - id else groups + id
        }
    }

    fun banMeasurement(id: String) {
        _bannedMeasurements.value += id
    }

    fun unbanMeasurement(id: String) {
        _bannedMeasurements.value -= id
    }

    fun banMeasurements(ids: List<String>) {
        viewModelScope.launch(ioDispatcher) {
            _bannedMeasurements.value += ids
        }
    }

    fun unbanMeasurements(ids: List<String>) {
        viewModelScope.launch(ioDispatcher) {
            _bannedMeasurements.value -= ids
        }
    }

    fun onDateRangeChange(start: Instant, end: Instant) {
        _selectedDateRange.value = start..end
    }

    fun onFormatChange(format: ReportFormat) {
        _reportFormat.value = format
    }

    fun onDataTypeToggle(dataType: UiMeasurement.Type) {
        _selectedDataTypes.update { types ->
            if (types.contains(dataType)) types - dataType else types + dataType
        }
    }

    fun generateReport() {
        viewModelScope.launch(ioDispatcher) {
            val measurements = measurements.value
            val bannedMeasurements = bannedMeasurements.value

            val result = reportRepository.generateReport(
                format = _reportFormat.value,
                measurements = measurements.filter { measurement ->
                    measurement.id !in bannedMeasurements
                },
                dateRange = _selectedDateRange.value,
            )

            _events.emit(ReportUiEvent.ShareFile(result.toString().toUri(), _reportFormat.value))
        }
    }

    private fun UiMeasurement.Type.toMeasurementClass(): KClass<out Measurement> = when (this) {
        UiMeasurement.Type.WEIGHT -> BodyWeight::class
        UiMeasurement.Type.HEART_RATE -> HeartRate::class
        UiMeasurement.Type.BLOOD_GLUCOSE -> BloodGlucose::class
        UiMeasurement.Type.BLOOD_PRESSURE -> BloodPressure::class
        UiMeasurement.Type.RESPIRATION_RATE -> RespirationRate::class
        UiMeasurement.Type.OXYGEN_SATURATION -> OxygenSaturation::class
    }

    private data class MeasurementQuery(
        val to: Instant,
        val from: Instant,
        val types: List<KClass<out Measurement>>,
    )
}

@Immutable
internal sealed interface ReportUiEvent {

    data class ShareFile(val uri: Uri, val format: ReportFormat) : ReportUiEvent
}

@Immutable
internal data class MeasurementGroup(
    val id: String,
    val date: LocalDate,
    val measurements: List<UiMeasurement>
)
