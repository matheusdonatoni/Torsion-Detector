package com.donatoni.torsiondetector.utils.exts

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun String.isJsonParseable(): Boolean {
    return try {
        JSONObject(this)

        true
    } catch (exception: Exception) {
        false
    }
}

