package ru.health.stream.feature.measurement.impl.presentation.component.input

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.health.stream.data.vitals.model.measurement.Measurement

interface InputTypeComponent {

    fun build(): Result<Measurement>

    @Composable
    fun Content(modifier: Modifier = Modifier)
}
