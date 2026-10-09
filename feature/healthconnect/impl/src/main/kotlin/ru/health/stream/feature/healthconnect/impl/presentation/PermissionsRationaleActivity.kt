package ru.health.stream.feature.healthconnect.impl.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ru.health.stream.core.ui.theme.HealthStreamTheme
import ru.health.stream.feature.healthconnect.impl.presentation.screen.PermissionsRationaleScreen

class PermissionsRationaleActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            HealthStreamTheme {
                PermissionsRationaleScreen()
            }
        }
    }
}
