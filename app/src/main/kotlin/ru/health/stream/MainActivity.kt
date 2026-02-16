package ru.health.stream

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.lifecycle.lifecycleScope
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.arttttt.nav3router.Nav3Host
import com.arttttt.nav3router.Router
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.health.stream.core.monitor.logI
import ru.health.stream.core.starter.StarterActivity
import ru.health.stream.core.ui.theme.HealthStreamTheme
import ru.health.stream.feature.vitals.data.model.HealthMeasurement
import ru.health.stream.feature.vitals.data.navigation.MainVitalsScreen
import ru.health.stream.feature.vitals.data.repository.MeasurementRepository
import javax.inject.Inject
import kotlin.time.Duration.Companion.days

@AndroidEntryPoint
class MainActivity : StarterActivity() {

    @Inject
    lateinit var navigationRouter: Router<NavKey>

    @Inject
    lateinit var measurementRepository: MeasurementRepository

    @Inject
    lateinit var entryBuilders: Set<@JvmSuppressWildcards EntryProviderScope<NavKey>.() -> Unit>

    val flow = MutableStateFlow(listOf<HealthMeasurement.HeartRate>())

    val PERMISSIONS =
        setOf(
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getWritePermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getWritePermission(StepsRecord::class)
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val availabilityStatus = HealthConnectClient.getSdkStatus(this)

        logI("availabilityStatus: $availabilityStatus")
//        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE) {
//            return // early return as there is no viable integration
//        }
//        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
//            // Optionally redirect to package installer to find a provider, for example:
//            val uriString =
//                "market://details?id=com.google.android.apps.healthdata&url=healthconnect%3A%2F%2Fonboarding"
//            startActivity(
//                Intent(Intent.ACTION_VIEW).apply {
//                    setPackage("com.android.vending")
//                    data = uriString.toUri()
//                    putExtra("overlay", true)
//                    putExtra("callerId", packageName)
//                }
//            )
//            return
//        }

        lifecycleScope.launch {
            flow.value = measurementRepository.getMeasurementsByDuration(
                duration = 200.days,
                type = HealthMeasurement.HeartRate::class,
            )
        }

        enableEdgeToEdge()
        setContent {
            HealthStreamTheme {
                val backStack = rememberNavBackStack(MainVitalsScreen)

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Nav3Host(
                        backStack = backStack,
                        router = navigationRouter,
                    ) { backStack, onBack, _ ->
                        NavDisplay(
                            modifier = Modifier.padding(innerPadding),
                            backStack = backStack,
                            onBack = onBack,
                            entryProvider = entryProvider {
                                entryBuilders.forEach { builder -> this.builder() }
                            },
                        )
                    }
                }
            }
        }
    }

    // Create the permissions launcher
    val requestPermissionActivityContract =
        PermissionController.createRequestPermissionResultContract()

    val requestPermissions =
        registerForActivityResult(requestPermissionActivityContract) { granted ->
            if (granted.containsAll(PERMISSIONS)) {
                // Permissions successfully granted
            } else {
                // Lack of required permissions
            }
        }

    suspend fun checkPermissionsAndRun(healthConnectClient: HealthConnectClient) {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        if (granted.containsAll(PERMISSIONS)) {
            // Permissions already granted; proceed with inserting or reading data
        } else {
            requestPermissions.launch(PERMISSIONS)
        }
    }
}
