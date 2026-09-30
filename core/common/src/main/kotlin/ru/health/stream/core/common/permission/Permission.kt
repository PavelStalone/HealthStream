package ru.health.stream.core.common.permission

enum class Permission {

    BluetoothConnect,
    BLUETOOTH_SCAN,
}

enum class PermissionStatus {

    Granted,
    Denied,
    DeniedAlways,
    ShowRequestPermissionRationale,
    ;
}
