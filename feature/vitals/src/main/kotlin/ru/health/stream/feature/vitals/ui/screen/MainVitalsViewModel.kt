package ru.health.stream.feature.vitals.ui.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
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
import ru.health.stream.core.ui.model.UiIcon
import ru.health.stream.core.ui.model.UiText
import ru.health.stream.feature.chart.model.ChartPosition
import ru.health.stream.feature.vitals.data.model.HeartRate
import ru.health.stream.feature.vitals.data.repository.MeasurementRepository
import ru.health.stream.feature.vitals.domain.DatePositionTransformer
import ru.health.stream.feature.vitals.domain.Period
import javax.inject.Inject
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class MainVitalsViewModel @Inject constructor(
    measurementRepository: MeasurementRepository,
) : ViewModel() {

    private val positionTransformer = DatePositionTransformer(
        timeZone = TimeZone.currentSystemDefault(),
        dateNow = Clock.System.now(),
        period = Period.Week(firstDayOfWeek = DayOfWeek.MONDAY)
    )

    val heartRateFlow = measurementRepository.getMeasurementsFlowByDuration(
        duration = 7.days,
        type = HeartRate.WithResource::class,
    ).map { heartRates ->
        WeekCardState(
            measurementUnit = UiText.NonTranslatable("bpm"),
            measurementValue = heartRates.firstOrNull()?.pulse?.run {
                UiText.NonTranslatable(toString())
            },
            measurementTitle = UiText.NonTranslatable("Pulse"),
            measurementIcon = UiIcon.Vector(Icons.Rounded.FavoriteBorder),
            points = heartRates.map { heartRate ->
                ChartPosition.Point(
                    x = positionTransformer.transform(heartRate.createdAt),
                    y = heartRate.pulse.toFloat(),
                    z = 0f
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
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeout = 5.seconds)
    )
}

@Immutable
data class WeekCardState(
    val measurementUnit: UiText,
    val measurementValue: UiText.NonTranslatable?,
    val measurementTitle: UiText,
    val measurementIcon: UiIcon,
    val points: List<ChartPosition.Point>,
)
