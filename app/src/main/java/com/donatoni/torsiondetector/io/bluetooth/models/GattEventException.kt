package com.donatoni.torsiondetector.io.bluetooth.models

import java.util.UUID

sealed class GattEventException(override val message: String) : Exception() {
    class MissingGattException(gattType: String, addressOrUuid: String) : GattEventException(
        "Could not find any Gatt for the given Parcelable: $gattType - $addressOrUuid"
    )

    class OnDeviceConnectionException(name: String?, address: String) : GattEventException(
        "Could not connect to device $name - $address"
    )

    class OnDeviceDisconnectionException(name: String?, address: String) : GattEventException(
        "Could not disconnect from device $name - $address"
    )

    class OnDiscoverServicesException(name: String?, address: String) : GattEventException(
        "Could not discover services from device $name - $address"
    )

    sealed class OnNotifyCharacteristicException(message: String) : GattEventException(
        message
    )

    class CharacteristicUnreportedException(uuid: UUID) : OnNotifyCharacteristicException(
        "Characteristic $uuid do not support notification"
    )

    class OnCharacteristicReadException(uuid: UUID) : GattEventException(
        "Failed to read characteristic $uuid"
    )

    class OnCharacteristicWriteException(uuid: UUID) : GattEventException(
        "Failed to write characteristic $uuid"
    )

    class OnDescriptorReadException(uuid: UUID) : GattEventException(
        "Failed to read descriptor $uuid"
    )

    class OnDescriptorWriteException(uuid: UUID) : GattEventException(
        "Failed to write to descriptor $uuid"
    )
}