package ru.health.stream.feature.vitals.ui.screen

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
import ru.health.stream.core.ui.icon.Icons
import ru.health.stream.core.ui.icon.device.BPCuff
import ru.health.stream.core.ui.icon.device.Pencil
import ru.health.stream.core.ui.icon.device.PulseOximeter
import ru.health.stream.core.ui.icon.device.WeightScale
import ru.health.stream.core.ui.model.UiIcon
import ru.health.stream.core.ui.model.UiText
import ru.health.stream.feature.vitals.data.model.Device
import ru.health.stream.feature.vitals.data.model.Note
import ru.health.stream.feature.vitals.data.model.Resource
import ru.health.stream.feature.vitals.data.model.measurement.BloodGlucose
import ru.health.stream.feature.vitals.data.model.measurement.BloodPressure
import ru.health.stream.feature.vitals.data.model.measurement.BodyWeight
import ru.health.stream.feature.vitals.data.model.measurement.DiastolicPressure
import ru.health.stream.feature.vitals.data.model.measurement.HealthMeasurement
import ru.health.stream.feature.vitals.data.model.measurement.HeartRate
import ru.health.stream.feature.vitals.data.model.measurement.OxygenSaturation
import ru.health.stream.feature.vitals.data.model.measurement.RespirationRate
import ru.health.stream.feature.vitals.data.model.measurement.SystolicPressure
import ru.health.stream.feature.vitals.data.repository.MeasurementRepository
import ru.health.stream.feature.vitals.domain.DrawableData
import ru.health.stream.feature.vitals.domain.GroupMeasurementUseCase
import ru.health.stream.feature.vitals.ui.model.UiPeriod
import ru.health.stream.feature.vitals.ui.model.asPeriod
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.seconds

@HiltViewModel(assistedFactory = MeasurementViewModel.Factory::class)
internal class MeasurementViewModel @AssistedInject constructor(
    measurementRepository: MeasurementRepository,
    @Assisted private val period: UiPeriod,
    @Assisted private val measurementType: KClass<out HealthMeasurement>,
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
                measurements.map { measurement -> measurement.asUiMeasurement() }
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

    private fun HealthMeasurement.asUiMeasurement(): UiMeasurement {
        val (value, unit) = when (this) {
            is HeartRate -> pulse.toString() to "уд/мин"
            is BodyWeight -> weight.toString() to "кг"
            is OxygenSaturation -> saturation.toString() to "%"
            is BloodPressure -> "$systolic/$diastolic" to "мм рт. ст."
            is RespirationRate -> rate.toString() to "дых/мин"
            is BloodGlucose -> level.toInt().toString() to "ммоль/л"

            is SystolicPressure -> systolic.toString() to "мм рт. ст."
            is DiastolicPressure -> diastolic.toString() to "мм рт. ст."
        }

        val resourceTitle = when (val resource = resource) {
            is Device.BloodPressure -> UiText.NonTranslatable(value = "Тонометр")
            is Device.PulseOximeter -> UiText.NonTranslatable(value = "Пульсоксиметр")
            is Device.WeightScale -> UiText.NonTranslatable(value = "Весы")
            is Resource.App -> UiText.App(packageName = resource.packageName)
            Resource.Manual -> UiText.NonTranslatable(value = "Ручной ввод")
        }

        val resourceIcon = when (val resource = resource) {
            is Device.BloodPressure -> UiIcon.Vector(imageVector = Icons.Device.BPCuff)
            is Device.PulseOximeter -> UiIcon.Vector(imageVector = Icons.Device.PulseOximeter)
            is Device.WeightScale -> UiIcon.Vector(imageVector = Icons.Device.WeightScale)
            is Resource.App -> UiIcon.App(packageName = resource.packageName)
            Resource.Manual -> UiIcon.Vector(imageVector = Icons.Device.Pencil)
        }

        return UiMeasurement(
            id = id,
            createdAt = createdAt,
            resourceTitle = resourceTitle,
            resourceIcon = resourceIcon,
            title = UiText.NonTranslatable(
                value = this::class.simpleName ?: "Неизвестно"
            ), // TODO: Change text - shoplikpavel 2026-03-17
            value = UiText.NonTranslatable(value = value),
            unit = UiText.NonTranslatable(value = unit),
            note = metadata[Note]?.let { note -> UiText.NonTranslatable(value = note.description) }
        )
    }

    @AssistedFactory
    interface Factory {

        fun create(
            period: UiPeriod,
            measurementType: KClass<out HealthMeasurement>,
        ): MeasurementViewModel
    }
}

@Immutable
sealed interface MeasurementsState {

    data object Loading : MeasurementsState

    data class Main(
        val measurements: List<MeasurementGroup>,
    ) : MeasurementsState
}

@Immutable
data class MeasurementGroup(
    val id: String,
    val date: Instant,
    val measurements: List<UiMeasurement>
)

@Immutable
data class UiMeasurement(
    val id: String,
    val createdAt: Instant,
    val resourceTitle: UiText,
    val resourceIcon: UiIcon,
    val title: UiText,
    val value: UiText,
    val unit: UiText,
    val note: UiText?,
)

@Immutable
sealed interface MeasurementsChartState {

    data object Loading : MeasurementsChartState

    data class Main(val drawableData: DrawableData) : MeasurementsChartState
}
