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
import ru.health.stream.core.ui.model.UiText
import ru.health.stream.data.vitals.model.Period
import ru.health.stream.data.vitals.model.measurement.HeartRate
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.data.vitals.repository.MeasurementRepository
import ru.health.stream.feature.chart.model.ChartPosition
import ru.health.stream.feature.home.impl.domain.DatePositionTransformer
import javax.inject.Inject
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class HomeViewModel @Inject constructor(
    measurementRepository: MeasurementRepository,
) : ViewModel() {

    private val positionTransformer = DatePositionTransformer(
        timeZone = TimeZone.currentSystemDefault(),
        dateNow = Clock.System.now(),
        period = Period.Week(firstDayOfWeek = DayOfWeek.MONDAY)
    )

    private val period = Period.Week(firstDayOfWeek = DayOfWeek.MONDAY)
    private val range =
        period.calculateRange(date = Clock.System.now(), timeZone = TimeZone.currentSystemDefault())

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
            measurementTitle = UiText.NonTranslatable(value = "Пульс"),
            measurementIcon = UiIcon.Vector(imageVector = Icons.Default.Favorite),
            points = heartRates.map { heartRate ->
                ChartPosition.Point(
                    x = positionTransformer.transform(date = heartRate.createdAt),
                    y = heartRate.pulse.toFloat(),
                )
            }
        )
    }.shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeout = 5.seconds)
    )

    val weekCardStates = combine(
        // TODO: Bind by settings - shoplikpavel 2026-02-24
        heartRateFlow,
        transform = { states -> states.toList() },
    ).shareIn(
        replay = 1,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeout = 5.seconds),
    )
}

@Immutable
data class WeekCardState(
    val key: String,
    val measurementType: KClass<out Measurement>,
    val measurementUnit: UiText,
    val measurementValue: UiText.NonTranslatable?,
    val measurementTitle: UiText,
    val measurementIcon: UiIcon,
    val points: List<ChartPosition.Point>,
)
