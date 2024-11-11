package com.donatoni.torsiondetector.io.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

@AndroidEntryPoint
class BluetoothStateBroadcast(
    initialState: Boolean,
) : BroadcastReceiver() {
    // Intent bluetooth filter
    private val bluetoothFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)

    // State Flow
    private val _isBluetoothEnabled = MutableStateFlow(initialState)
    val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled
    private fun updateState(enabled: Boolean) = _isBluetoothEnabled.update { enabled }

    fun register(context: Context) = context.registerReceiver(this, bluetoothFilter)
    fun unregister(context: Context) = context.unregisterReceiver(this)

    override fun onReceive(context: Context?, intent: Intent?) {
        val state = intent?.getIntExtra(
            BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR
        )

        when (state) {
            BluetoothAdapter.STATE_ON -> updateState(true)
            else -> updateState(false)
        }
    }
}

