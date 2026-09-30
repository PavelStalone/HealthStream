package ru.health.stream.permission

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.health.stream.core.common.permission.Permission
import ru.health.stream.core.common.permission.PermissionManager
import ru.health.stream.core.common.permission.PermissionStatus
import ru.health.stream.core.monitor.logD
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

@Singleton
class AndroidPermissionManagerProxy @Inject constructor() : PermissionManager {

    private var permissionManager: PermissionManager? = null

    private val mutex: Mutex = Mutex(locked = true)

    override suspend fun request(permission: Permission): PermissionStatus {
        logD("Try request permission: $permission")
        return mutex.withLock {
            val manager = permissionManager

            checkNotNull(manager) { "Android permission manager not initialized" }
            manager.request(permission)
        }
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
}

class AndroidPermissionManager(private val context: ComponentActivity) : PermissionManager {

    private var continuation: Continuation<PermissionStatus>? = null

    private val requestPermissionLauncher = context.registerForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val c = continuation ?: return@registerForActivityResult

        if (isGranted) {
            c.resume(PermissionStatus.Granted)
        } else {
            c.resume(PermissionStatus.Denied)
        }
    }

    private val requestPermissionLauncher2 = context.registerForActivityResult(
        contract = PermissionCon.RequestMultiplePermissions()
    ) { isGranted ->
        val c = continuation ?: return@registerForActivityResult

        if (isGranted) {
            c.resume(PermissionStatus.Granted)
        } else {
            c.resume(PermissionStatus.Denied)
        }
    }

    override suspend fun request(permission: Permission): PermissionStatus {
        val androidPermission = permission.asAndroid() ?: return PermissionStatus.Granted

        return when {
            ContextCompat.checkSelfPermission(
                context,
                androidPermission
            ) == PackageManager.PERMISSION_GRANTED -> PermissionStatus.Granted

            ActivityCompat.shouldShowRequestPermissionRationale(context, androidPermission) ->
                PermissionStatus.ShowRequestPermissionRationale

            else -> {
                suspendCancellableCoroutine { continuation ->
                    this.continuation = continuation

                    requestPermissionLauncher.launch(androidPermission)

                    continuation.invokeOnCancellation {
                        this.continuation = null
                    }
                }
            }
        }
    }

    private fun Permission.asAndroid(): String? = when (this) {
        Permission.BluetoothConnect -> Manifest.permission.BLUETOOTH_CONNECT
        Permission.BLUETOOTH_SCAN -> Manifest.permission.BLUETOOTH_SCAN
    }
}
