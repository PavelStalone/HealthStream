package ru.health.stream.feature.healthconnect.impl.presentation.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import ru.health.stream.core.ui.icon.Icons
import ru.health.stream.core.ui.icon.default.Blood
import ru.health.stream.core.ui.icon.default.Favorite
import ru.health.stream.core.ui.icon.default.Spo2
import ru.health.stream.core.ui.icon.default.Weight
import ru.health.stream.feature.healthconnect.impl.R

@Immutable
data class DataTypeRationale(
    @StringRes val titleRes: Int,
    @StringRes val bodyRes: Int,
    val icon: ImageVector,
    val hasRead: Boolean,
    val hasWrite: Boolean,
)

val DefaultDataTypeRationales: List<DataTypeRationale> = listOf(
    DataTypeRationale(
        titleRes = R.string.permissions_rationale_data_heart_rate_title,
        bodyRes = R.string.permissions_rationale_data_heart_rate_body,
        icon = Icons.Default.Favorite,
        hasRead = true,
        hasWrite = true,
    ),
    DataTypeRationale(
        titleRes = R.string.permissions_rationale_data_blood_pressure_title,
        bodyRes = R.string.permissions_rationale_data_blood_pressure_body,
        icon = Icons.Default.Blood,
        hasRead = true,
        hasWrite = true,
    ),
    DataTypeRationale(
        titleRes = R.string.permissions_rationale_data_oxygen_saturation_title,
        bodyRes = R.string.permissions_rationale_data_oxygen_saturation_body,
        icon = Icons.Default.Spo2,
        hasRead = true,
        hasWrite = true,
    ),
    DataTypeRationale(
        titleRes = R.string.permissions_rationale_data_weight_title,
        bodyRes = R.string.permissions_rationale_data_weight_body,
        icon = Icons.Default.Weight,
        hasRead = true,
        hasWrite = true,
    ),
)
