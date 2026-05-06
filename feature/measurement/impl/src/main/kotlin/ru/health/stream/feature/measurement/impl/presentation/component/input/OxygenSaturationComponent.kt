package ru.health.stream.feature.measurement.impl.presentation.component.input

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.datetime.Clock
import ru.health.stream.core.ui.theme.HealthStreamTheme
import ru.health.stream.data.vitals.model.EmptyMetadata
import ru.health.stream.data.vitals.model.Resource
import ru.health.stream.data.vitals.model.measurement.OxygenSaturation
import kotlin.uuid.Uuid

class OxygenSaturationComponent(
    private val measurement: OxygenSaturation? = null
) : InputTypeComponent {

    private val saturationState = mutableStateOf(
        OxygenSaturationUi(
            saturation = measurement?.saturation?.toString() ?: ""
        )
    )

    override fun build(): Result<OxygenSaturation> = runCatching {
        val uiState = saturationState.value
        val saturation =
            requireNotNull(uiState.saturation.toFloatOrNull()) { "Saturation is not correct" }

        require(saturation in 0f..100f) { "Oxygen saturation must be between 0 and 100" }

        OxygenSaturation(
            id = measurement?.id ?: Uuid.random().toString(),
            createdAt = measurement?.createdAt ?: Clock.System.now(),
            resource = measurement?.resource ?: Resource.Manual,
            metadata = EmptyMetadata,
            saturation = saturation,
        )
    }.onFailure { throwable ->
        saturationState.value = saturationState.value.copy(error = throwable.message)
    }

    fun changeSaturation(value: String) {
        saturationState.value = saturationState.value.copy(
            saturation = value,
            error = null
        )
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val state by retain(saturationState) { saturationState }

        OutlinedTextField(
            modifier = modifier,
            value = state.saturation,
            shape = MaterialTheme.shapes.large,
            onValueChange = { value -> changeSaturation(value) },
            label = { Text(text = "SpO₂") },
            isError = state.error != null,
            supportingText = state.error?.let { error ->
                { Text(text = error) }
            },
            suffix = {
                Text(
                    text = "%",
                    style = MaterialTheme.typography.bodyLarge,
                    color = LocalContentColor.current.copy(alpha = 0.6f)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            singleLine = true
        )
    }

    @Immutable
    private data class OxygenSaturationUi(
        val saturation: String = "",
        val error: String? = null,
    )
}

@Preview(showBackground = true)
@Composable
private fun OxygenSaturationComponentPreview() {
    HealthStreamTheme {
        OxygenSaturationComponent().Content()
    }
}
