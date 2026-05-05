package ru.health.stream.feature.measurement.impl.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.arttttt.nav3router.Router
import ru.health.stream.feature.measurement.api.navigation.AddMeasurementNavKey
import ru.health.stream.feature.measurement.api.navigation.MeasurementNavKey
import ru.health.stream.feature.measurement.impl.presentation.screen.AddMeasurementContent
import ru.health.stream.feature.measurement.impl.presentation.screen.MeasurementScreen

internal fun EntryProviderScope<NavKey>.measurementEntry(router: Router<NavKey>) {

    entry<MeasurementNavKey> { key ->
        MeasurementScreen(
            modifier = Modifier.fillMaxSize(),
            onBackClick = { router.pop() },
            onEditClick = { measurement ->
                router.push(
                    AddMeasurementNavKey(
                        measurementType = measurement::class,
                        measurement = measurement,
                    )
                )
            },
            measurementType = key.measurementType,
            addMeasurementClick = { measurementType ->
                router.push(AddMeasurementNavKey(measurementType = measurementType))
            },
        )
    }

    entry<AddMeasurementNavKey> { key ->
        AddMeasurementContent(
            modifier = Modifier.fillMaxSize(),
            onClose = { router.pop() },
            measurement = key.measurement,
            measurementType = key.measurementType,
        )
    }
}
