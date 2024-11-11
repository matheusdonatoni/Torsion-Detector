package com.donatoni.torsiondetector.utils

import android.content.pm.PackageManager
import androidx.activity.ComponentActivity

fun ComponentActivity.hasPermission(permission: String): Boolean {
    return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
}