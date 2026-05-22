package ru.health.stream.feature.measurement.impl.presentation.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ru.health.stream.core.ui.model.UiMeasurement
import ru.health.stream.core.ui.model.asUi
import ru.health.stream.data.vitals.model.Period
import ru.health.stream.data.vitals.model.measurement.HeartRate
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.data.vitals.repository.MeasurementRepository
import ru.health.stream.data.vitals.usecase.GroupMeasurementByPeriodUseCase
import ru.health.stream.core.chart.model.DrawableData
import ru.health.stream.feature.measurement.impl.presentation.model.UiPeriod
import ru.health.stream.feature.measurement.impl.presentation.model.asPeriod
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
internal class MeasurementViewModel @Inject constructor(
    private val measurementRepository: MeasurementRepository,
    groupMeasurementByPeriodUseCase: GroupMeasurementByPeriodUseCase,
) : ViewModel() {

    private val periodFlow = MutableStateFlow<UiPeriod>(UiPeriod.Week)
    private val measurementTypeFlow = MutableStateFlow<KClass<out Measurement>>(HeartRate::class)

    private val _expandedMeasurementsFlow = MutableStateFlow<Set<String>>(emptySet())
    val expandedMeasurementsFlow = _expandedMeasurementsFlow.asStateFlow()

    private val queryFlow = combine(
        periodFlow,
        measurementTypeFlow,
    ) { uiPeriod, type ->
        val period = uiPeriod.asPeriod(firstDayOfWeek = DayOfWeek.MONDAY)
        val range = period.calculateRange(
            date = Clock.System.now(),
            timeZone = TimeZone.currentSystemDefault(),
        )

        Query(
            period = period,
            uiPeriod = uiPeriod,
            range = range,
            type = type
        )
    }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val measurementFlow = queryFlow
        .flatMapLatest { query ->
            measurementRepository.getMeasurementsFlowByRange(
                from = query.range.start,
                to = query.range.endInclusive,
                type = query.type,
            )
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val mainMeasurementsStateFlow = measurementFlow.mapLatest { measurements ->
        val query = queryFlow.first()
        val timeZone = TimeZone.currentSystemDefault()
        val groupPeriod = when (query.period) {
            is Period.Week -> Period.SixHour
            Period.Month -> Period.Day
            Period.Year -> Period.Month
            else -> Period.OneHour
        }

        val drawableData = DrawableData.create(
            timeZone = timeZone,
            period = groupPeriod,
            dateRange = query.range,
            measurements = measurements,
            groupMeasurementByPeriodUseCase = groupMeasurementByPeriodUseCase,
        )
        val measurementGroups = measurements.groupBy { measurement ->
            measurement.createdAt.toLocalDateTime(timeZone).date
        }.map { (date, measurements) ->
            MeasurementGroup(
                date = date,
                id = date.toString(),
                measurements = measurements.map { measurement -> measurement.asUi() },
            )
        }

        if (measurementGroups.isEmpty()) {
            MeasurementsState.Empty
        } else {
            MeasurementsState.Main(
                drawableData = drawableData,
                measurements = measurementGroups,
            )
        }
    }

    val measurementStateFlow = callbackFlow {
        launch {
            queryFlow.collect {
                send(MeasurementsState.Loading)
            }
        }
        launch {
            mainMeasurementsStateFlow.collectLatest { state ->
                send(state)
            }
        }

        awaitClose()
    }.stateIn(
        scope = viewModelScope,
        initialValue = MeasurementsState.Loading,
        started = SharingStarted.WhileSubscribed(3.seconds),
    )

    fun expandMeasurement(id: String) {
        if (_expandedMeasurementsFlow.value.contains(id)) {
            _expandedMeasurementsFlow.value -= id
        } else {
            _expandedMeasurementsFlow.value += id
        }
    }

    fun changePeriod(period: UiPeriod) {
        periodFlow.value = period
    }

    fun changeMeasurementType(measurementType: KClass<out Measurement>) {
        measurementTypeFlow.value = measurementType
    }

    fun editMeasurement(uiMeasurement: UiMeasurement, onEdit: (Measurement) -> Unit) {
        viewModelScope.launch {
            runCatching {
                val measurement = measurementFlow.first()
                    .first { measurement -> measurement.id == uiMeasurement.id }

                onEdit(measurement)
            }
        }
    }

    fun deleteMeasurement(uiMeasurement: UiMeasurement) {
        viewModelScope.launch {
            runCatching {
                val measurement = measurementFlow.first()
                    .first { measurement -> measurement.id == uiMeasurement.id }

                measurementRepository.deleteMeasurement(measurement)
            }
        }
    }

    private data class Query(
        val period: Period,
        val uiPeriod: UiPeriod,
        val range: ClosedRange<Instant>,
        val type: KClass<out Measurement>,
    )
}

@Immutable
internal sealed interface MeasurementsState {

    data object Empty : MeasurementsState

    data object Loading : MeasurementsState

    data class Main(
        val drawableData: DrawableData,
        val measurements: List<MeasurementGroup>,
    ) : MeasurementsState
}

@Immutable
internal data class MeasurementGroup(
    val id: String,
    val date: LocalDate,
    val measurements: List<UiMeasurement>
)
