package com.donatoni.torsiondetector.ui.screens.discover_devices

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.donatoni.torsiondetector.io.bluetooth.BLEManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

const val TAG = "DiscoverDevicesViewModel"

sealed interface DiscoverDevicesUiState {
    val scanning: Boolean
    val errorMessage: String?

    val hasError: Boolean
        get() = errorMessage != null

    data class NoDevices(
        override val scanning: Boolean,
        override val errorMessage: String? = null,
    ) : DiscoverDevicesUiState

    data class HasDevices(
        val scanResults: List<ScanResult>,
        val connectedDevices: List<BluetoothDevice>,
        override val scanning: Boolean,
        override val errorMessage: String? = null,
    ) : DiscoverDevicesUiState
}

private data class DiscoverDevicesViewModelState(
    val scanResults: Set<ScanResult> = emptySet(),
    val connectedDevices: Set<BluetoothDevice> = emptySet(),
    val scanning: Boolean = false,
    val errorMessage: String? = null
) {
    private val isEmpty: Boolean
        get() = scanResults.isEmpty() && connectedDevices.isEmpty()

    fun toUiState(): DiscoverDevicesUiState {
        return if (isEmpty) {
            DiscoverDevicesUiState.NoDevices(
                scanning = scanning,
                errorMessage = errorMessage,
            )
        } else {
            DiscoverDevicesUiState.HasDevices(
                scanResults = scanResults.toList(),
                connectedDevices = connectedDevices.toList(),
                scanning = scanning,
                errorMessage = errorMessage,
            )
        }
    }
}

@HiltViewModel
class DiscoverDevicesViewModel @Inject constructor(
    private val bleManager: BLEManager,
) : ViewModel() {
    private val viewModelState = MutableStateFlow(
        DiscoverDevicesViewModelState()
    )

    val uiState = viewModelState.map(DiscoverDevicesViewModelState::toUiState).stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), viewModelState.value.toUiState()
    )

    init {
        listenToScanState()
        listenToConnectedDevices()
        listenToScanResults()

        startScan()
    }

    fun startScan() = bleManager.startScan()

    fun stopScan() = bleManager.stopScan()

    fun onDeviceTileClick(device: BluetoothDevice) {
        if (bleManager.deviceIsConnected(device)) {
            viewModelScope.launch { bleManager.disconnect(device) }
        } else {
            viewModelScope.launch { bleManager.connect(device) }
        }
    }

    private fun listenToConnectedDevices() {
        viewModelScope.launch(Dispatchers.IO) {
            bleManager.connectedDevices.collect { devices ->
                viewModelState.update {
                    it.copy(connectedDevices = devices)
                }
            }
        }
    }

    private fun listenToScanResults() {
        viewModelScope.launch(Dispatchers.IO) {
            bleManager.scanResults.collect { scanResults ->
                viewModelState.update {
                    it.copy(scanResults = scanResults)
                }
            }
        }
    }

    private fun listenToScanState() {
        viewModelScope.launch(Dispatchers.IO) {
            bleManager.scanning.collect { scanning ->
                viewModelState.update {
                    it.copy(scanning = scanning)
                }
            }
        }
    }
}