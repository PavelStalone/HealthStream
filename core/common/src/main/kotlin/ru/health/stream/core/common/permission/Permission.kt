package ru.health.stream.core.common.permission

enum class Permission {

    BluetoothConnect,
    BluetoothScan,

    ReadHeartRate,
    ReadBloodPressure,
    ReadOxygenSaturation,
    ReadWeightScale,

    WriteHeartRate,
    WriteBloodPressure,
    WriteOxygenSaturation,
    WriteWeightScale,
    ;
}

enum class PermissionStatus {

    Granted,
    Denied,
    ;
}
