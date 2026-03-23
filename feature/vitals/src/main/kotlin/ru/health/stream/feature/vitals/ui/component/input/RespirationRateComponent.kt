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
import ru.health.stream.feature.vitals.data.model.measurement.RespirationRate
import kotlin.uuid.Uuid

class RespirationRateComponent : InputTypeComponent {

    private val respirationRateState = mutableStateOf(RespirationRateUi())

    override fun build(): Result<RespirationRate> = runCatching {
        val uiState = respirationRateState.value
        val rate =
            requireNotNull(uiState.rate.toDoubleOrNull()) { "Respiration rate is not correct" }

        require(rate >= 0) { "Respiration rate cannot be negative" }

        RespirationRate(
            id = Uuid.random().toString(),
            createdAt = Clock.System.now(),
            resource = Resource.Manual,
            metadata = EmptyMetadata,
            rate = rate,
        )
    }.onFailure { throwable ->
        respirationRateState.value = respirationRateState.value.copy(error = throwable.message)
    }

    fun changeRate(value: String) {
        respirationRateState.value = respirationRateState.value.copy(
            rate = value,
            error = null
        )
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val state by retain(respirationRateState) { respirationRateState }

        OutlinedTextField(
            modifier = modifier,
            value = state.rate,
            shape = MaterialTheme.shapes.large,
            onValueChange = { value -> changeRate(value) },
            label = { Text("Respiration Rate") },
            isError = state.error != null,
            supportingText = state.error?.let { error ->
                { Text(text = error) }
            },
            suffix = {
                Text(
                    text = "rpm",
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
    private data class RespirationRateUi(
        val rate: String = "",
        val error: String? = null,
    )
}

@Preview(showBackground = true)
@Composable
private fun RespirationRateComponentPreview() {
    HealthStreamTheme {
        RespirationRateComponent().Content()
    }
}
