package com.donatoni.torsiondetector.ui.wrappers.bluetooth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.donatoni.torsiondetector.io.bluetooth.BluetoothStateBroadcast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface BluetoothWrapperUiState {
    object On : BluetoothWrapperUiState
    object Off : BluetoothWrapperUiState
}

private data class BluetoothWrapperViewModelState(
    val isBluetoothEnabled: Boolean = false,
) {
    fun toUiState(): BluetoothWrapperUiState {
        return if (isBluetoothEnabled) {
            BluetoothWrapperUiState.On
        } else {
            BluetoothWrapperUiState.Off
        }
    }
}

@HiltViewModel
class BluetoothWrapperViewModel @Inject constructor(
    private val bluetoothStateBroadcast: BluetoothStateBroadcast,
) : ViewModel() {

    private val viewModelState = MutableStateFlow(
        BluetoothWrapperViewModelState(false)
    )

    val uiState = viewModelState
        .map(BluetoothWrapperViewModelState::toUiState)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            viewModelState.value.toUiState()
        )

    init {
        listenToBluetoothState()
    }

    private fun listenToBluetoothState() {
        viewModelScope.launch {
            bluetoothStateBroadcast.isBluetoothEnabled.collect { enabled ->
                viewModelState.update {
                    it.copy(isBluetoothEnabled = enabled)
                }
            }
        }
    }
}