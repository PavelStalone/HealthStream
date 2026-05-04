package ru.health.stream.feature.home.impl.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.arttttt.nav3router.Router
import ru.health.stream.feature.home.api.navigation.HomeNavKey
import ru.health.stream.feature.home.impl.presentation.screen.HomeScreen
import ru.health.stream.feature.measurement.api.navigation.AddMeasurementNavKey
import ru.health.stream.feature.measurement.api.navigation.MeasurementNavKey
import ru.health.stream.feature.report.api.navigation.ReportNavKey

internal fun EntryProviderScope<NavKey>.homeEntry(router: Router<NavKey>) {
    entry<HomeNavKey> {
        HomeScreen(
            onReportIconClick = { router.push(ReportNavKey) },
            onAddMeasurementIconClick = { router.push(AddMeasurementNavKey()) },
            onMeasurementCardClick = { measurementType ->
                router.push(MeasurementNavKey(measurementType))
            }
        )
    }
}
