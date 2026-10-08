package ru.health.stream.permission

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.WeightRecord
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.health.stream.core.common.permission.Permission
import ru.health.stream.core.common.permission.PermissionManager
import ru.health.stream.core.common.permission.PermissionStatus
import ru.health.stream.core.common.permission.PermissionStatus.Denied
import ru.health.stream.core.common.permission.PermissionStatus.Granted
import ru.health.stream.core.monitor.logD
import ru.health.stream.core.monitor.logI
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

@Singleton
class AndroidPermissionManagerProxy @Inject constructor() : PermissionManager {

    private val mutex: Mutex = Mutex(locked = true)

    private var permissionManager: PermissionManager? = null

    override suspend fun request(permission: Permission): PermissionStatus {
        logD("Try request permission: $permission")
        return withLock { request(permission) }
    }

    override suspend fun requestGroup(vararg permissions: Permission): Map<Permission, PermissionStatus> {
        logD("Try request permissions: ${permissions.toList()}")
        return withLock { requestGroup(*permissions) }
    }

    override suspend fun check(permission: Permission): PermissionStatus {
        logD("Try check permission: $permission")
        return withLock { check(permission) }
    }

    override suspend fun checkGroup(vararg permissions: Permission): Map<Permission, PermissionStatus> {
        logD("Try check permissions: ${permissions.toList()}")
        return withLock { checkGroup(*permissions) }
    }

    fun setManager(permissionManager: PermissionManager) {
        this.permissionManager = permissionManager
        if (mutex.isLocked) mutex.unlock()
    }

    fun removeManager(permissionManager: PermissionManager) {
        if (this.permissionManager == permissionManager) {
            if (!mutex.isLocked) mutex.tryLock()
            this.permissionManager = null
        }
    }

    private suspend fun <T> withLock(block: suspend PermissionManager.() -> T) = mutex.withLock {
        val manager = permissionManager

        checkNotNull(manager) { "Android permission manager not initialized" }
        manager.block()
    }
}

internal class AndroidPermissionManager(
    private val context: ComponentActivity
) : PermissionManager {

    private val requestOnePermissionLauncher = context.registerForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val c = continuation ?: return@registerForActivityResult

        if (isGranted) {
            c.resume(Granted)
        } else {
            c.resume(Denied)
        }
    }

    private val requestMultiplePermissionsLauncher = context.registerForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grantedMap ->
        val c = multipleContinuation ?: return@registerForActivityResult
        val result = grantedMap.filterKeys { permission -> permission.asCommon() != null }
            .mapKeys { (permission, _) -> permission.asCommon()!! }
            .mapValues { (_, isGranted) -> if (isGranted) Granted else Denied }

        c.resume(result)
    }

    private var continuation: Continuation<PermissionStatus>? = null
    private var multipleContinuation: Continuation<Map<Permission, PermissionStatus>>? = null

    override suspend fun request(permission: Permission): PermissionStatus {
        val androidPermission = permission.asAndroid() ?: return Granted

        val result = when {
            checkAlreadyGranted(androidPermission) -> Granted

            else -> {
                suspendCancellableCoroutine { continuation ->
                    this.continuation = continuation

                    requestOnePermissionLauncher.launch(androidPermission)

                    continuation.invokeOnCancellation {
                        this.continuation = null
                    }
                }
            }
        }

        logI("Permission status for $permission: $result")
        return result
    }

    override suspend fun requestGroup(vararg permissions: Permission): Map<Permission, PermissionStatus> {
        val androidPermissions = permissions.mapNotNull { permission -> permission.asAndroid() }
        val unknownPermission = permissions.filter { permission -> permission.asAndroid() == null }
        val alreadyGranted = androidPermissions.filter { checkAlreadyGranted(it) }

        val needRequestPermissions = androidPermissions.minus(alreadyGranted)
        val notNeedRequestPermissions = alreadyGranted.mapNotNull { it.asCommon() }
            .plus(unknownPermission)
            .associateWith { Granted }

        val result = if (needRequestPermissions.isNotEmpty()) {
            val requestedResult = suspendCancellableCoroutine { continuation ->
                this.multipleContinuation = continuation

                requestMultiplePermissionsLauncher.launch(needRequestPermissions.toTypedArray())

                continuation.invokeOnCancellation {
                    this.multipleContinuation = null
                }
            }

            requestedResult + notNeedRequestPermissions
        } else {
            notNeedRequestPermissions
        }

        result.forEach { (permission, status) -> logI("Permission status for $permission: $status") }
        return result
    }

    override suspend fun check(permission: Permission): PermissionStatus {
        val androidPermission = permission.asAndroid() ?: return Granted
        if (checkAlreadyGranted(androidPermission)) return Granted
        return Denied
    }

    override suspend fun checkGroup(vararg permissions: Permission): Map<Permission, PermissionStatus> {
        return permissions.associateWith { permission -> check(permission) }
    }

    private fun checkAlreadyGranted(androidPermission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            androidPermission
        ) == PackageManager.PERMISSION_GRANTED
    }

    // @formatter:off
    private fun Permission.asAndroid(): String? = when (this) {
        Permission.BluetoothConnect -> Manifest.permission.BLUETOOTH_CONNECT
        Permission.BluetoothScan -> Manifest.permission.BLUETOOTH_SCAN

        Permission.ReadHeartRate -> HealthPermission.getReadPermission(HeartRateRecord::class)
        Permission.ReadBloodPressure -> HealthPermission.getReadPermission(BloodPressureRecord::class)
        Permission.ReadOxygenSaturation -> HealthPermission.getReadPermission(OxygenSaturationRecord::class)
        Permission.ReadWeightScale -> HealthPermission.getReadPermission(WeightRecord::class)

        Permission.WriteHeartRate -> HealthPermission.getWritePermission(HeartRateRecord::class)
        Permission.WriteBloodPressure -> HealthPermission.getWritePermission(BloodPressureRecord::class)
        Permission.WriteOxygenSaturation -> HealthPermission.getWritePermission(OxygenSaturationRecord::class)
        Permission.WriteWeightScale -> HealthPermission.getWritePermission(WeightRecord::class)
    }
    // @formatter:on

    private fun String.asCommon(): Permission? = when (this) {
        Manifest.permission.BLUETOOTH_CONNECT -> Permission.BluetoothConnect
        Manifest.permission.BLUETOOTH_SCAN -> Permission.BluetoothScan

        HealthPermission.getReadPermission(HeartRateRecord::class) -> Permission.ReadHeartRate
        HealthPermission.getReadPermission(BloodPressureRecord::class) -> Permission.ReadBloodPressure
        HealthPermission.getReadPermission(OxygenSaturationRecord::class) -> Permission.ReadOxygenSaturation
        HealthPermission.getReadPermission(WeightRecord::class) -> Permission.ReadWeightScale

        HealthPermission.getWritePermission(HeartRateRecord::class) -> Permission.WriteHeartRate
        HealthPermission.getWritePermission(BloodPressureRecord::class) -> Permission.WriteBloodPressure
        HealthPermission.getWritePermission(OxygenSaturationRecord::class) -> Permission.WriteOxygenSaturation
        HealthPermission.getWritePermission(WeightRecord::class) -> Permission.WriteWeightScale

        else -> null
    }
}
