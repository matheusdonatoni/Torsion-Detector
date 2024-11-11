package com.donatoni.torsiondetector.ui.screens.home

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.donatoni.torsiondetector.io.bluetooth.BLEManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

const val TAG = "HomeViewModel"

sealed interface HomeUiState {
    val scanning: Boolean
    val errorMessage: String?

    val hasError: Boolean
        get() = errorMessage != null

    data class NoDevices(
        override val scanning: Boolean,
        override val errorMessage: String? = null,
    ) : HomeUiState

    data class HasDevices(
        val connectedDevices: List<BluetoothDevice>,
        override val scanning: Boolean,
        override val errorMessage: String? = null,
    ) : HomeUiState
}

private data class HomeViewModelState(
    val connectedDevices: Set<BluetoothDevice> = emptySet(),
    val scanning: Boolean = false,
    val errorMessage: String? = null
) {

    private val isEmpty: Boolean
        get() = connectedDevices.isEmpty()

    fun toUiState(): HomeUiState {
        return if (isEmpty) {
            HomeUiState.NoDevices(
                scanning = scanning,
                errorMessage = errorMessage,
            )
        } else {
            HomeUiState.HasDevices(
                connectedDevices = connectedDevices.toList(),
                scanning = scanning,
                errorMessage = errorMessage,
            )
        }
    }
}


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bleManager: BLEManager,
) : ViewModel() {
    private val viewModelState = MutableStateFlow(
        HomeViewModelState()
    )

    val uiState = viewModelState
        .map(HomeViewModelState::toUiState)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            viewModelState.value.toUiState()
        )

    init {
        listenToConnectedDevices()
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
}