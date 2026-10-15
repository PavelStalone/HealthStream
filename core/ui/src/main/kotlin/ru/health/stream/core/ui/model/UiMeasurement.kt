package ru.health.stream.core.ui.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant
import ru.health.stream.core.ui.R
import ru.health.stream.core.ui.icon.Icons
import ru.health.stream.core.ui.icon.default.Blood
import ru.health.stream.core.ui.icon.default.Favorite
import ru.health.stream.core.ui.icon.default.Spo2
import ru.health.stream.core.ui.icon.default.Weight
import ru.health.stream.core.ui.icon.device.BPCuff
import ru.health.stream.core.ui.icon.device.Glucose
import ru.health.stream.core.ui.icon.device.Pencil
import ru.health.stream.core.ui.icon.device.PulseOximeter
import ru.health.stream.core.ui.icon.device.WeightScale
import ru.health.stream.data.vitals.model.Device
import ru.health.stream.data.vitals.model.Estimation
import ru.health.stream.data.vitals.model.Note
import ru.health.stream.data.vitals.model.Resource
import ru.health.stream.data.vitals.model.measurement.BloodGlucose
import ru.health.stream.data.vitals.model.measurement.BloodPressure
import ru.health.stream.data.vitals.model.measurement.BodyWeight
import ru.health.stream.data.vitals.model.measurement.DiastolicPressure
import ru.health.stream.data.vitals.model.measurement.HeartRate
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.data.vitals.model.measurement.OxygenSaturation
import ru.health.stream.data.vitals.model.measurement.SystolicPressure
import kotlin.reflect.KClass

@Immutable
data class UiMeasurement(
    val id: String,
    val type: Type,
    val unit: UiText,
    val time: Instant,
    val value: String,
    val resource: Resource,
    val note: UiText? = null,
    val estimation: UiLevel? = null,
) {

    sealed class Resource(
        open val text: UiText,
        open val icon: UiIcon,
    ) {

        data class App(
            override val text: UiText,
            override val icon: UiIcon,
        ) : Resource(
            text = text,
            icon = icon,
        )

        data object Manual : Resource(
            text = UiText.Translatable(R.string.resource_manual),
            icon = UiIcon.Vector(imageVector = Icons.Device.Pencil),
        )

        data object PulseOximeter : Resource(
            text = UiText.Translatable(R.string.resource_pulse_oximeter),
            icon = UiIcon.Vector(imageVector = Icons.Device.PulseOximeter),
        )

        data object WeightScale : Resource(
            text = UiText.Translatable(R.string.resource_weight_scale),
            icon = UiIcon.Vector(imageVector = Icons.Device.WeightScale),
        )

        data object BPCuff : Resource(
            text = UiText.Translatable(R.string.resource_bp_cuff),
            icon = UiIcon.Vector(imageVector = Icons.Device.BPCuff),
        )

        data object Glucose : Resource(
            text = UiText.Translatable(R.string.resource_glucose_meter),
            icon = UiIcon.Vector(imageVector = Icons.Device.Glucose),
        )
    }

    enum class Type(
        val text: UiText,
        val icon: UiIcon,
        val unit: UiText,
    ) {

        WEIGHT(
            text = UiText.Translatable(R.string.measurement_type_weight),
            icon = UiIcon.Vector(imageVector = Icons.Default.Weight),
            unit = UiText.Translatable(R.string.unit_kg),
        ),
        BLOOD_GLUCOSE(
            text = UiText.Translatable(R.string.measurement_type_glucose),
            icon = UiIcon.Vector(imageVector = Icons.Device.Glucose),
            unit = UiText.Translatable(R.string.unit_mmol_l),
        ),
        HEART_RATE(
            text = UiText.Translatable(R.string.measurement_type_heart_rate),
            icon = UiIcon.Vector(imageVector = Icons.Default.Favorite),
            unit = UiText.Translatable(R.string.unit_bpm),
        ),
        OXYGEN_SATURATION(
            text = UiText.Translatable(R.string.measurement_type_oxygen_saturation),
            icon = UiIcon.Vector(imageVector = Icons.Default.Spo2),
            unit = UiText.Translatable(R.string.unit_percent),
        ),
        BLOOD_PRESSURE(
            text = UiText.Translatable(R.string.measurement_type_blood_pressure),
            icon = UiIcon.Vector(imageVector = Icons.Default.Blood),
            unit = UiText.Translatable(R.string.unit_mm_hg),
        ),
        ;
    }
}

fun Measurement.asUi(): UiMeasurement {
    val type = when (this) {
        is BodyWeight -> UiMeasurement.Type.WEIGHT
        is HeartRate -> UiMeasurement.Type.HEART_RATE
        is BloodGlucose -> UiMeasurement.Type.BLOOD_GLUCOSE
        is OxygenSaturation -> UiMeasurement.Type.OXYGEN_SATURATION

        is BloodPressure,
        is SystolicPressure,
        is DiastolicPressure -> UiMeasurement.Type.BLOOD_PRESSURE
    }

    val value = when (this) {
        is HeartRate -> "$pulse"
        is BodyWeight -> "${weight.kg}"
        is BloodGlucose -> "$level"
        is OxygenSaturation -> "$saturation"
        is BloodPressure -> "${systolic.toInt()}/${diastolic.toInt()}"
        is SystolicPressure -> "$systolic"
        is DiastolicPressure -> "$diastolic"
    }

    val resource = when (val resource = resource) {
        Resource.Manual -> UiMeasurement.Resource.Manual
        is Device.WeightScale -> UiMeasurement.Resource.WeightScale
        is Device.BloodPressureCuff -> UiMeasurement.Resource.BPCuff
        is Device.PulseOximeter -> UiMeasurement.Resource.PulseOximeter
        is Device.GlucoseMeter -> UiMeasurement.Resource.Glucose
        is Resource.App -> UiMeasurement.Resource.App(
            text = UiText.App(packageName = resource.packageName),
            icon = UiIcon.App(packageName = resource.packageName),
        )
    }

    return UiMeasurement(
        id = id,
        type = type,
        unit = type.unit,
        time = createdAt,
        value = value,
        resource = resource,
        estimation = metadata[Estimation]?.asUi(),
        note = metadata[Note]?.let { note -> UiText.NonTranslatable(note.description) },
    )
}

fun KClass<out Measurement>.asUi(): UiMeasurement.Type = when (this) {
    BodyWeight::class -> UiMeasurement.Type.WEIGHT
    HeartRate::class -> UiMeasurement.Type.HEART_RATE
    BloodGlucose::class -> UiMeasurement.Type.BLOOD_GLUCOSE
    BloodPressure::class -> UiMeasurement.Type.BLOOD_PRESSURE
    OxygenSaturation::class -> UiMeasurement.Type.OXYGEN_SATURATION
    else -> UiMeasurement.Type.HEART_RATE
}

fun UiMeasurement.Type.asDomain(): KClass<out Measurement> = when (this) {
    UiMeasurement.Type.WEIGHT -> BodyWeight::class
    UiMeasurement.Type.HEART_RATE -> HeartRate::class
    UiMeasurement.Type.BLOOD_GLUCOSE -> BloodGlucose::class
    UiMeasurement.Type.BLOOD_PRESSURE -> BloodPressure::class
    UiMeasurement.Type.OXYGEN_SATURATION -> OxygenSaturation::class
}
