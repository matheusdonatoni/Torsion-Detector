package com.donatoni.torsiondetector.utils

class DataBuffer<T> {
    private val _buffer = mutableListOf<T>()

    fun clear() {
        _buffer.clear()
    }

    fun collect(value: T, predicate: (T) -> Boolean): List<T>? {
        _buffer.add(value)

        if (predicate(value)) {
            val auxiliaryBuffer = _buffer.toList()
            _buffer.clear()

            return auxiliaryBuffer
        }

        return null
    }
}