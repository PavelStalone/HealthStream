package ru.health.stream.feature.vitals.ui.component.input

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
import ru.health.stream.feature.vitals.data.model.EmptyMetadata
import ru.health.stream.feature.vitals.data.model.Resource
import ru.health.stream.feature.vitals.data.model.measurement.HeartRate
import kotlin.uuid.Uuid

class HeartRateComponent : InputTypeComponent {

    private val heartRateState = mutableStateOf(HeartRateUi())

    override fun build(): Result<HeartRate> = runCatching {
        val heartRateUi = heartRateState.value
        val pulse = requireNotNull(heartRateUi.pulse.toIntOrNull()) { "Input type is not correct" }

        require(pulse > 0) { "Pulse cannot be negative" }
        require(pulse < 220) { "Pulse cannot be greater than 220" }

        HeartRate(
            id = Uuid.random().toString(),
            createdAt = Clock.System.now(),
            resource = Resource.Manual,
            metadata = EmptyMetadata,
            pulse = pulse,
        )
    }.onFailure { throwable ->
        heartRateState.value = heartRateState.value.copy(error = throwable.message)
    }

    fun changePulse(value: String) {
        heartRateState.value = heartRateState.value.copy(
            pulse = value,
            error = null
        )
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val heartRate by retain(heartRateState) { heartRateState }

        OutlinedTextField(
            modifier = modifier,
            value = heartRate.pulse,
            shape = MaterialTheme.shapes.large,
            onValueChange = { value -> changePulse(value) },
            label = { Text(text = "Пульс") },
            isError = heartRate.error != null,
            supportingText = heartRate.error?.let { error ->
                { Text(text = error) }
            },
            suffix = {
                Text(
                    text = "уд/мин",
                    style = MaterialTheme.typography.bodyLarge,
                    color = LocalContentColor.current.copy(alpha = 0.6f)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            singleLine = true
        )
    }

    @Immutable
    private data class HeartRateUi(
        val pulse: String = "",
        val error: String? = null,
    )
}

@Preview(showBackground = true)
@Composable
private fun HeartRateComponentPreview() {
    HealthStreamTheme {
        HeartRateComponent().Content()
    }
}
