package ru.health.stream.feature.vitals.ui.component.input

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.health.stream.feature.vitals.data.model.measurement.HealthMeasurement

interface InputTypeComponent {

    fun build(): Result<HealthMeasurement>

    @Composable
    fun Content(modifier: Modifier = Modifier)
}
