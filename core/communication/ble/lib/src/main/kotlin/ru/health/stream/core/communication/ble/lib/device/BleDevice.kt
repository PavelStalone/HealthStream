package ru.health.stream.core.communication.ble.lib.device

import java.util.UUID
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import no.nordicsemi.android.ble.annotation.WriteType
import no.nordicsemi.android.support.v18.scanner.ScanFilter
import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice

/**
 * Marker for device DSL scope
 *
 * Used to constrain the scope of DSL functions when configuring BLE devices,
 * preventing unintended use of methods outside their intended context
 */
@DslMarker
annotation class DeviceScopeMarker

/**
 * Base class for defining BLE devices
 *
 * Provides a foundation for implementing interaction with specific BLE devices.
 * Subclasses must define initialization logic and supported device verification
 */
abstract class BleDevice {

    /**
     * List of scan filters for discovering this device type
     *
     * These filters are used during BLE scanning to identify potential matches
     * for this device type. Implementing classes should define filters based on
     * manufacturer data, service UUIDs, or device names that uniquely identify
     * their specific devices
     */
    abstract val scanFilters: List<ScanFilter>

    /**
     * Initializes the device configuration
     *
     * This method is called during device registration to set up
     * services, characteristics, and packet handlers
     */
    abstract fun ConfigurationScope.init()

    /**
     * Handles device invalidation event
     *
     * Called when the device connection is lost, terminated, or otherwise becomes invalid
     */
    abstract fun onInvalidated()

    /**
     * Checks if the discovered Bluetooth device is supported
     *
     * @param discoveredDevice discovered device to check
     * @return True if the device is supported, false otherwise
     */
    abstract fun isDeviceSupported(discoveredDevice: DiscoveredBluetoothDevice): Boolean
}

/**
 * Scope for BLE device configuration
 *
 * Allows declarative definition of BLE device services and characteristics
 * in a structured and type-safe manner
 */
@DeviceScopeMarker
interface ConfigurationScope {

    /**
     * Defines a GATT service with the specified UUID
     *
     * @param uuid unique identifier of the service
     * @param block lambda for configuring the service's characteristics
     * @return Configured GATT service object
     */
    fun service(uuid: UUID, block: GattServiceScope.() -> Unit): GattService
}

/**
 * Scope for GATT service configuration
 *
 * Provides methods for defining characteristics within a service
 */
@DeviceScopeMarker
interface GattServiceScope {

    /**
     * Defines a GATT characteristic with the specified UUID
     *
     * @param uuid unique identifier of the characteristic
     * @param isRequired indicates if the characteristic is required for device operation
     * @param block lambda for configuring characteristic handlers
     * @return Configured GATT characteristic object
     */
    fun characteristic(
        uuid: UUID,
        isRequired: Boolean = true,
        block: GattCharacteristicScope.() -> Unit = {},
    ): GattCharacteristic
}

/**
 * Scope for GATT characteristic configuration
 *
 * Allows registration of handlers for data packets received from the characteristic
 */
@DeviceScopeMarker
interface GattCharacteristicScope {

    /**
     * Registers a handler for notification packets
     *
     * @param packet packet structure for data deserialization
     * @param callback callback function called when a packet of this type is received
     * @param T type of the filled BLE packet
     */
    fun <T : BlePacket.Data> notificationCallback(
        packet: BlePacket.Definition<T>,
        callback: (packet: T) -> Unit,
    )

    /**
     * Registers a handler for indication packets
     *
     * @param packet packet structure for data deserialization
     * @param callback callback function called when a packet of this type is received
     * @param T type of the filled BLE packet
     */
    fun <T : BlePacket.Data> indicationCallback(
        packet: BlePacket.Definition<T>,
        callback: (packet: T) -> Unit,
    )

    /**
     * Registers a handler for a specific type of characteristic updates
     *
     * @param callbackType type of callback to register
     * @param callback callback function called when a packet of this type is received
     * @param packet packet structure for data deserialization
     * @param T type of the filled BLE packet
     */
    fun <T : BlePacket.Data> callback(
        callbackType: CallbackType,
        callback: (packet: T) -> Unit,
        packet: BlePacket.Definition<T>,
    )

    /**
     * Defines types of characteristic value change callbacks
     */
    enum class CallbackType {

        /**
         * Notification callbacks
         */
        NOTIFICATION,

        /**
         * Indication callbacks
         */
        INDICATION,
        ;
    }
}

/**
 * Represents a device GATT service
 *
 * Service object is created during device configuration and used
 * to access the service's characteristics
 */
interface GattService {

    /**
     * Underlying Android [BluetoothGattService] object
     *
     * Provides direct access to the native Android Bluetooth API when needed
     * for advanced operations not covered by the framework
     */
    val nativeService: BluetoothGattService
}

/**
 * Represents a device GATT characteristic
 *
 * Provides methods for reading and writing data packets through the BLE characteristic
 */
interface GattCharacteristic {

    /**
     * Underlying Android [BluetoothGattCharacteristic] object
     *
     * Provides direct access to the native Android Bluetooth API when needed
     * for advanced operations not covered by the framework
     */
    val nativeCharacteristic: BluetoothGattCharacteristic

    /**
     * Asynchronously reads a data packet from the characteristic
     *
     * @param packet packet structure for data deserialization
     * @param consumer function to process the read packet
     * @param T type of the filled BLE packet
     */
    fun <T : BlePacket.Data> readPacket(
        packet: BlePacket.Definition<T>,
        consumer: (packet: T) -> Unit,
    )

    /**
     * Writes a data packet to the characteristic
     *
     * @param packet filled packet to write
     * @param writeType type of write operation
     * @param T type of the filled BLE packet
     */
    fun <T : BlePacket.Convertible> writePacket(
        packet: T,
        @WriteType writeType: Int = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE,
    )
}
