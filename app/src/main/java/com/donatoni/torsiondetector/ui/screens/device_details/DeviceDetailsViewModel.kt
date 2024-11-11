package com.donatoni.torsiondetector.ui.screens.device_details

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.donatoni.torsiondetector.io.bluetooth.BLEManager
import com.donatoni.torsiondetector.io.bluetooth.gatts.*
import com.donatoni.torsiondetector.models.TorsionData
import com.donatoni.torsiondetector.ui.navigation.DeviceDetails
import com.donatoni.torsiondetector.utils.exts.measurementsToCSV
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "DeviceDetailsViewModel"

sealed interface DeviceDetailsUiState {
    val loading: Boolean
    val errorMessage: String?

    val hasError: Boolean
        get() = errorMessage != null

    data class NoDevice(
        override val loading: Boolean,
        override val errorMessage: String? = null,
    ) : DeviceDetailsUiState

    data class HasDevice(
        val device: BluetoothDevice,
        val scaleService: BluetoothGattService?,
        val dataFlow: StateFlow<List<TorsionData>>?,
        val autoMode: Boolean,
        override val loading: Boolean,
        override val errorMessage: String? = null,
    ) : DeviceDetailsUiState
}

private data class DeviceDetailsViewModelState(
    val device: BluetoothDevice? = null,
    val scaleService: BluetoothGattService? = null,
    val scaleCharacteristic: BluetoothGattCharacteristic? = null,
    val autoModeCharacteristic: BluetoothGattCharacteristic? = null,
    val scaleCommandCharacteristic: BluetoothGattCharacteristic? = null,
    val dataCharacteristic: BluetoothGattCharacteristic? = null,
    val dataFlow: MutableStateFlow<List<TorsionData>> = MutableStateFlow(emptyList()),
    val autoMode: Boolean = false,
    val loading: Boolean = false,
    val errorMessage: String? = null
) {
    fun toUiState(): DeviceDetailsUiState {
        return if (device != null) {
            DeviceDetailsUiState.HasDevice(
                device = device,
                scaleService = scaleService,
                dataFlow = dataFlow,
                autoMode = autoMode,
                loading = loading,
                errorMessage = errorMessage,
            )
        } else {
            DeviceDetailsUiState.NoDevice(
                loading = loading,
                errorMessage = errorMessage,
            )
        }
    }
}

@HiltViewModel
class DeviceDetailsViewModel @Inject constructor(
    private val bleManager: BLEManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val viewModelState = MutableStateFlow(
        DeviceDetailsViewModelState()
    )

    private val scaleServiceInfo = ScaleServiceInfo()
    private val scaleCharacteristicInfo = ScaleCharacteristicInfo()
    private val autoModeCharacteristicInfo = AutoModeCharacteristicInfo()
    private val scaleCommandCharacteristicInfo = ScaleCommandCharacteristicInfo()

    val uiState = viewModelState.map(
        DeviceDetailsViewModelState::toUiState
    ).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000L),
        viewModelState.value.toUiState(),
    )

    init {
        findSelectedDeviceAndServices(savedStateHandle[DeviceDetails.deviceAddressArg])
    }

    private val scaleCommandCharacteristic: BluetoothGattCharacteristic?
        get() = viewModelState.value.scaleCommandCharacteristic

    private val autoModeCharacteristic: BluetoothGattCharacteristic?
        get() = viewModelState.value.autoModeCharacteristic

    private val scaleCharacteristic: BluetoothGattCharacteristic?
        get() = viewModelState.value.scaleCharacteristic

    private suspend fun setCharacteristicNotification(
        vararg characteristics: BluetoothGattCharacteristic, enable: Boolean
    ) {
        characteristics.forEach {
            val result = bleManager.setCharacteristicNotification(it, enable)

            Log.i(TAG, "Setting ${it.uuid} to $enable completed -> $result")
        }
    }

    private fun findSelectedDeviceAndServices(deviceAddress: String?) {
        if (deviceAddress != null) {
            val device = bleManager.connectedDevices.value.firstOrNull {
                it.address == deviceAddress
            }

            viewModelState.update { it.copy(device = device) }

            if (device != null) {
                viewModelScope.launch {
                    val services = bleManager.discoverServices(device)

                    val scaleService = services.firstOrNull {
                        it.uuid == scaleServiceInfo.uuid
                    }

                    if (scaleService != null) {
                        val scaleCharacteristic = scaleService.characteristics.firstOrNull {
                            it.uuid == scaleCharacteristicInfo.uuid
                        }

                        val autoModeCharacteristic = scaleService.characteristics.firstOrNull {
                            it.uuid == autoModeCharacteristicInfo.uuid
                        }

                        val scaleCommandCharacteristic = scaleService.characteristics.firstOrNull {
                            it.uuid == scaleCommandCharacteristicInfo.uuid
                        }

                        if (scaleCharacteristic != null && scaleCommandCharacteristic != null) {
                            viewModelState.update {
                                it.copy(
                                    scaleService = scaleService,
                                    scaleCharacteristic = scaleCharacteristic,
                                    autoModeCharacteristic = autoModeCharacteristic,
                                    scaleCommandCharacteristic = scaleCommandCharacteristic,
                                )
                            }

                            listenAutoModeUpdates()
                            listenScaleUpdates()
                        }
                    }
                }
            }
        }
    }

    private fun listenAutoModeUpdates() {
        Log.i(TAG, "autoModeCharacteristic exists: ${autoModeCharacteristic != null}")
        if (autoModeCharacteristic != null) {
            viewModelScope.launch {
                bleManager.getCharacteristicValueFlow(autoModeCharacteristic!!).map {
                    autoModeCharacteristicInfo.valueFromBytes(it)
                }.collect { struct ->
                    Log.i(TAG, "Struct received: ${struct.autoMode}")

                    viewModelState.update {
                        it.copy(
                            autoMode = struct.autoMode
                        )
                    }
                }
            }

            viewModelScope.launch {
                val autoMode = autoModeCharacteristicInfo.valueFromBytes(
                    bleManager.readCharacteristic(autoModeCharacteristic!!)
                ).autoMode

                Log.i(TAG, "Auto mode first launch: $autoMode")

                viewModelState.update {
                    it.copy(
                        autoMode = autoMode
                    )
                }
            }

            viewModelScope.launch {
                Log.i(TAG, "Enable autoMode notification")
                setCharacteristicNotification(autoModeCharacteristic!!, enable = true)
            }
        }
    }

    private fun listenScaleUpdates() {
        if (scaleCharacteristic != null) {
            viewModelScope.launch {
                bleManager.getCharacteristicValueFlow(scaleCharacteristic!!).collect {
                    val dataHistory = viewModelState.value.dataFlow.value.toMutableList()

                    dataHistory.apply {
                        if (isEmpty()) {
                            add(
                                TorsionData(
                                    scaleCharacteristicInfo.valueFromBytes(it),
                                    scaleCharacteristicInfo.valueFromBytes(it)
                                )
                            )
                        } else {
                            add(
                                TorsionData(
                                    dataHistory.first().initialCondition,
                                    scaleCharacteristicInfo.valueFromBytes(it)
                                )
                            )
                        }
                    }.toList()

                    viewModelState.value.dataFlow.update {
                        dataHistory
                    }
                }
            }

            viewModelScope.launch {
                setCharacteristicNotification(
                    scaleCharacteristic!!,
                    enable = true
                )
            }
        }
    }

    fun requestScaleSingleRead() {
        if (scaleCommandCharacteristic != null) {
            viewModelScope.launch {
                writeCharacteristic(
                    scaleCommandCharacteristic!!, ScaleCommandCharacteristicInfo.READ_SCALE_ONCE
                )
            }
        }
    }

    fun requestScaleAutoRead() {
        if (scaleCommandCharacteristic != null) {
            val command = if (viewModelState.value.autoMode) {
                ScaleCommandCharacteristicInfo.READ_SCALE_AUTOMATICALLY_FALSE
            } else {
                ScaleCommandCharacteristicInfo.READ_SCALE_AUTOMATICALLY_TRUE
            }

            viewModelScope.launch {
                writeCharacteristic(
                    scaleCommandCharacteristic!!, command
                )
            }
        }
    }

    fun requestRestart() {
        if (scaleCommandCharacteristic != null) {
            val command = ScaleCommandCharacteristicInfo.RESTART_MEASUREMENTS

            viewModelScope.launch {
                writeCharacteristic(
                    scaleCommandCharacteristic!!, command
                )
            }

            viewModelState.value.dataFlow.update {
                emptyList()
            }
        }
    }

    fun onSaveLocallyClick(context: Context, uri: Uri, measurement: List<TorsionData>) {
        viewModelScope.launch {
            val outputStream = context.contentResolver.openOutputStream(uri)

            outputStream?.let { stream ->
                withContext(Dispatchers.IO) {
                    stream.write(measurementsToCSV(measurement).toByteArray())
                    stream.close()

                }
            }
        }
    }

    private suspend fun writeCharacteristic(
        characteristic: BluetoothGattCharacteristic, value: ByteArray
    ): Boolean {
        return bleManager.writeCharacteristic(characteristic, value)
    }
}



