package ru.health.stream.core.store.healthconnect.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import ru.health.stream.core.navigation.LocalRouter
import ru.health.stream.core.store.healthconnect.navigation.HealthConnectSettings
import ru.health.stream.core.ui.icon.Icons
import ru.health.stream.core.ui.icon.default.AccountCircle
import ru.health.stream.feature.settings.Settings

internal data object SettingsCell : Settings.Cell {

    override val key: String = "HealthStreamSettingsCell"
    override val priority: Int = 0
    override val isEnabled: Flow<Boolean> = flowOf(true)

    @Composable
    override fun Content() {
        val router = LocalRouter.current

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable(onClick = {
                    router.push(HealthConnectSettings)
                })
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "HealthConnect"
            )
        }
    }
}
