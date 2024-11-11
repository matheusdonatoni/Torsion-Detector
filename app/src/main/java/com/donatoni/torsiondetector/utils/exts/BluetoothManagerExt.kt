package com.donatoni.torsiondetector.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile

@SuppressLint("MissingPermission")
fun BluetoothManager.getAllConnectedDevices(): Set<BluetoothDevice> {
    val devices: MutableList<BluetoothDevice> = mutableListOf()

    devices.apply {
        addAll(getConnectedDevices(BluetoothProfile.GATT))
        addAll(getConnectedDevices(BluetoothProfile.GATT_SERVER))

    }

    return devices.toSet()
}