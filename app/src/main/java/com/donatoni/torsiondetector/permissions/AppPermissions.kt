package com.donatoni.torsiondetector.permissions

import android.Manifest


interface AppPermission {
    val legacyPermissions: List<String>
    val modernPermissions: List<String>
    val permissions: List<String>

    fun toFormattedString(manifestString: String): String
}


object Bluetooth : AppPermission {
    override val permissions: List<String>
        get() {
            val isBluetoothLegacy = android.os.Build.VERSION.SDK_INT <= 30

            if (isBluetoothLegacy) {
                return legacyPermissions
            }

            return modernPermissions
        }

    override val legacyPermissions = listOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN
    )

    override val modernPermissions = listOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    )

    override fun toFormattedString(manifestString: String): String {
        return when (manifestString) {
            Manifest.permission.BLUETOOTH -> "Bluetooth"
            Manifest.permission.BLUETOOTH_ADMIN -> "Bluetooth admin"
            Manifest.permission.BLUETOOTH_SCAN -> "Bluetooth scan"
            Manifest.permission.BLUETOOTH_CONNECT -> "Bluetooth connect"
            else -> "Unknown"
        }
    }
}