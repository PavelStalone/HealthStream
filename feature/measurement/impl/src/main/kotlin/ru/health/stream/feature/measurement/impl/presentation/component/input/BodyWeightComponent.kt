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
import ru.health.stream.data.vitals.model.kg
import ru.health.stream.data.vitals.model.measurement.BodyWeight
import kotlin.uuid.Uuid

class BodyWeightComponent(private val measurement: BodyWeight? = null) : InputTypeComponent {

    private val weightState = mutableStateOf(
        BodyWeightUi(
            weight = measurement?.weight?.kg?.toString() ?: ""
        )
    )

    override fun build(): Result<BodyWeight> = runCatching {
        val uiState = weightState.value
        val weightValue = requireNotNull(uiState.weight.toFloatOrNull()) { "Weight is not correct" }

        require(weightValue > 0) { "Weight must be positive" }

        BodyWeight(
            id = measurement?.id ?: Uuid.random().toString(),
            createdAt = measurement?.createdAt ?: Clock.System.now(),
            resource = measurement?.resource ?: Resource.Manual,
            metadata = EmptyMetadata,
            weight = weightValue.kg,
        )
    }.onFailure { throwable ->
        weightState.value = weightState.value.copy(error = throwable.message)
    }

    fun changeWeight(value: String) {
        weightState.value = weightState.value.copy(
            weight = value,
            error = null
        )
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val state by retain(weightState) { weightState }

        OutlinedTextField(
            modifier = modifier,
            value = state.weight,
            shape = MaterialTheme.shapes.large,
            onValueChange = { value -> changeWeight(value) },
            label = { Text(text = "Вес тела") },
            isError = state.error != null,
            supportingText = state.error?.let { error ->
                { Text(text = error) }
            },
            suffix = {
                Text(
                    text = "кг",
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
    private data class BodyWeightUi(
        val weight: String = "",
        val error: String? = null,
    )
}

@Preview(showBackground = true)
@Composable
private fun BodyWeightComponentPreview() {
    HealthStreamTheme {
        BodyWeightComponent().Content()
    }
}
