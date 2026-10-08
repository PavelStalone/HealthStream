package ru.health.stream.feature.healthconnect.impl.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import ru.health.stream.core.ui.model.UiIcon
import ru.health.stream.core.ui.model.drawIcon
import ru.health.stream.feature.healthconnect.impl.R
import ru.health.stream.source.local.healthconnect.HealthConnectManager

@Composable
fun HealthConnectSettingsField(
    healthConnectManager: HealthConnectManager,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val checked by healthConnectManager.isActiveHealthConnect.collectAsStateWithLifecycle(false)
    var isEnabled by remember { mutableStateOf(healthConnectManager.isHealthConnectSupported()) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            UiIcon.Resource(resId = R.drawable.health_connect_logo).drawIcon(
                modifier = Modifier.size(48.dp),
                tint = Color.Unspecified
            )
            Column {
                Text("HealthConnect")
                Text(
                    text = "Синхронизация данных в системе Android",
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            enabled = isEnabled,
            onCheckedChange = {
                isEnabled = false

                coroutineScope.launch {
                    if (checked) {
                        healthConnectManager.disableHealthConnect()
                    } else {
                        healthConnectManager.enableHealthConnect()
                    }

                    isEnabled = true
                }
            }
        )
    }
}