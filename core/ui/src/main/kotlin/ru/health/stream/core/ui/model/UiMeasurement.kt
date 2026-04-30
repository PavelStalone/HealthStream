package ru.health.stream.core.ui.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant
import ru.health.stream.core.ui.icon.Icons
import ru.health.stream.core.ui.icon.device.BPCuff
import ru.health.stream.core.ui.icon.device.Glucose
import ru.health.stream.core.ui.icon.device.Pencil
import ru.health.stream.core.ui.icon.device.PulseOximeter
import ru.health.stream.core.ui.icon.device.WeightScale
import ru.health.stream.core.ui.icon.fill.Favorite
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
import ru.health.stream.data.vitals.model.measurement.RespirationRate
import ru.health.stream.data.vitals.model.measurement.SystolicPressure

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
            text = UiText.NonTranslatable("Ручная запись"),
            icon = UiIcon.Vector(imageVector = Icons.Device.Pencil),
        )

        data object PulseOximeter : Resource(
            text = UiText.NonTranslatable("Пульсоксиметр"),
            icon = UiIcon.Vector(imageVector = Icons.Device.PulseOximeter),
        )

        data object WeightScale : Resource(
            text = UiText.NonTranslatable("Весы"),
            icon = UiIcon.Vector(imageVector = Icons.Device.WeightScale),
        )

        data object BPCuff : Resource(
            text = UiText.NonTranslatable("Тонометр"),
            icon = UiIcon.Vector(imageVector = Icons.Device.BPCuff),
        )

        data object Glucose : Resource(
            text = UiText.NonTranslatable("Глюкометр"),
            icon = UiIcon.Vector(imageVector = Icons.Device.Glucose),
        )
    }

    enum class Type(
        val text: UiText,
        val icon: UiIcon,
    ) {

        WEIGHT(
            text = UiText.NonTranslatable("Вес"),
            icon = UiIcon.Vector(imageVector = Icons.Fill.Favorite),
        ),
        BLOOD_GLUCOSE(
            text = UiText.NonTranslatable("Глюкоза"),
            icon = UiIcon.Vector(imageVector = Icons.Fill.Favorite),
        ),
        HEART_RATE(
            text = UiText.NonTranslatable("Пульс"),
            icon = UiIcon.Vector(imageVector = Icons.Fill.Favorite),
        ),
        RESPIRATION_RATE(
            text = UiText.NonTranslatable("Дыхание"),
            icon = UiIcon.Vector(imageVector = Icons.Fill.Favorite),
        ),
        OXYGEN_SATURATION(
            text = UiText.NonTranslatable("Сатурация"),
            icon = UiIcon.Vector(imageVector = Icons.Fill.Favorite),
        ),
        BLOOD_PRESSURE(
            text = UiText.NonTranslatable("Давление"),
            icon = UiIcon.Vector(imageVector = Icons.Fill.Favorite),
        ),
        ;
    }
}

fun Measurement.asUi(): UiMeasurement {
    val (value, unit) = when (this) {
        is HeartRate -> "$pulse" to UiText.NonTranslatable(value = "уд/мин")
        is BodyWeight -> "$weight" to UiText.NonTranslatable(value = "кг")
        is OxygenSaturation -> "$saturation" to UiText.NonTranslatable(value = "%")
        is BloodPressure -> "${systolic.toInt()}/${diastolic.toInt()}" to UiText.NonTranslatable(value = "мм рт. ст.")
        is RespirationRate -> "$rate" to UiText.NonTranslatable(value = "дых/мин")
        is BloodGlucose -> "${level.toInt()}" to UiText.NonTranslatable(value = "ммоль/л")

        is SystolicPressure -> "$systolic" to UiText.NonTranslatable(value = "мм рт. ст.")
        is DiastolicPressure -> "$diastolic" to UiText.NonTranslatable(value = "мм рт. ст.")
    }

    val type = when (this) {
        is BodyWeight -> UiMeasurement.Type.WEIGHT
        is HeartRate -> UiMeasurement.Type.HEART_RATE
        is BloodGlucose -> UiMeasurement.Type.BLOOD_GLUCOSE
        is RespirationRate -> UiMeasurement.Type.RESPIRATION_RATE
        is OxygenSaturation -> UiMeasurement.Type.OXYGEN_SATURATION

        is BloodPressure,
        is SystolicPressure,
        is DiastolicPressure -> UiMeasurement.Type.BLOOD_PRESSURE
    }

    val resource = when (val resource = resource) {
        Resource.Manual -> UiMeasurement.Resource.Manual
        is Device.WeightScale -> UiMeasurement.Resource.WeightScale
        is Device.BloodPressureCuff -> UiMeasurement.Resource.BPCuff
        is Device.PulseOximeter -> UiMeasurement.Resource.PulseOximeter
        is Resource.App -> UiMeasurement.Resource.App(
            text = UiText.App(packageName = resource.packageName),
            icon = UiIcon.App(packageName = resource.packageName),
        )
    }

    return UiMeasurement(
        id = id,
        type = type,
        unit = unit,
        time = createdAt,
        value = value,
        resource = resource,
        estimation = metadata[Estimation]?.asUi(),
        note = metadata[Note]?.let { note -> UiText.NonTranslatable(note.description) },
    )
}

fun Estimation.asUi(): UiLevel = when (level) {
    Estimation.Level.LOW -> UiLevel.LOW
    Estimation.Level.NORMAL -> UiLevel.NORMAL
    Estimation.Level.HIGH -> UiLevel.HIGH
    Estimation.Level.EXTRA_HIGH -> UiLevel.EXTRA_HIGH
}
