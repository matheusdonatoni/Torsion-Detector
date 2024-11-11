package com.donatoni.torsiondetector.io.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanResult

import android.content.Context
import android.os.Build
import android.util.Log
import com.donatoni.torsiondetector.io.bluetooth.models.*
import com.donatoni.torsiondetector.utils.exts.isIndictable
import com.donatoni.torsiondetector.utils.exts.isNotifiable
import com.donatoni.torsiondetector.utils.exts.lockWithTimeout
import com.donatoni.torsiondetector.utils.getAllConnectedDevices
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.experimental.and

private const val TAG = "BLEManager"
private const val CCCD_UUID = "00002902-0000-1000-8000-00805f9b34fb"

@SuppressLint("MissingPermission")
@Singleton
class BLEManager @Inject constructor(
    private val bluetoothManager: BluetoothManager,
    @ApplicationContext private val context: Context,
) {
    // Coroutine variables
    private val scope = CoroutineScope(Dispatchers.IO)
    private val gattMutex = Mutex()
    private var scanJob: Job? = null

    // Callback flows
    private val scanCallbackFlow = ScanCallbackFlow(scope)
    private val gattCallbackFlow = BluetoothGattCallbackFlow()

    // Bluetooth adapter
    private val bluetoothAdapter: BluetoothAdapter
        get() = bluetoothManager.adapter

    // Stores every connected device Gatt for performing BLE operations
    private val gatts = mutableListOf<BluetoothGatt>()

    init {
        scope.launch {
            gattCallbackFlow.emitter.collect(::onDevicesConnectionStateChange)
        }
    }

    // Bluetooth scanner
    private val scanner = bluetoothAdapter.bluetoothLeScanner
    private val _scanning = MutableStateFlow(false)
    val scanning: StateFlow<Boolean> = _scanning

    // Scan results flow
    private val _scanResultSet: MutableSet<ScanResult> = mutableSetOf()
    private val _scanResults = MutableStateFlow<Set<ScanResult>>(emptySet())
    val scanResults: StateFlow<Set<ScanResult>> = _scanResults.map { set ->
        set.sortedBy { it.device.name }.toSet()
    }.stateIn(scope, SharingStarted.WhileSubscribed(5_000L), emptySet())

    // Connected devices flow
    private val _connectedDevices = MutableStateFlow<Set<BluetoothDevice>>(emptySet())
    val connectedDevices: StateFlow<Set<BluetoothDevice>> = _connectedDevices.map { set ->
        set.sortedBy { it.name }.toSet()
    }.stateIn(scope, SharingStarted.WhileSubscribed(5_000L), emptySet())

    // Listen to any device connection state change
    private fun onDevicesConnectionStateChange(event: GattEvent) {
        if (event is GattEvent.OnConnectionStateChange) {
            _connectedDevices.update { bluetoothManager.getAllConnectedDevices() }
        }
    }

    fun startScan(
        timeout: Long = 5_000L,
    ) {
        if (!scanning.value) {
            scanJob = scope.launch {
                withTimeoutOrNull(timeout) {
                    scanCallbackFlow.emitter.onSubscription {
                        _scanResultSet.clear()
                        scanner.startScan(scanCallbackFlow)
                        _scanning.update { true }
                    }.onCompletion {
                        stopScan()
                    }.collect {
                        onScanCallbackEvent(it)
                    }
                }
            }

            Log.i(TAG, "Scanning")
        }
    }

    fun stopScan() {
        if (scanning.value) {
            scanner.stopScan(scanCallbackFlow)
            _scanning.update { false }

            scope.launch {
                scanJob?.cancelAndJoin()
                scanJob = null

                Log.i(TAG, "Finished scanning")
            }
        }
    }


    private suspend fun onScanCallbackEvent(scanEvent: ScanEvent) {
        when (scanEvent) {
            is ScanEvent.OnScanResult -> {
                val result = scanEvent.result
                Log.i(TAG, "Result: ${result.device.name} - ${result.device.address}")

                _scanResultSet.removeIf { it.device == result.device }
                _scanResultSet.add(result)

                scope.launch {
                    _scanResults.emit(_scanResultSet.toSet())
                }
            }
            else -> Unit
        }
    }

    suspend fun connect(device: BluetoothDevice, timeout: Long = 3_000L) {
        try {
            val result = gattMutex.lockWithTimeout(timeout) {
                gattCallbackFlow.emitter.onSubscription {
                    Log.i(
                        TAG, "Performing connection to device: ${device.name}, ${device.address}"
                    )

                    gattFrom(device)?.connect() ?: gatts.add(
                        device.connectGatt(
                            context,
                            true,
                            gattCallbackFlow,
                            BluetoothDevice.TRANSPORT_LE,
                        )
                    )
                }.first {
                    it is GattEvent.OnConnectionStateChange && it.gatt.device == device && it.newState == BluetoothGatt.STATE_CONNECTED
                } as GattEvent.OnConnectionStateChange
            }

            if (result.status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Connected to device: ${device.name}, ${device.address}")

                return
            }

            throw GattEventException.OnDeviceConnectionException(
                device.name ?: "Unknown", device.address
            )
        } catch (exception: Exception) {
            Log.i(
                TAG, exception.message.toString()
            )
        }
    }

    suspend fun disconnect(device: BluetoothDevice) {
        withTimeoutOrNull(1_000L) {
            Log.i(
                TAG, "Disconnecting from device: ${device.name}, ${device.address}"
            )

            val result = gattCallbackFlow.emitter.onSubscription {
                gattFrom(device)?.disconnect()
            }.first {
                it is GattEvent.OnConnectionStateChange && it.gatt.device == device && it.newState == BluetoothGatt.STATE_DISCONNECTED
            } as GattEvent.OnConnectionStateChange

            if (result.status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(
                    TAG, "Disconnected from device: ${device.name}, ${device.address}"
                )
            }
        }
    }

    fun deviceIsConnected(device: BluetoothDevice): Boolean =
        bluetoothManager.getAllConnectedDevices().contains(device)

    suspend fun discoverServices(device: BluetoothDevice): List<BluetoothGattService> {
        gattFrom(device)?.let { gatt ->
            val result = gattMutex.lockWithTimeout {
                gattCallbackFlow.emitter.onSubscription {
                    gatt.discoverServices()
                }.first {
                    it is GattEvent.OnServicesDiscovered && it.gatt.device == device
                } as GattEvent.OnServicesDiscovered
            }

            if (result.status == BluetoothGatt.GATT_SUCCESS) {
                return gatt.services
            }

            throw GattEventException.OnDiscoverServicesException(
                device.name ?: "Unknown", device.address
            )
        } ?: throw GattEventException.MissingGattException(
            device.javaClass.toString(), device.address
        )
    }

    suspend fun readCharacteristic(characteristic: BluetoothGattCharacteristic): ByteArray {
        gattFrom(characteristic)?.let { gatt ->
            val result = gattMutex.lockWithTimeout {
                gattCallbackFlow.emitter.onSubscription {
                    gatt.readCharacteristic(characteristic)
                }.first {
                    it is GattEvent.OnCharacteristicRead && it.characteristic == characteristic
                } as GattEvent.OnCharacteristicRead
            }

            if (result.status == BluetoothGatt.GATT_SUCCESS) {
                return result.value
            }

            throw GattEventException.OnCharacteristicReadException(characteristic.uuid)
        } ?: throw GattEventException.MissingGattException(
            characteristic.javaClass.toString(), characteristic.uuid.toString()
        )
    }

    suspend fun writeCharacteristic(
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        writeType: Int = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT,
    ): Boolean {
        gattFrom(characteristic)?.let { gatt ->
            val result = gattMutex.lockWithTimeout {
                gattCallbackFlow.emitter.onSubscription {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        gatt.writeCharacteristic(
                            characteristic,
                            value,
                            writeType
                        )
                    } else {
                        characteristic.value = value
                        characteristic.writeType = writeType
                        gatt.writeCharacteristic(characteristic)
                    }
                }.first {
                    it is GattEvent.OnCharacteristicWrite && it.characteristic == characteristic
                } as GattEvent.OnCharacteristicWrite
            }

            if (result.status == BluetoothGatt.GATT_SUCCESS) {
                return true
            }

            throw GattEventException.OnCharacteristicWriteException(characteristic.uuid)
        } ?: throw GattEventException.MissingGattException(
            characteristic.javaClass.toString(), characteristic.uuid.toString()
        )
    }

    suspend fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic, enable: Boolean
    ): Boolean {
        val gatt = gattFrom(characteristic)

        if (gatt != null) {
            val payload = if (enable) {
                when {
                    characteristic.isNotifiable() -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    characteristic.isIndictable() -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                    else -> return false
                }
            } else {
                BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            }

            val cccd = UUID.fromString(CCCD_UUID)

            characteristic.getDescriptor(cccd)?.let { it ->
                if (!gatt.setCharacteristicNotification(characteristic, enable)) {
                    Log.d(TAG, "Set characteristics notification failed")
                    return false
                }

                return writeDescriptor(it, payload)
            }
        }

        return false
    }

    suspend fun characteristicIsNotifying(characteristic: BluetoothGattCharacteristic): Boolean {
        gattFrom(characteristic)?.let {
            val cccd = UUID.fromString(CCCD_UUID)

            return characteristic.getDescriptor(cccd)?.let { descriptor ->
                val content = readDescriptor(descriptor)

                val firstCondition = content[0].and(0x01) > 0
                val secondCondition = content[0].and(0x02) > 0

                firstCondition || secondCondition
            } ?: false
        } ?: throw GattEventException.MissingGattException(
            characteristic.javaClass.toString(), characteristic.uuid.toString()
        )
    }

    fun getCharacteristicValueFlow(characteristic: BluetoothGattCharacteristic): Flow<ByteArray> {
        return gattCallbackFlow.emitter.transform {
            if (it is GattEvent.OnCharacteristicChanged && it.characteristic == characteristic) {
                emit(it.value)
            }
        }
    }

    suspend fun writeDescriptor(descriptor: BluetoothGattDescriptor, value: ByteArray): Boolean {
        gattFrom(descriptor)?.let { gatt ->
            Log.i(TAG, "Before lock")
            val result = gattMutex.lockWithTimeout {
                gattCallbackFlow.emitter.onSubscription {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        gatt.writeDescriptor(descriptor, value)
                    } else {
                        descriptor.value = value
                        gatt.writeDescriptor(descriptor)
                    }
                }.first {
                    it is GattEvent.OnDescriptorWrite && it.descriptor == descriptor
                } as GattEvent.OnDescriptorWrite
            }

            Log.i(TAG, "after lock")

            if (result.status == BluetoothGatt.GATT_SUCCESS) {
                return true
            }

            throw GattEventException.OnDescriptorWriteException(descriptor.uuid)
        } ?: throw GattEventException.MissingGattException(
            gattType = descriptor.javaClass.toString(),
            addressOrUuid = descriptor.uuid.toString(),
        )
    }

    suspend fun readDescriptor(descriptor: BluetoothGattDescriptor): ByteArray {
        gattFrom(descriptor)?.let { gatt ->
            val result = gattMutex.lockWithTimeout {
                gattCallbackFlow.emitter.onSubscription {
                    gatt.readDescriptor(descriptor)
                }
            }.first {
                it is GattEvent.OnDescriptorRead && it.descriptor == descriptor
            } as GattEvent.OnDescriptorRead

            if (result.status == BluetoothGatt.GATT_SUCCESS) {
                return result.value
            }

            throw GattEventException.OnDescriptorReadException(descriptor.uuid)
        } ?: throw GattEventException.MissingGattException(
            descriptor.javaClass.toString(), descriptor.uuid.toString()
        )
    }

    private fun gattFrom(device: BluetoothDevice): BluetoothGatt? {
        return gatts.firstOrNull { gatt ->
            gatt.device == device
        }
    }

    private fun gattFrom(service: BluetoothGattService): BluetoothGatt? {
        return gatts.firstOrNull { gatt ->
            gatt.services.contains(service)
        }
    }

    private fun gattFrom(characteristic: BluetoothGattCharacteristic): BluetoothGatt? {
        return gatts.firstOrNull { gatt ->
            gatt.services.contains(characteristic.service)
        }
    }

    private fun gattFrom(descriptor: BluetoothGattDescriptor): BluetoothGatt? = gattFrom(
        descriptor.characteristic
    )
}
