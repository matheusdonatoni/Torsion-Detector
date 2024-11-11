package com.donatoni.torsiondetector.io.bluetooth.models

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

private const val TAG = "GattEvent"

sealed interface GattEvent {
    data class OnConnectionStateChange(
        val gatt: BluetoothGatt,
        val status: Int,
        val newState: Int,
    ) : GattEvent

    data class OnReadRemoteRssi(
        val gatt: BluetoothGatt, val rssi: Int, val status: Int
    ) : GattEvent

    data class OnMtuChanged(
        val gatt: BluetoothGatt, val mtu: Int, val status: Int
    ) : GattEvent

    data class OnServicesDiscovered(
        val gatt: BluetoothGatt, val status: Int
    ) : GattEvent

    data class OnServiceChanged(val gatt: BluetoothGatt) : GattEvent

    data class OnCharacteristicWrite(
        val gatt: BluetoothGatt, val characteristic: BluetoothGattCharacteristic, val status: Int
    ) : GattEvent

    data class OnCharacteristicRead(
        val gatt: BluetoothGatt,
        val characteristic: BluetoothGattCharacteristic,
        val value: ByteArray,
        val status: Int
    ) : GattEvent {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as OnCharacteristicRead

            if (gatt != other.gatt) return false
            if (characteristic != other.characteristic) return false
            if (!value.contentEquals(other.value)) return false
            if (status != other.status) return false

            return true
        }

        override fun hashCode(): Int {
            var result = gatt.hashCode()
            result = 31 * result + characteristic.hashCode()
            result = 31 * result + value.contentHashCode()
            result = 31 * result + status
            return result
        }
    }

    data class OnCharacteristicChanged(
        val gatt: BluetoothGatt,
        val characteristic: BluetoothGattCharacteristic,
        val value: ByteArray
    ) : GattEvent {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as OnCharacteristicChanged

            if (gatt != other.gatt) return false
            if (characteristic != other.characteristic) return false
            if (!value.contentEquals(other.value)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = gatt.hashCode()
            result = 31 * result + characteristic.hashCode()
            result = 31 * result + value.contentHashCode()
            return result
        }
    }

    data class OnDescriptorWrite(
        val gatt: BluetoothGatt, val descriptor: BluetoothGattDescriptor, val status: Int
    ) : GattEvent

    data class OnDescriptorRead(
        val gatt: BluetoothGatt,
        val descriptor: BluetoothGattDescriptor,
        val status: Int,
        val value: ByteArray
    ) : GattEvent {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as OnDescriptorRead

            if (gatt != other.gatt) return false
            if (descriptor != other.descriptor) return false
            if (status != other.status) return false
            if (!value.contentEquals(other.value)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = gatt.hashCode()
            result = 31 * result + descriptor.hashCode()
            result = 31 * result + status
            result = 31 * result + value.contentHashCode()
            return result
        }
    }

    data class OnPhyRead(
        val gatt: BluetoothGatt, val txPhy: Int, val rxPhy: Int, val status: Int
    ) : GattEvent

    data class OnPhyUpdate(
        val gatt: BluetoothGatt, val txPhy: Int, val rxPhy: Int, val status: Int
    ) : GattEvent

    data class OnReliableWriteCompleted(
        val gatt: BluetoothGatt, val status: Int
    ) : GattEvent
}


class BluetoothGattCallbackFlow : BluetoothGattCallback() {
    private val _emitter = MutableSharedFlow<GattEvent>(1)
    val emitter: SharedFlow<GattEvent> = _emitter

    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        _emitter.tryEmit(
            GattEvent.OnConnectionStateChange(gatt, status, newState)
        )
    }

    override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
        _emitter.tryEmit(
            GattEvent.OnReadRemoteRssi(gatt, rssi, status)
        )
    }

    override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
        _emitter.tryEmit(
            GattEvent.OnMtuChanged(gatt, mtu, status)
        )
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        _emitter.tryEmit(
            GattEvent.OnServicesDiscovered(gatt, status)
        )
    }

    override fun onServiceChanged(gatt: BluetoothGatt) {
        _emitter.tryEmit(
            GattEvent.OnServiceChanged(gatt)
        )

    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int
    ) {
        _emitter.tryEmit(
            GattEvent.OnCharacteristicWrite(gatt, characteristic, status)
        )
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        status: Int
    ) {
        _emitter.tryEmit(
            GattEvent.OnCharacteristicRead(gatt, characteristic, value, status)
        )
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray
    ) {
        runBlocking(Dispatchers.Default) {
            _emitter.emit(GattEvent.OnCharacteristicChanged(gatt, characteristic, value))
        }
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int
    ) {
        _emitter.tryEmit(
            GattEvent.OnDescriptorWrite(gatt, descriptor, status)
        )
    }

    override fun onDescriptorRead(
        gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int, value: ByteArray
    ) {
        _emitter.tryEmit(
            GattEvent.OnDescriptorRead(gatt, descriptor, status, value)
        )
    }

    override fun onPhyRead(gatt: BluetoothGatt, txPhy: Int, rxPhy: Int, status: Int) {
        _emitter.tryEmit(
            GattEvent.OnPhyRead(gatt, txPhy, rxPhy, status)
        )
    }

    override fun onPhyUpdate(gatt: BluetoothGatt, txPhy: Int, rxPhy: Int, status: Int) {
        _emitter.tryEmit(
            GattEvent.OnPhyUpdate(gatt, txPhy, rxPhy, status)
        )
    }

    override fun onReliableWriteCompleted(gatt: BluetoothGatt, status: Int) {
        _emitter.tryEmit(
            GattEvent.OnReliableWriteCompleted(gatt, status)
        )
    }
}