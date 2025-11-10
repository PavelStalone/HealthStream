package ru.health.stream

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.net.toUri
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectFeatures
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
import ru.health.stream.core.starter.StarterActivity
import ru.health.stream.core.ui.theme.HealthStreamTheme
import ru.health.stream.feature.chart.api.CubicLine
import ru.health.stream.feature.chart.api.Line
import ru.health.stream.feature.chart.api.LineChart
import ru.health.stream.feature.chart.model.ChartPosition
import java.time.Instant

@AndroidEntryPoint
class MainActivity : StarterActivity() {

    val TAG = "MainActivity"

    val flow = MutableStateFlow(listOf<HeartRateRecord>())

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
        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE) {
            return // early return as there is no viable integration
        }
        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
            // Optionally redirect to package installer to find a provider, for example:
            val uriString =
                "market://details?id=com.google.android.apps.healthdata&url=healthconnect%3A%2F%2Fonboarding"
            startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    setPackage("com.android.vending")
                    data = uriString.toUri()
                    putExtra("overlay", true)
                    putExtra("callerId", packageName)
                }
            )
            return
        }

        val healthConnectClient = HealthConnectClient.getOrCreate(this)

        lifecycleScope.launch {
            if (healthConnectClient.features.getFeatureStatus(
                    feature = HealthConnectFeatures.FEATURE_READ_HEALTH_DATA_IN_BACKGROUND
                ) == HealthConnectFeatures.FEATURE_STATUS_AVAILABLE
            ) {
                Log.i(TAG, "Check permission")

                checkPermissionsAndRun(healthConnectClient)

                Log.i(TAG, "readHeartrate")
                readHeartRateByTimeRange(
                    healthConnectClient,
                    startTime = Instant.now().minusSeconds(3600 * 24 * 10L),
                    endTime = Instant.now()
                )
            } else {
                Log.d(TAG, "FEATURE_READ_HEALTH_DATA_IN_BACKGROUND is not available")
            }
        }

        enableEdgeToEdge()
        setContent {
            HealthStreamTheme {
//                val h by flow.collectAsState()
//
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    LazyColumn(
//                        modifier = Modifier.padding(innerPadding),
//                        verticalArrangement = Arrangement.spacedBy(8.dp),
//                    ) {
//                        items(h) {
//                            Card(Modifier.padding(horizontal = 8.dp)) {
//                                Text(
//                                    modifier = Modifier.padding(8.dp),
//                                    text = it.toString()
//                                )
//                            }
//                        }
//                    }
//                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.Gray)
                ) {
                    val width by rememberInfiniteTransition().animateFloat(
                        initialValue = 1f, targetValue = 20f, infiniteRepeatable(
                            tween(5000), RepeatMode.Reverse
                        )
                    )
                    LineChart(
                        modifier = Modifier.fillMaxSize(),
                        lines = listOf(
                            Line(
                                points = listOf(
                                    ChartPosition.Point(x = 0f, y = 0f, z = 0f),
                                    ChartPosition.Point(x = 1f, y = 40f, z = 0f),
                                    ChartPosition.Point(x = 2f, y = 20f, z = 0f),
                                    ChartPosition.Point(x = 3f, y = 50f, z = 0f),
                                    ChartPosition.Point(x = 5f, y = 0f, z = 0f),
                                )
                            ),
                            CubicLine(
                                points = listOf(
                                    ChartPosition.Point(x = 0f, y = 0f, z = 0f),
                                    ChartPosition.Point(x = 2f, y = 40f, z = 0f),
                                    ChartPosition.Point(x = 4f, y = 20f, z = 0f),
                                    ChartPosition.Point(x = 6f, y = 50f, z = 0f),
                                    ChartPosition.Point(x = width, y = 0f, z = 0f),
                                )
                            )
                        )
                    )
                }
            }
        }

        Log.i("MainActivity", "onCreate!!!!!!!!!!!!!!!")
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

    suspend fun readHeartRateByTimeRange(
        healthConnectClient: HealthConnectClient,
        startTime: Instant,
        endTime: Instant
    ) {
        try {
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )

            Log.i(TAG, "Records found: ${response.records.size} - ${response.records}")
            flow.value = response.records
            for (record in response.records) {
                Log.d(TAG, "Record:")
                Log.d(TAG, record.toString())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error while read", e)
        }
    }
}
