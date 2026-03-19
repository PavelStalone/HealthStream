package ru.health.stream.feature.vitals.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import ru.health.stream.feature.vitals.data.navigation.MainVitalsScreen
import ru.health.stream.feature.vitals.data.navigation.MeasurementScreen

internal fun EntryProviderScope<NavKey>.featureEntryBuilder() {
    entry<MainVitalsScreen> {
        MainVitalsScreen()
    }

    entry<MeasurementScreen> { key ->
        MeasurementScreen(
            measurementType = key.measurementType,
            modifier = Modifier.fillMaxSize()
        )
    }
}
