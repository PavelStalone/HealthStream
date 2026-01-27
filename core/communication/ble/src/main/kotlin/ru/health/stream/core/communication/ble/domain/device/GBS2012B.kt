package ru.health.stream.core.communication.ble.domain.device

import android.Manifest
import androidx.annotation.RequiresPermission
import com.movisens.smartgattlib.attributes.DefaultAttribute
import com.movisens.smartgattlib.helper.Characteristic
import com.movisens.smartgattlib.helper.Service
import no.nordicsemi.android.support.v18.scanner.ScanFilter
import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice
import ru.health.stream.core.communication.ble.domain.device.GBS2012BPacket.Filled
import ru.health.stream.core.communication.ble.domain.device.GBS2012BPacket.Filled.BindResult
import ru.health.stream.core.communication.ble.domain.device.GBS2012BPacket.Filled.ReceiverAuth
import ru.health.stream.core.communication.ble.domain.device.GBS2012BPacket.Filled.ReceiverInit
import ru.health.stream.core.communication.ble.domain.device.GBS2012BPacket.Filled.RegistrationResponse
import ru.health.stream.core.communication.ble.domain.device.GBS2012BPacket.Filled.SettingCallback
import ru.health.stream.core.communication.ble.domain.device.GBS2012BPacket.Filled.SettingCallback.SettingAck
import ru.health.stream.core.communication.ble.domain.device.GBS2012BPacket.Filled.Unknown
import ru.health.stream.core.communication.ble.domain.device.GBS2012BPacket.Filled.WeightData
import ru.health.stream.core.communication.ble.lib.device.BleDevice
import ru.health.stream.core.communication.ble.lib.device.BlePacket
import ru.health.stream.core.communication.ble.lib.device.ConfigurationScope
import ru.health.stream.core.communication.ble.lib.device.GattCharacteristic
import ru.health.stream.core.communication.ble.lib.device.buildScanFilter
import ru.health.stream.core.communication.ble.lib.device.matchesAnyPrefix
import ru.health.stream.core.monitor.logD
import ru.health.stream.core.monitor.logI
import ru.health.stream.core.monitor.logV
import ru.health.stream.core.monitor.logW

class GBS2012B : BleDevice() {

    private val DEVICE_NAMES = listOf("gbs-2012-b", "gbf-2008-bf")

    override val scanFilters: List<ScanFilter> = DEVICE_NAMES
        .flatMap { deviceName -> listOf(deviceName, deviceName.uppercase()) }
        .map { deviceName -> buildScanFilter { setDeviceName(deviceName) } }

    override fun ConfigurationScope.init() {
        service(uuid = LX_SERVICE.uuid) {
            val lxTransfer = characteristic(uuid = LX_ACK_TRANSFER_DEVICE_TO_APP.uuid) {
                notificationCallback(
                    packet = GBS2012BPacket.Definition,
                    callback = { packet ->
                        logI("notification packet in lxTransfer: $packet")
                    }
                )
            }
            val lxSendCommand = characteristic(uuid = LX_SEND_COMMAND.uuid)

            characteristic(uuid = LX_NOTIFY.uuid) {
                notificationCallback(
                    packet = GBS2012BPacket.Definition,
                    callback = { packet ->
                        logI("notification packet: $packet")

                        packet.handle(lxTransfer = lxTransfer, lxSendCommand = lxSendCommand)
                    }
                )
            }
            characteristic(uuid = LX_INDICATE.uuid) {
                indicationCallback(
                    packet = GBS2012BPacket.Definition,
                    callback = { packet ->
                        logI("indication packet: $packet")

                        packet.handle(lxTransfer = lxTransfer, lxSendCommand = lxSendCommand)
                    }
                )
            }
            characteristic(uuid = LX_READ_FEATURE.uuid).run {
                readPacket(packet = BlePacket.Definition.default) { packet ->
                    logD("ReadFeature: ${packet.bytes}")
                }
            }
        }
    }

    override fun onInvalidated() {
        logI("onInvalidated called")
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun isDeviceSupported(discoveredDevice: DiscoveredBluetoothDevice): Boolean {
        logV("isDeviceSupported called: ${discoveredDevice.device.name}")

        return discoveredDevice.matchesAnyPrefix(prefixList = DEVICE_NAMES)
    }

    private fun Filled.handle(
        lxTransfer: GattCharacteristic,
        lxSendCommand: GattCharacteristic,
    ) {
        when (this) {
            BindResult -> {
                lxTransfer.writePacket(packet = GBS2012BPacket.ackResponse)

                lxSendCommand.writePacket(packet = GBS2012BPacket.requestMeasurement)
            }

            is ReceiverAuth -> {
                lxTransfer.writePacket(packet = GBS2012BPacket.ackResponse)
                lxSendCommand.writePacket(
                    packet = GBS2012BPacket.loginReply(
                        verifyCode = verifyCode.toByteArray()
                    )
                )
            }

            ReceiverInit -> {
                lxTransfer.writePacket(packet = GBS2012BPacket.ackResponse)

                lxSendCommand.writePacket(packet = GBS2012BPacket.dateTimeAlt)
            }

            RegistrationResponse -> {
                lxTransfer.writePacket(packet = GBS2012BPacket.ackResponse)
            }

            is SettingCallback -> {
                lxTransfer.writePacket(packet = GBS2012BPacket.ackResponse)

                when (settingAck) {
                    SettingAck.PUSH_TIME_TO_WEIGHT -> {
                        lxSendCommand.writePacket(packet = GBS2012BPacket.weightUserInformation)
                    }

                    SettingAck.PUSH_USER_INFO_TO_WEIGHT -> {
                        lxSendCommand.writePacket(packet = GBS2012BPacket.unitOfMeasure)
                    }

                    SettingAck.PUSH_UNIT_TO_WEIGHT -> {
                        lxSendCommand.writePacket(packet = GBS2012BPacket.bindDevice)
                    }

                    SettingAck.UNKNOWN -> {
                        logW("Unknown settingAck")
                    }
                }
            }

            is WeightData -> {
                lxTransfer.writePacket(packet = GBS2012BPacket.ackResponse)
            }

            Unknown -> {
                logW("Unknown packet")
            }
        }
    }

    companion object {

        private val LX_SERVICE = Service("a602", "LX")

        private val LX_INDICATE = Characteristic(
            "a620",
            "Lx Indicate",
            DefaultAttribute::class.java,
            *arrayOfNulls(0),
        )

        private val LX_NOTIFY = Characteristic(
            "a621",
            "Lx Notify",
            DefaultAttribute::class.java,
            *arrayOfNulls(0),
        )

        private val LX_ACK_TRANSFER_APP_TO_DEVICE = Characteristic(
            "a622",

            "Lx Ack Device to App",
            DefaultAttribute::class.java,
            *arrayOfNulls(0),
        )

        private val LX_SEND_INFO = Characteristic(
            "a623",
            "Lx SendInfo",
            DefaultAttribute::class.java,
            *arrayOfNulls(0),
        )

        private val LX_SEND_COMMAND = Characteristic(
            "a624",
            "Lx SendCommand",
            DefaultAttribute::class.java,
            *arrayOfNulls(0),
        )

        private val LX_ACK_TRANSFER_DEVICE_TO_APP = Characteristic(
            "a625",
            "Lx Ack App to Device",
            DefaultAttribute::class.java,
            *arrayOfNulls(0),
        )

        private val LX_READ_FEATURE = Characteristic(
            "a641",
            "Lx Read feature",
            DefaultAttribute::class.java,
            *arrayOfNulls(0),
        )
    }
}
