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
import ru.health.stream.feature.vitals.data.model.measurement.BloodGlucose
import kotlin.uuid.Uuid

class BloodGlucoseComponent : InputTypeComponent {

    private val glucoseState = mutableStateOf(BloodGlucoseUi())

    override fun build(): Result<BloodGlucose> = runCatching {
        val uiState = glucoseState.value
        val level =
            requireNotNull(uiState.level.toDoubleOrNull()) { "Glucose level is not correct" }

        require(level >= 0) { "Glucose level cannot be negative" }

        BloodGlucose(
            id = Uuid.random().toString(),
            createdAt = Clock.System.now(),
            resource = Resource.Manual,
            metadata = EmptyMetadata,
            level = level,
        )
    }.onFailure { throwable ->
        glucoseState.value = glucoseState.value.copy(error = throwable.message)
    }

    fun changeLevel(value: String) {
        glucoseState.value = glucoseState.value.copy(
            level = value,
            error = null
        )
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val state by retain(glucoseState) { glucoseState }

        OutlinedTextField(
            modifier = modifier,
            value = state.level,
            shape = MaterialTheme.shapes.large,
            onValueChange = { value -> changeLevel(value) },
            label = { Text("Blood Glucose") },
            isError = state.error != null,
            supportingText = state.error?.let { error ->
                { Text(text = error) }
            },
            suffix = {
                Text(
                    text = "mmol/L",
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
    private data class BloodGlucoseUi(
        val level: String = "",
        val error: String? = null,
    )
}

@Preview(showBackground = true)
@Composable
private fun BloodGlucoseComponentPreview() {
    HealthStreamTheme {
        BloodGlucoseComponent().Content()
    }
}
