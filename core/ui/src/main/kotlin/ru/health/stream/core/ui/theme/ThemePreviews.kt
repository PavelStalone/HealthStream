package ru.health.stream.core.ui.theme

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "1. Light Theme",
    group = "Themes",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true
)
@Preview(
    name = "2. Dark Theme",
    group = "Themes",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
annotation class ThemePreviews

@Preview(
    name = "Small Device",
    group = "Devices",
    device = Devices.NEXUS_5,
    showBackground = true
)
@Preview(
    name = "Medium Device",
    group = "Devices",
    device = Devices.PIXEL_4,
    showBackground = true
)
@Preview(
    name = "Large Device",
    group = "Devices",
    device = Devices.PIXEL_4_XL,
    showBackground = true
)
annotation class DevicePreviews

@ThemePreviews
@DevicePreviews
annotation class DeviceThemePreviews
