package ru.health.stream.feature.measurement.impl.presentation.viewmodel

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

@HiltViewModel(assistedFactory = AddMeasurementViewModel.Factory::class)
internal class AddMeasurementViewModel @AssistedInject constructor(
    @Assisted private val measurementType: KClass<out Measurement>,
    private val createMeasurementUseCase: CreateMeasurementUseCase,
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
                    inputTypeComponent = getInputComponent(measurementType = type)
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
                createMeasurementUseCase(measurement).onSuccess {
                    onSuccess()
                }
            }
        }
    }

    private fun getInputComponent(
        measurementType: KClass<out Measurement>
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
        measurementType: KClass<out Measurement>
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

    @AssistedFactory
    interface Factory {

        fun create(measurementType: KClass<out Measurement>): AddMeasurementViewModel
    }
}

@Immutable
internal data class AddMeasurementUiState(
    val selectedType: MeasurementType = MeasurementType.HEART_RATE,
    val inputTypeComponent: InputTypeComponent = HeartRateComponent(),
    val note: String = "",
)

internal enum class MeasurementType(val title: String) {
    HEART_RATE(title = "Пульс"),
    BLOOD_PRESSURE(title = "Давление"),
    OXYGEN_SATURATION(title = "Сатурация кислорода"),
    BODY_WEIGHT(title = "Вес тела"),
    BLOOD_GLUCOSE(title = "Глюкоза в крови"),
    RESPIRATION_RATE(title = "Частота дыхания")
}
