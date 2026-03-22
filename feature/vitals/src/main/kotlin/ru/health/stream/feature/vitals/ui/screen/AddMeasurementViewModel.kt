package ru.health.stream.feature.vitals.ui.screen

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.health.stream.feature.vitals.data.model.EmptyMetadata
import ru.health.stream.feature.vitals.data.model.Metadata
import ru.health.stream.feature.vitals.data.model.Note
import ru.health.stream.feature.vitals.data.model.measurement.BloodGlucose
import ru.health.stream.feature.vitals.data.model.measurement.BloodPressure
import ru.health.stream.feature.vitals.data.model.measurement.BodyWeight
import ru.health.stream.feature.vitals.data.model.measurement.HealthMeasurement
import ru.health.stream.feature.vitals.data.model.measurement.HeartRate
import ru.health.stream.feature.vitals.data.model.measurement.OxygenSaturation
import ru.health.stream.feature.vitals.data.model.measurement.RespirationRate
import ru.health.stream.feature.vitals.data.model.measurement.copy
import ru.health.stream.feature.vitals.data.repository.MeasurementRepository
import ru.health.stream.feature.vitals.ui.component.input.BloodGlucoseComponent
import ru.health.stream.feature.vitals.ui.component.input.BloodPressureComponent
import ru.health.stream.feature.vitals.ui.component.input.BodyWeightComponent
import ru.health.stream.feature.vitals.ui.component.input.HeartRateComponent
import ru.health.stream.feature.vitals.ui.component.input.InputTypeComponent
import ru.health.stream.feature.vitals.ui.component.input.OxygenSaturationComponent
import ru.health.stream.feature.vitals.ui.component.input.RespirationRateComponent
import kotlin.reflect.KClass
import kotlin.uuid.Uuid

@HiltViewModel(assistedFactory = AddMeasurementViewModel.Factory::class)
internal class AddMeasurementViewModel @AssistedInject constructor(
    @Assisted private val measurementType: KClass<out HealthMeasurement>,
    private val measurementRepository: MeasurementRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AddMeasurementUiState(
            inputTypeComponent = getInputComponent(measurementType),
            selectedType = getMeasurementTypeUi(measurementType),
        )
    )
    val uiState = _uiState.asStateFlow()

    fun onTypeSelected(type: MeasurementType) {
        val state = _uiState.value

        if (state.selectedType != type) {
            _uiState.update {
                state.copy(
                    selectedType = type,
                    inputTypeComponent = getInputComponent(type)
                )
            }
        }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun saveMeasurement(onSuccess: () -> Unit) {
        val state = _uiState.value

        viewModelScope.launch {
            val measurement = createMeasurementFromState(state).getOrNull()

            if (measurement != null) {
                measurementRepository.createMeasurement(measurement)
                onSuccess()
            }
        }
    }

    private fun getInputComponent(
        measurementType: KClass<out HealthMeasurement>
    ): InputTypeComponent = when (measurementType) {
        HeartRate::class -> HeartRateComponent()
        BloodPressure::class -> BloodPressureComponent()
        BloodGlucose::class -> BloodGlucoseComponent()
        BodyWeight::class -> BodyWeightComponent()
        OxygenSaturation::class -> OxygenSaturationComponent()
        RespirationRate::class -> RespirationRateComponent()
        else -> HeartRateComponent()
    }

    private fun getInputComponent(
        measurementType: MeasurementType
    ): InputTypeComponent = when (measurementType) {
        MeasurementType.HEART_RATE -> HeartRateComponent()
        MeasurementType.BLOOD_PRESSURE -> BloodPressureComponent()
        MeasurementType.OXYGEN_SATURATION -> OxygenSaturationComponent()
        MeasurementType.BODY_WEIGHT -> BodyWeightComponent()
        MeasurementType.BLOOD_GLUCOSE -> BloodGlucoseComponent()
        MeasurementType.RESPIRATION_RATE -> RespirationRateComponent()
    }

    private fun getMeasurementTypeUi(
        measurementType: KClass<out HealthMeasurement>
    ): MeasurementType = when (measurementType) {
        HeartRate::class -> MeasurementType.HEART_RATE
        BloodPressure::class -> MeasurementType.BLOOD_PRESSURE
        BloodGlucose::class -> MeasurementType.BLOOD_GLUCOSE
        BodyWeight::class -> MeasurementType.BODY_WEIGHT
        OxygenSaturation::class -> MeasurementType.OXYGEN_SATURATION
        RespirationRate::class -> MeasurementType.RESPIRATION_RATE
        else -> MeasurementType.HEART_RATE
    }

    private fun createMeasurementFromState(
        state: AddMeasurementUiState,
    ): Result<HealthMeasurement> = runCatching {
        val healthMeasurement = state.inputTypeComponent.build().getOrThrow()

        val metadata: Metadata = if (state.note.isNotBlank()) {
            Note(
                id = Uuid.random().toString(),
                createdAt = healthMeasurement.createdAt,
                description = state.note,
                measurementsId = listOf(healthMeasurement.id)
            )
        } else {
            EmptyMetadata
        }

        healthMeasurement.copy(metadata = metadata)
    }

    @AssistedFactory
    interface Factory {

        fun create(measurementType: KClass<out HealthMeasurement>): AddMeasurementViewModel
    }
}

@Immutable
internal data class AddMeasurementUiState(
    val selectedType: MeasurementType = MeasurementType.HEART_RATE,
    val inputTypeComponent: InputTypeComponent = HeartRateComponent(),
    val note: String = "",
)

internal enum class MeasurementType(val title: String) {
    HEART_RATE("Heart Rate"),
    BLOOD_PRESSURE("Blood Pressure"),
    OXYGEN_SATURATION("Oxygen Saturation"),
    BODY_WEIGHT("Body Weight"),
    BLOOD_GLUCOSE("Blood Glucose"),
    RESPIRATION_RATE("Respiration Rate")
}
