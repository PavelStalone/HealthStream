package ru.health.stream.feature.measurement.impl.presentation.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
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
import ru.health.stream.feature.chart.model.DrawableData
import ru.health.stream.feature.measurement.impl.presentation.model.UiPeriod
import ru.health.stream.feature.measurement.impl.presentation.model.asPeriod
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
internal class MeasurementViewModel @Inject constructor(
    measurementRepository: MeasurementRepository,
    groupMeasurementByPeriodUseCase: GroupMeasurementByPeriodUseCase,
) : ViewModel() {

    private val periodFlow: MutableStateFlow<UiPeriod> = MutableStateFlow(UiPeriod.Week)
    private val measurementTypeFlow: MutableStateFlow<KClass<out Measurement>> =
        MutableStateFlow(HeartRate::class)

    private val _expandedMeasurementsFlow = MutableStateFlow<Set<String>>(emptySet())
    val expandedMeasurementsFlow = _expandedMeasurementsFlow.asStateFlow()

    val convertedPeriodFlow = periodFlow.map { period ->
        period.asPeriod(firstDayOfWeek = DayOfWeek.MONDAY)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = periodFlow.value.asPeriod(firstDayOfWeek = DayOfWeek.MONDAY)
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val rangeFlow = convertedPeriodFlow.mapLatest { period ->
        period.calculateRange(
            date = Clock.System.now(),
            timeZone = TimeZone.currentSystemDefault(),
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val measurementFlow = combine(
        rangeFlow,
        measurementTypeFlow,
    ) { range, measurementType ->
        range to measurementType
    }
        .distinctUntilChanged()
        .flatMapLatest { (range, measurementType) ->
            measurementRepository.getMeasurementsFlowByRange(
                from = range.start,
                to = range.endInclusive,
                type = measurementType,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )

    val measurementChartStates = combine(
        rangeFlow,
        measurementFlow,
        convertedPeriodFlow,
    ) { range, measurements, period ->
        val groupPeriod = when (period) {
            is Period.Week -> Period.SixHour
            Period.Month -> Period.Day
            Period.Year -> Period.Month
            else -> Period.OneHour
        }

        MeasurementsChartState.Main(
            drawableData = DrawableData.create(
                period = groupPeriod,
                dateRange = range,
                measurements = measurements,
                timeZone = TimeZone.currentSystemDefault(),
                groupMeasurementByPeriodUseCase = groupMeasurementByPeriodUseCase,
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeout = 3.seconds),
        initialValue = MeasurementsChartState.Loading
    )

    val measurementsState = measurementFlow.map { measurements ->
        measurements.groupBy { it.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).date }
            .mapValues { (_, measurements) ->
                measurements.map { measurement -> measurement.asUi() }
            }
            .map { (date, measurements) ->
                MeasurementGroup(
                    date = date,
                    id = date.toString(),
                    measurements = measurements,
                )
            }
            .let { MeasurementsState.Main(it) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeout = 3.seconds),
        initialValue = MeasurementsState.Loading
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
}

@Immutable
internal sealed interface MeasurementsState {

    data object Loading : MeasurementsState

    data class Main(
        val measurements: List<MeasurementGroup>,
    ) : MeasurementsState
}

@Immutable
internal data class MeasurementGroup(
    val id: String,
    val date: LocalDate,
    val measurements: List<UiMeasurement>
)

@Immutable
internal sealed interface MeasurementsChartState {

    data object Loading : MeasurementsChartState

    data class Main(val drawableData: DrawableData) : MeasurementsChartState
}
