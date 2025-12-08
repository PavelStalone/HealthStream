package ru.health.stream

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.health.stream.core.monitor.logD
import ru.health.stream.core.monitor.logE
import ru.health.stream.core.monitor.logI
import ru.health.stream.core.starter.StarterActivity
import ru.health.stream.core.ui.theme.HealthStreamTheme
import ru.health.stream.feature.vitals.data.model.HealthMeasurement
import ru.health.stream.feature.vitals.data.repository.MeasurementRepository
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration.Companion.days

@AndroidEntryPoint
class MainActivity : StarterActivity() {

    @Inject
    lateinit var measurementRepository: MeasurementRepository

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
//
//        val healthConnectClient = HealthConnectClient.getOrCreate(this)
//
        lifecycleScope.launch {
            flow.value = measurementRepository.getMeasurementsByDuration(
                duration = 200.days,
                type = HealthMeasurement.HeartRate::class,
            )
//            if (healthConnectClient.features.getFeatureStatus(
//                    feature = HealthConnectFeatures.FEATURE_READ_HEALTH_DATA_IN_BACKGROUND
//                ) == HealthConnectFeatures.FEATURE_STATUS_AVAILABLE
//            ) {
//                Log.i(TAG, "Check permission")
//
//                checkPermissionsAndRun(healthConnectClient)
//
//                Log.i(TAG, "readHeartrate")
//                readHeartRateByTimeRange(
//                    healthConnectClient,
//                    startTime = Instant.now().minusSeconds(3600 * 24 * 10L),
//                    endTime = Instant.now()
//                )
//            } else {
//                Log.d(TAG, "FEATURE_READ_HEALTH_DATA_IN_BACKGROUND is not available")
//            }

        }

        enableEdgeToEdge()
        setContent {
            HealthStreamTheme {
                val h by flow.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LazyColumn(
                        modifier = Modifier.padding(innerPadding),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(h) {
                            Card(Modifier.padding(horizontal = 8.dp)) {
                                Text(
                                    modifier = Modifier.padding(8.dp),
                                    text = it.toString()
                                )
                            }
                        }
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

//    suspend fun readHeartRateByTimeRange(
//        healthConnectClient: HealthConnectClient,
//        startTime: Instant,
//        endTime: Instant
//    ) {
//        try {
//            val response = healthConnectClient.readRecords(
//                ReadRecordsRequest(
//                    HeartRateRecord::class,
//                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
//                )
//            )
//
//            logI("Records found: ${response.records.size} - ${response.records}")
//            flow.value = response.records
//            for (record in response.records) {
//                logD("Record:")
//                logD(record.toString())
//            }
//        } catch (e: Exception) {
//            logE(e, "Error while read")
//        }
//    }
}
