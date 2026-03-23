package ru.health.stream.feature.vitals.ui.component.input

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import ru.health.stream.core.ui.theme.HealthStreamTheme
import ru.health.stream.feature.vitals.data.model.EmptyMetadata
import ru.health.stream.feature.vitals.data.model.Resource
import ru.health.stream.feature.vitals.data.model.measurement.BloodPressure
import kotlin.uuid.Uuid

class BloodPressureComponent : InputTypeComponent {

    private val bloodPressureState = mutableStateOf(BloodPressureUi())

    override fun build(): Result<BloodPressure> = runCatching {
        val uiState = bloodPressureState.value
        val systolic =
            requireNotNull(uiState.systolic.toFloatOrNull()) { "Systolic pressure is not correct" }
        val diastolic =
            requireNotNull(uiState.diastolic.toFloatOrNull()) { "Diastolic pressure is not correct" }

        require(systolic > 0) { "Systolic pressure must be positive" }
        require(diastolic > 0) { "Diastolic pressure must be positive" }
        require(systolic > diastolic) { "Systolic must be greater than diastolic" }

        BloodPressure(
            id = Uuid.random().toString(),
            createdAt = Clock.System.now(),
            resource = Resource.Manual,
            metadata = EmptyMetadata,
            systolic = systolic,
            diastolic = diastolic,
        )
    }.onFailure { throwable ->
        bloodPressureState.value = bloodPressureState.value.copy(error = throwable.message)
    }

    fun changeSystolic(value: String) {
        bloodPressureState.value = bloodPressureState.value.copy(
            systolic = value,
            error = null
        )
    }

    fun changeDiastolic(value: String) {
        bloodPressureState.value = bloodPressureState.value.copy(
            diastolic = value,
            error = null
        )
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val state by retain(bloodPressureState) { bloodPressureState }

        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = state.systolic,
                shape = MaterialTheme.shapes.large,
                onValueChange = { value -> changeSystolic(value) },
                label = { Text("Systolic") },
                isError = state.error != null,
                suffix = {
                    Text(
                        text = "mmHg",
                        style = MaterialTheme.typography.bodyLarge,
                        color = LocalContentColor.current.copy(alpha = 0.6f)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                singleLine = true
            )

            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = state.diastolic,
                shape = MaterialTheme.shapes.large,
                onValueChange = { value -> changeDiastolic(value) },
                label = { Text("Diastolic") },
                isError = state.error != null,
                suffix = {
                    Text(
                        text = "mmHg",
                        style = MaterialTheme.typography.bodyLarge,
                        color = LocalContentColor.current.copy(alpha = 0.6f)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                singleLine = true
            )
        }

        state.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    @Immutable
    private data class BloodPressureUi(
        val systolic: String = "",
        val diastolic: String = "",
        val error: String? = null,
    )
}

@Preview(showBackground = true)
@Composable
private fun BloodPressureComponentPreview() {
    HealthStreamTheme {
        BloodPressureComponent().Content()
    }
}
