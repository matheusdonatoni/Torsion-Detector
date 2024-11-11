package com.donatoni.torsiondetector.utils.exts

import android.bluetooth.BluetoothGattCharacteristic

fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean =
    properties and property != 0

fun BluetoothGattCharacteristic.isNotifiable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)

fun BluetoothGattCharacteristic.isIndictable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_INDICATE)

