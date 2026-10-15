package ru.health.stream.feature.measurement.impl.presentation.component.input

import androidx.annotation.StringRes
import ru.health.stream.core.ui.R

/**
 * Exception carrying a translatable error message resource id.
 */
internal class ValidationError(
    @StringRes val messageRes: Int,
) : RuntimeException() {

    companion object {
        val InvalidNumber = ValidationError(R.string.validation_error_invalid_number)
        val PositiveRequired = ValidationError(R.string.validation_error_positive_required)
        val GreaterThanDiastolic = ValidationError(R.string.validation_error_greater_than_diastolic)
        val PulseTooHigh = ValidationError(R.string.validation_error_pulse_too_high)
        val GlucoseNegative = ValidationError(R.string.validation_error_glucose_negative)
        val SaturationRange = ValidationError(R.string.validation_error_saturation_range)
        val WeightPositive = ValidationError(R.string.validation_error_weight_positive)
    }
}