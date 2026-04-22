package ru.health.stream.feature.measurement.impl.presentation.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import ru.health.stream.core.ui.model.UiIcon
import ru.health.stream.core.ui.model.UiLevel
import ru.health.stream.core.ui.model.UiMeasurement
import ru.health.stream.core.ui.model.UiText
import ru.health.stream.core.ui.model.asUi
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.data.vitals.repository.MeasurementRepository
import ru.health.stream.feature.measurement.impl.domain.DrawableData
import ru.health.stream.feature.measurement.impl.domain.GroupMeasurementUseCase
import ru.health.stream.feature.measurement.impl.presentation.model.UiPeriod
import ru.health.stream.feature.measurement.impl.presentation.model.asPeriod
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.seconds

@HiltViewModel(assistedFactory = MeasurementViewModel.Factory::class)
internal class MeasurementViewModel @AssistedInject constructor(
    measurementRepository: MeasurementRepository,
    @Assisted private val period: UiPeriod,
    @Assisted private val measurementType: KClass<out Measurement>,
) : ViewModel() {

    private val periodFlow = MutableStateFlow(period)
    private val measurementTypeFlow = MutableStateFlow(measurementType)

    private val _expandedMeasurementsFlow = MutableStateFlow<Set<String>>(emptySet())
    val expandedMeasurementsFlow = _expandedMeasurementsFlow.asStateFlow()

    val convertedPeriodFlow = periodFlow.map { period ->
        period.asPeriod(firstDayOfWeek = DayOfWeek.MONDAY)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = period.asPeriod(firstDayOfWeek = DayOfWeek.MONDAY)
    )

    private val measurementFlow = combine(
        convertedPeriodFlow,
        measurementTypeFlow,
    ) { period, measurementType ->
        val range = period.calculateRange(
            date = Clock.System.now(),
            timeZone = TimeZone.currentSystemDefault()
        )

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
        measurementFlow,
        convertedPeriodFlow,
    ) { measurements, period ->
        MeasurementsChartState.Main(
            drawableData = DrawableData.create(
                period = period,
                measurements = measurements,
                dateNow = Clock.System.now(),
                coroutineScope = viewModelScope,
                timeZone = TimeZone.currentSystemDefault(),
                groupMeasurementUseCase = GroupMeasurementUseCase(),
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
                    id = date.toString(),
                    date = date.atStartOfDayIn(TimeZone.currentSystemDefault()),
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

    @AssistedFactory
    interface Factory {

        fun create(
            period: UiPeriod,
            measurementType: KClass<out Measurement>,
        ): MeasurementViewModel
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
    val date: Instant,
    val measurements: List<UiMeasurement>
)

@Immutable
internal sealed interface MeasurementsChartState {

    data object Loading : MeasurementsChartState

    data class Main(val drawableData: DrawableData) : MeasurementsChartState
}
