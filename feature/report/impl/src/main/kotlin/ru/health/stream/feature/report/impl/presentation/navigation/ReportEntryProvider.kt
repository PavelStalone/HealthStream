package ru.health.stream.feature.report.impl.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.arttttt.nav3router.Router
import ru.health.stream.feature.measurement.api.navigation.AddMeasurementNavKey
import ru.health.stream.feature.report.api.navigation.ReportNavKey
import ru.health.stream.feature.report.impl.presentation.screen.ReportScreen

internal fun EntryProviderScope<NavKey>.reportEntry(router: Router<NavKey>) {
    entry<ReportNavKey> {
        ReportScreen(
            onBackClick = { router.pop() },
            onEditClick = { measurement ->
                router.push(
                    AddMeasurementNavKey(
                        measurementType = measurement::class,
                        measurement = measurement,
                    )
                )
            },
        )
    }
}
