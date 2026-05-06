package ru.health.stream.feature.measurement.impl.presentation.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.health.stream.core.ui.model.UiMeasurement
import ru.health.stream.core.ui.model.asUi
import ru.health.stream.data.vitals.model.EmptyMetadata
import ru.health.stream.data.vitals.model.Metadata
import ru.health.stream.data.vitals.model.Note
import ru.health.stream.data.vitals.model.measurement.BloodGlucose
import ru.health.stream.data.vitals.model.measurement.BloodPressure
import ru.health.stream.data.vitals.model.measurement.BodyWeight
import ru.health.stream.data.vitals.model.measurement.HeartRate
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.data.vitals.model.measurement.OxygenSaturation
import ru.health.stream.data.vitals.model.measurement.RespirationRate
import ru.health.stream.data.vitals.model.measurement.copy
import ru.health.stream.data.vitals.usecase.CreateMeasurementUseCase
import ru.health.stream.feature.measurement.impl.presentation.component.input.BloodGlucoseComponent
import ru.health.stream.feature.measurement.impl.presentation.component.input.BloodPressureComponent
import ru.health.stream.feature.measurement.impl.presentation.component.input.BodyWeightComponent
import ru.health.stream.feature.measurement.impl.presentation.component.input.HeartRateComponent
import ru.health.stream.feature.measurement.impl.presentation.component.input.InputTypeComponent
import ru.health.stream.feature.measurement.impl.presentation.component.input.OxygenSaturationComponent
import ru.health.stream.feature.measurement.impl.presentation.component.input.RespirationRateComponent
import kotlin.reflect.KClass
import kotlin.uuid.Uuid

@HiltViewModel
internal class AddMeasurementViewModel @Inject constructor(
    private val createMeasurementUseCase: CreateMeasurementUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AddMeasurementUiState(
            inputTypeComponent = getInputComponent(HeartRate::class),
            selectedType = HeartRate::class.asUi(),
        )
    )
    val uiState = _uiState.asStateFlow()

    fun onTypeSelected(type: UiMeasurement.Type) {
        val state = _uiState.value

        _uiState.update {
            state.copy(
                selectedType = type,
                inputTypeComponent = getInputComponent(measurementType = type)
            )
        }
    }

    fun onTypeSelected(type: KClass<out Measurement>) = onTypeSelected(type.asUi())

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun updateMeasurement(measurement: Measurement) {
        val state = _uiState.value
        val type = measurement::class.asUi()
        val note = measurement[Note]?.description

        _uiState.update {
            state.copy(
                selectedType = type,
                inputTypeComponent = getInputComponent(
                    measurementType = type,
                    measurement = measurement,
                ),
                note = note ?: state.note
            )
        }
    }

    fun saveMeasurement(onSuccess: () -> Unit) {
        val state = _uiState.value

        viewModelScope.launch {
            val measurement = createMeasurementFromState(state).getOrNull()

            if (measurement != null) {
                createMeasurementUseCase(measurement).onSuccess {
                    onSuccess()
                }
            }
        }
    }

    private fun getInputComponent(
        measurementType: UiMeasurement.Type,
        measurement: Measurement? = null,
    ): InputTypeComponent = when (measurementType) {
        UiMeasurement.Type.WEIGHT -> BodyWeightComponent(measurement as? BodyWeight)
        UiMeasurement.Type.HEART_RATE -> HeartRateComponent(measurement as? HeartRate)
        UiMeasurement.Type.BLOOD_GLUCOSE -> BloodGlucoseComponent(measurement as? BloodGlucose)
        UiMeasurement.Type.BLOOD_PRESSURE -> BloodPressureComponent(measurement as? BloodPressure)
        UiMeasurement.Type.RESPIRATION_RATE -> RespirationRateComponent(measurement as? RespirationRate)
        UiMeasurement.Type.OXYGEN_SATURATION -> OxygenSaturationComponent(measurement as? OxygenSaturation)
    }

    private fun getInputComponent(
        measurementType: KClass<out Measurement>
    ): InputTypeComponent = getInputComponent(measurementType.asUi())

    private fun createMeasurementFromState(
        state: AddMeasurementUiState,
    ): Result<Measurement> = runCatching {
        val healthMeasurement = state.inputTypeComponent.build().getOrThrow()

        val metadata: Metadata = if (state.note.isNotBlank()) {
            Note(
                id = Uuid.random().toString(),
                createdAt = healthMeasurement.createdAt,
                description = state.note,
            )
        } else {
            EmptyMetadata
        }

        healthMeasurement.copy(metadata = metadata)
    }
}

@Immutable
internal data class AddMeasurementUiState(
    val selectedType: UiMeasurement.Type = UiMeasurement.Type.HEART_RATE,
    val inputTypeComponent: InputTypeComponent = HeartRateComponent(),
    val note: String = "",
)
