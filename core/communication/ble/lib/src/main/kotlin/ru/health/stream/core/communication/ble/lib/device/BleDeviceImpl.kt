package ru.health.stream.core.communication.ble.lib.device

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import ru.health.stream.core.communication.ble.lib.device.GattCharacteristicScope.CallbackType
import ru.health.stream.core.communication.ble.lib.device.NordicBleManager.GattCharacteristicImpl
import ru.health.stream.core.communication.ble.lib.device.NordicBleManager.GattServiceImpl
import ru.health.stream.core.communication.ble.lib.packet.PacketSplitter
import ru.health.stream.core.communication.ble.lib.packet.model.FilledPacket
import ru.health.stream.core.communication.ble.lib.packet.model.asBytes
import no.nordicsemi.android.ble.BleManager
import ru.health.stream.core.monitor.logD
import ru.health.stream.core.monitor.logI
import ru.health.stream.core.monitor.logW
import java.util.UUID

/**
 * BLE manager implementation based on Nordic BLE library
 *
 * Manages communication with BLE devices by translating between device profiles
 * and the underlying Nordic BLE framework. Handles service discovery, characteristic
 * operations, and notification processing
 *
 * @property bleDevice device profile that defines services and characteristics
 */
internal class NordicBleManager(
    context: Context,
    private val bleDevice: BleDevice,
) : BleManager(context) {

    private var services: List<GattServiceImpl> = listOf()

    /**
     * Checks if the device supports all required services and characteristics
     *
     * Called by the Nordic BLE library during connection to verify that
     * the device has the necessary services for proper operation
     *
     * @param gatt the GATT profile of the connected device
     * @return True if all required services and characteristics are available
     */
    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        logD("Checking required services support")

        services = ConfigurationScopeImpl(nordicBleManager = this@NordicBleManager)
            .apply { with(bleDevice) { init() } }
            .build()
        logD("Device profile requires ${services.size} services")

        val isInitialize = services.all { service ->
            val nativeService = gatt.getService(service.uuid)
            service.gattService = nativeService

            logD("Found service ${service.uuid}")

            service.gattCharacteristics.all { characteristic ->
                val nativeCharacteristic = nativeService.getCharacteristic(characteristic.uuid)
                characteristic.gattCharacteristic = nativeCharacteristic

                val isSupported = nativeCharacteristic != null || !characteristic.isRequired

                if (!isSupported) logW("Required characteristic not found: ${characteristic.uuid}")
                isSupported
            }
        }

        logI("Device support check result: $isInitialize")
        return isInitialize
    }

    /**
     * Initializes the connection with the BLE device
     *
     * Called after the connection is established and required services are confirmed.
     * Sets up notification handlers and enables notifications for characteristics
     */
    override fun initialize() {
        logD("Initializing device connections")

        services.forEach { service ->
            logD("Setting up service ${service.uuid}")

            service.gattCharacteristics.forEach { characteristic ->
                setupCharacteristic(characteristic)
            }
        }

        logI("Device initialization complete with ${services.size} services")
    }

    /**
     * Handles invalidation of services when the device disconnects
     *
     * Cleans up all service and characteristic references and notifies
     * the device profile that the connection has been invalidated
     */
    override fun onServicesInvalidated() {
        logD("Services invalidated, cleaning up")

        services.forEach { service ->
            service.gattService = null

            service.gattCharacteristics.forEach { characteristic ->
                characteristic.isReady = false
                characteristic.gattCharacteristic = null
            }
        }

        logD("Cleared ${services.size} services")
        services = emptyList()

        logI("Notifying device profile of invalidation")
        bleDevice.onInvalidated()
    }

    /**
     * Sets up a characteristic with notification handlers if needed
     *
     * @param characteristic characteristic to set up
     */
    private fun setupCharacteristic(characteristic: GattCharacteristicImpl) {
        logD("Setting up characteristic ${characteristic.uuid} with ${characteristic.callbacks.size} notifications")

        if (characteristic.callbacks.isNotEmpty()) {
            characteristic.callbacks.forEach { (type, callbacks) ->
                val packetSplitter = PacketSplitter(callbacks.mapKeys { (key, _) -> key.structure })

                when (type) {
                    CallbackType.NOTIFICATION -> {
                        setNotificationCallback(characteristic.gattCharacteristic).with { _, data ->
                            logD("Notification received for ${characteristic.uuid} (${data.value?.size ?: 0} bytes)")

                            packetSplitter.pushData(data)
                        }

                        logD("Enabling notifications for ${characteristic.uuid}")
                        enableNotifications(characteristic.gattCharacteristic).enqueue()
                    }

                    CallbackType.INDICATION -> {
                        setIndicationCallback(characteristic.gattCharacteristic).with { _, data ->
                            logD("Indication received for ${characteristic.uuid} (${data.value?.size ?: 0} bytes)")

                            packetSplitter.pushData(data)
                        }

                        logD("Enabling indications for ${characteristic.uuid}")
                        enableIndications(characteristic.gattCharacteristic).enqueue()
                    }
                }
            }
        }

        characteristic.isReady = true
        logD("Characteristic ${characteristic.uuid} ready")
    }

    /**
     * Implementation of GattService for the Nordic BLE library
     *
     * @property uuid UUID of the service
     * @property gattCharacteristics list of characteristics in this service
     */
    internal class GattServiceImpl(
        val uuid: UUID,
        val gattCharacteristics: List<GattCharacteristicImpl>,
    ) : GattService {

        var gattService: BluetoothGattService? = null

        override val nativeService: BluetoothGattService
            get() = requireNotNull(gattService) { "Service $uuid not initialized" }
    }

    /**
     * Implementation of [GattCharacteristic] for the Nordic BLE library
     *
     * Handles read/write operations and notification processing for a BLE characteristic
     *
     * @property uuid UUID of the characteristic
     * @property isRequired whether this characteristic is required for device operation
     * @property callbacks map of packet definitions to callback handlers
     */
    internal inner class GattCharacteristicImpl(
        val uuid: UUID,
        val isRequired: Boolean,
        val callbacks: Map<CallbackType, Map<BlePacket.Definition<*>, (FilledPacket) -> Unit>>,
    ) : GattCharacteristic {

        var gattCharacteristic: BluetoothGattCharacteristic? = null

        /**
         * Indicates if the characteristic is ready for operations
         *
         * When set to true, all pending commands will be executed
         */
        var isReady: Boolean = false
            set(value) {
                field = value

                if (value && pendingCommands.isNotEmpty()) {
                    logD("Executing ${pendingCommands.size} pending commands for $uuid")

                    pendingCommands.removeIf { command ->
                        command()
                        true
                    }
                }
            }

        private val pendingCommands: MutableList<() -> Unit> = mutableListOf()

        override val nativeCharacteristic: BluetoothGattCharacteristic
            get() = requireNotNull(gattCharacteristic) { "Characteristic $uuid not initialized" }

        /**
         * Reads a packet from this characteristic
         *
         * @param packet definition of the packet to read
         * @param consumer callback to handle the parsed packet data
         */
        override fun <T : BlePacket.Data> readPacket(
            packet: BlePacket.Definition<T>,
            consumer: (packet: T) -> Unit,
        ) {
            logD("Request to read packet from $uuid")

            enqueueOrExecute {
                logD("Reading characteristic $uuid")

                readCharacteristic(nativeCharacteristic)
                    .with { _, data ->
                        logD(
                            "Read response for $uuid: ${data.value?.size ?: 0} bytes: ${
                                data.value?.joinToString(" ") { byte -> byte.toHexString() }
                            }"
                        )

                        val packetSplitter = PacketSplitter(
                            mapOf(
                                packet.structure to { filledPacket ->
                                    consumer(packet.parse(rawPacket = filledPacket))
                                }
                            )
                        )
                        packetSplitter.pushData(incomingData = data)
                    }.enqueue()
            }
        }

        /**
         * Writes a packet to this characteristic
         *
         * @param packet packet data to write
         * @param writeType type of write operation to perform
         */
        override fun <T : BlePacket.Convertible> writePacket(packet: T, writeType: Int) {
            val packetBytes = packet.rawPacket.asBytes()
            logD("Request to write ${packetBytes.size} bytes to $uuid (type: $writeType)")

            enqueueOrExecute {
                logD("Writing to characteristic $uuid: $packet")

                writeCharacteristic(
                    nativeCharacteristic,
                    packetBytes.toByteArray(),
                    writeType,
                ).enqueue()
            }
        }

        /**
         * Executes a command immediately if the characteristic is ready,
         * or queues it for later execution
         *
         * @param command the operation to perform
         */
        private fun enqueueOrExecute(command: () -> Unit) {
            if (isReady) {
                logD("Executing command immediately for $uuid")

                command()
            } else {
                logD("Queuing command for later execution on $uuid")

                pendingCommands.add(command)
            }
        }
    }
}

/**
 * Implementation of [ConfigurationScope] for building device configuration
 *
 * Collects services and characteristics defined by the device profile
 *
 * @property nordicBleManager BLE manager that will use this configuration
 */
internal class ConfigurationScopeImpl(
    private val nordicBleManager: NordicBleManager,
) : ConfigurationScope {

    private val services: MutableList<GattServiceImpl> = mutableListOf()

    /**
     * Defines a BLE service with the given UUID
     *
     * @param uuid UUID of the service
     * @param block configuration block for adding characteristics
     * @return Configured service instance
     */
    override fun service(uuid: UUID, block: GattServiceScope.() -> Unit): GattService {
        logD("Configuring service $uuid")

        return GattServiceScopeImpl(serviceUUID = uuid, nordicBleManager = nordicBleManager)
            .apply(block)
            .build()
            .also { service ->
                services.add(service)

                logD("Added service $uuid with ${service.gattCharacteristics.size} characteristics")
            }
    }

    /**
     * Builds the final list of configured services
     *
     * @return List of configured service implementations
     */
    fun build() = services.toList()
}

/**
 * Implementation of [GattServiceScope] for configuring a service
 *
 * Collects characteristics defined within a service
 *
 * @property serviceUUID UUID of the service being configured
 * @property nordicBleManager BLE manager that will use this configuration
 */
internal class GattServiceScopeImpl(
    private val serviceUUID: UUID,
    private val nordicBleManager: NordicBleManager,
) : GattServiceScope {

    private val characteristics: MutableList<GattCharacteristicImpl> = mutableListOf()

    /**
     * Defines a characteristic within this service
     *
     * @param uuid UUID of the characteristic
     * @param isRequired whether this characteristic is required for proper operation
     * @param block configuration block for adding notification handlers
     * @return Configured characteristic instance
     */
    override fun characteristic(
        uuid: UUID,
        isRequired: Boolean,
        block: GattCharacteristicScope.() -> Unit
    ): GattCharacteristic {
        logD("Configuring characteristic $uuid for service $serviceUUID")

        val callbacks = GattCharacteristicScopeImpl().apply(block).build()
        logD("Characteristic $uuid has ${callbacks.size} handlers")

        return nordicBleManager.GattCharacteristicImpl(
            uuid = uuid,
            callbacks = callbacks,
            isRequired = isRequired,
        ).also { gattCharacteristic ->
            characteristics.add(gattCharacteristic)

            logD("Added characteristic $uuid to service $serviceUUID")
        }
    }

    /**
     * Builds the final service implementation with its characteristics
     *
     * @return configured service implementation
     */
    fun build(): GattServiceImpl = GattServiceImpl(
        uuid = serviceUUID,
        gattCharacteristics = characteristics.toList(),
    ).also {
        logD("Built service $serviceUUID with ${characteristics.size} characteristics")
    }
}

/**
 * Implementation of [GattCharacteristicScope] for configuring a characteristic
 *
 * Collects notification handlers for packets on this characteristic
 */
internal class GattCharacteristicScopeImpl : GattCharacteristicScope {

    private val callbacks: MutableMap<CallbackType, MutableMap<BlePacket.Definition<*>, (FilledPacket) -> Unit>> =
        LinkedHashMap(CallbackType.entries.size)

    /**
     * Registers a notification callback for handling specific packet types
     *
     * @param packet definition of the packet to handle
     * @param callback function to call when a matching packet is received
     */
    override fun <T : BlePacket.Data> notificationCallback(
        packet: BlePacket.Definition<T>,
        callback: (packet: T) -> Unit
    ) = callback(
        packet = packet,
        callback = callback,
        callbackType = CallbackType.NOTIFICATION,
    )

    /**
     * Registers an indication callback for handling specific packet types
     *
     * @param packet definition of the packet to handle
     * @param callback function to call when a matching packet is received
     */
    override fun <T : BlePacket.Data> indicationCallback(
        packet: BlePacket.Definition<T>,
        callback: (packet: T) -> Unit
    ) = callback(
        packet = packet,
        callback = callback,
        callbackType = CallbackType.INDICATION,
    )

    /**
     * Registers a callback for handling specific packet types with the specified callback type
     *
     * @param callbackType type of callback to register
     * @param callback function to call when a matching packet is received
     * @param packet definition of the packet to handle
     */
    override fun <T : BlePacket.Data> callback(
        callbackType: CallbackType,
        callback: (packet: T) -> Unit,
        packet: BlePacket.Definition<T>
    ) {
        callbacks.compute(callbackType) { _, currentMap ->
            val map = currentMap ?: mutableMapOf()

            map.apply {
                put(key = packet, value = { filledPacket -> callback(packet.parse(filledPacket)) })
            }
        }
    }

    /**
     * Builds the final map of packet definitions to callbacks
     *
     * @return Map of packet definitions to callback handlers
     */
    fun build() = callbacks.toMap().mapValues { (_, map) -> map.toMap() }.also {
        logD("Built characteristic with ${callbacks.size} notification handlers")
    }
}
