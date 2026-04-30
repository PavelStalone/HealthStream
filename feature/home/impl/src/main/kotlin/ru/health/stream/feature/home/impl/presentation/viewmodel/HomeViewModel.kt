package ru.health.stream.feature.home.impl.presentation.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import ru.health.stream.core.ui.icon.Icons
import ru.health.stream.core.ui.icon.default.Favorite
import ru.health.stream.core.ui.model.UiIcon
import ru.health.stream.core.ui.model.UiLevel
import ru.health.stream.core.ui.model.UiMeasurement
import ru.health.stream.core.ui.model.UiText
import ru.health.stream.core.ui.model.asUi
import ru.health.stream.data.vitals.model.Estimation
import ru.health.stream.data.vitals.model.Period
import ru.health.stream.data.vitals.model.measurement.BloodPressure
import ru.health.stream.data.vitals.model.measurement.HeartRate
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.data.vitals.repository.MeasurementRepository
import ru.health.stream.data.vitals.usecase.GroupMeasurementByPeriodUseCase
import ru.health.stream.feature.chart.core.Drawable
import ru.health.stream.feature.chart.model.ChartPosition
import ru.health.stream.feature.chart.model.DrawableData
import ru.health.stream.feature.home.impl.domain.DatePositionTransformer
import javax.inject.Inject
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class HomeViewModel @Inject constructor(
    measurementRepository: MeasurementRepository,
    groupMeasurementByPeriodUseCase: GroupMeasurementByPeriodUseCase,
) : ViewModel() {

    private val timeZone = TimeZone.currentSystemDefault()

    private val period = Period.Week(firstDayOfWeek = DayOfWeek.MONDAY)
    private val range =
        period.calculateRange(date = Clock.System.now(), timeZone = timeZone)

    val heartRateFlow = measurementRepository.getMeasurementsFlowByRange(
        from = range.start,
        to = range.endInclusive,
        type = HeartRate::class,
    ).map { heartRates ->
        WeekCardState(
            key = "HeartRate",
            measurementType = HeartRate::class,
            measurementUnit = UiText.NonTranslatable(value = "уд/мин"),
            measurementValue = heartRates.firstOrNull()?.pulse?.run {
                UiText.NonTranslatable(toString())
            },
            measurementTitle = UiMeasurement.Type.HEART_RATE.text,
            measurementIcon = UiIcon.Vector(imageVector = Icons.Default.Favorite),
            drawableData = DrawableData.create(
                dateRange = range,
                timeZone = timeZone,
                period = Period.SixHour,
                measurements = heartRates,
                groupMeasurementByPeriodUseCase = groupMeasurementByPeriodUseCase,
            ),
            estimationLevel = heartRates.firstOrNull()?.metadata[Estimation]?.asUi(),
        )
    }.shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeout = 5.seconds)
    )

    val bloodPressureFlow = measurementRepository.getMeasurementsFlowByRange(
        from = range.start,
        to = range.endInclusive,
        type = BloodPressure::class,
    ).map { bloodPressures ->
        WeekCardState(
            key = "BloodPressure",
            measurementType = BloodPressure::class,
            measurementUnit = UiText.NonTranslatable(value = "мм рт. ст."),
            measurementValue = bloodPressures.firstOrNull()?.let { bloodPressure ->
                UiText.NonTranslatable(value = bloodPressure.asUi().value)
            },
            measurementTitle = UiMeasurement.Type.BLOOD_PRESSURE.text,
            measurementIcon = UiIcon.Vector(imageVector = Icons.Default.Favorite),
            drawableData = DrawableData.create(
                dateRange = range,
                timeZone = timeZone,
                period = Period.SixHour,
                measurements = bloodPressures,
                groupMeasurementByPeriodUseCase = groupMeasurementByPeriodUseCase,
            ),
            estimationLevel = bloodPressures.firstOrNull()?.metadata[Estimation]?.asUi(),
        )
    }.shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeout = 5.seconds)
    )

    val weekCardStates = combine(
        // TODO: Bind by settings - shoplikpavel 2026-02-24
        heartRateFlow,
        bloodPressureFlow,
        transform = { states -> states.toList() },
    ).shareIn(
        replay = 1,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeout = 5.seconds),
    )

    private fun Estimation.asUi(): UiLevel = when (level) {
        Estimation.Level.LOW -> UiLevel.LOW
        Estimation.Level.NORMAL -> UiLevel.NORMAL
        Estimation.Level.HIGH -> UiLevel.HIGH
        Estimation.Level.EXTRA_HIGH -> UiLevel.EXTRA_HIGH
    }
}

@Immutable
data class WeekCardState(
    val key: String,
    val measurementType: KClass<out Measurement>,
    val measurementUnit: UiText,
    val measurementValue: UiText.NonTranslatable?,
    val measurementTitle: UiText,
    val measurementIcon: UiIcon,
    val estimationLevel: UiLevel?,
    val drawableData: DrawableData,
)
