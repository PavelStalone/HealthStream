package ru.health.stream.feature.vitals.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import ru.health.stream.core.navigation.LocalRouter
import ru.health.stream.feature.vitals.data.navigation.AddMeasurementScreen
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

    entry<AddMeasurementScreen>(
        metadata = DialogSceneStrategy.dialog()
    ) { key ->
        val router = LocalRouter.current

        AddMeasurementContent(
            onClose = { router.pop() },
            measurementType = key.measurementType,
        )
    }
}
