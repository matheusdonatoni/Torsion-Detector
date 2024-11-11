package com.donatoni.torsiondetector.utils.exts

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

val Context.packageUri: Uri
    get() = Uri.parse("package:$packageName")

fun Context.navigateToAppSettings() {
    startActivity(
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            packageUri
        )
    )
}

@SuppressLint("MissingPermission")
fun Context.askForTurningBluetoothOn() {
    startActivity(
        Intent(
            BluetoothAdapter.ACTION_REQUEST_ENABLE,
        )
    )
}