package com.donatoni.torsiondetector.utils.exts

import android.util.Log

fun ByteArray.toHexString(): String = this.joinToString(
    ", ", prefix = "[", postfix = "]"
) { "0x%02x".format(it) }

fun ByteArray.toInt(): Int {
    var value = 0
    for (i in this.indices) {
        val isLSB = i == this.size - 1

        val byte = if (isLSB) {
            this[i].toInt() shl 8 * i
        } else {
            0xff and this[i].toInt() shl 8 * i
        }

        value = value or byte
    }
    return value
}

fun ByteArray.toUInt(): UInt {
    var value = 0U

    for (i in this.indices) {
        value = value or (0xFFU and this[i].toUInt() shl (8 * i))
    }

    return value
}

fun ByteArray.toBoolean(): Boolean = this.first().toUInt() == 1U