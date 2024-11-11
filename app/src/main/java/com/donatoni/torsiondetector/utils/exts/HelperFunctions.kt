package com.donatoni.torsiondetector.utils.exts

import com.donatoni.torsiondetector.models.TorsionData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun generateFileNameByDateTime(): String {
    val sdf = SimpleDateFormat("ddmmyyyy_hhmmss", Locale.US)
    return sdf.format(Date()) + ".csv"
}

fun measurementsToCSV(measurements: List<TorsionData>): String {
    var csv = ""

    for (measurement in measurements) {
        csv += "${measurement.milliSeconds},${measurement.milliVolts},${measurement.value}" + "\n"
    }

    return csv
}