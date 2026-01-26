package ru.health.stream.core.communication.ble.lib.device

import android.Manifest
import androidx.annotation.RequiresPermission
import no.nordicsemi.android.support.v18.scanner.ScanFilter
import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice
import ru.health.stream.core.monitor.logD
import ru.health.stream.core.monitor.logE
import java.util.Locale
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

/**
 * Checks if this device's name contains any of the specified patterns
 *
 * This function compares the device's name against a list of pattern strings. A match occurs if
 * any pattern is contained within the device name
 *
 * @param prefixList list of case-insensitive string patterns to check against device name
 * @return true if the device name contains any of the patterns, false otherwise
 * @throws SecurityException if BLUETOOTH_CONNECT permission is not granted
 */
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
fun DiscoveredBluetoothDevice.matchesAnyPrefix(prefixList: List<String>): Boolean {
    logD("Local name: ${name}, device name: ${device.name}")

    val deviceNameLowercase = device.name.lowercase(Locale.ROOT)
    return prefixList.any { deviceNamePrefix ->
        deviceNameLowercase.contains(deviceNamePrefix)
    }
}

/**
 * Parses a string into a UUID with safe fallback
 *
 * Attempts to parse the provided string into a UUID. If parsing fails,
 * returns the NIL UUID (00000000-0000-0000-0000-000000000000) and logs an error
 *
 * @param uuid string to parse as a UUID
 * @return the parsed UUID, or NIL UUID if parsing failed
 */
@OptIn(ExperimentalUuidApi::class)
fun createUUID(uuid: String): UUID = runCatching {
    Uuid.parse(uuidString = uuid)
}.getOrElse { exception ->
    logE(exception, "Invalid UUID format: $uuid, using fallback UUID")

    Uuid.NIL
}.toJavaUuid()

/**
 * Creates a byte array from a sequence of integers
 *
 * Each integer is converted to a byte value. This is a convenience function
 * for creating byte arrays for binary protocols or configurations
 *
 * @param ints vararg of integers to convert to bytes
 * @return byte array containing the converted values
 */
fun byteArrayOfInts(vararg ints: Int) = ByteArray(ints.size) { pos -> ints[pos].toByte() }

/**
 * Builds a scan filter using a builder pattern with lambda
 *
 * Provides a concise way to configure and build a ScanFilter using Kotlin's
 * lambda and builder pattern
 *
 * @param configure lambda with ScanFilter.Builder as receiver to configure the filter
 * @return built ScanFilter instance
 */
fun buildScanFilter(configure: ScanFilter.Builder.() -> ScanFilter.Builder): ScanFilter =
    ScanFilter.Builder().run(configure).build()
