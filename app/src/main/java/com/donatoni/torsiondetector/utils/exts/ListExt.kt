package com.donatoni.torsiondetector.utils.exts

fun <E> List<E>.joinToStringWithoutSeparation() : String {
    return joinToString("")
}