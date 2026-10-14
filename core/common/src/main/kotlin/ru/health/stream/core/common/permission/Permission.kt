package ru.health.stream.core.common.permission

enum class Permission {

    BluetoothScan,
    BluetoothConnect,

    ReadHeartRate,
    ReadWeightScale,
    ReadBloodGlucose,
    ReadBloodPressure,
    ReadOxygenSaturation,

    WriteHeartRate,
    WriteWeightScale,
    WriteBloodGlucose,
    WriteBloodPressure,
    WriteOxygenSaturation,
    ;
}

enum class PermissionStatus {

    Granted,
    Denied,
    ;
}
