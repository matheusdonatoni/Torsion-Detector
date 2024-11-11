package com.donatoni.torsiondetector.utils.exts

import kotlinx.coroutines.flow.*


inline fun <T> Flow<T>.bufferCount(
    crossinline clearBufferAndSkipOn: (List<T>) -> Boolean = { false },
    crossinline predicate: suspend (Int) -> Boolean
): Flow<List<T>> {
    val buffer = mutableListOf<T>()

    return transform {
        if (clearBufferAndSkipOn(buffer)) {
            buffer.clear()

        } else {
            buffer.add(it)

            if (predicate(buffer.size)) {
                val content = buffer.toList()
                buffer.clear()

                emit(content)
            }
        }
    }
}

inline fun <T> Flow<T>.bufferTest(
    crossinline clearBufferAndSkipOn: (T) -> Boolean = { false },
    crossinline predicate: suspend (T) -> Boolean
): Flow<List<T>> {
    val buffer = mutableListOf<T>()

    return transform {
        if (clearBufferAndSkipOn(it)) {
            buffer.clear()
        } else {
            buffer.add(it)

            if (predicate(it)) {
                val content = buffer.toList()
                buffer.clear()
                emit(content)
            }
        }
    }
}

inline fun <T> Flow<T>.bufferListTest(
    crossinline clearBufferAndSkipOn: (List<T>) -> Boolean = { false },
    crossinline predicate: suspend (List<T>) -> Boolean
): Flow<List<T>> {
    val buffer = mutableListOf<T>()

    return transform {
        if (clearBufferAndSkipOn(buffer)) {
            buffer.clear()
        } else {
            buffer.add(it)

            if (predicate(buffer)) {
                val content = buffer.toList()
                buffer.clear()
                emit(content)
            }
        }
    }
}


/*
Buffer data based on a flow of ints, once the buffer size matches the count value it emits the
data, if the buffer overloads (based on the int flow last emission) buffer will be cleared.

Important: This work based in the fact that count will be emitted, then content. If the order is
messed, then it won't work.
*/

fun <T> Flow<Int>.combinedBufferCount(other: Flow<T>): Flow<List<T>> {
    val buffer = mutableListOf<T>()

    return combineTransform(other) { count, data ->
        if (buffer.size > count) {
            buffer.clear()
        } else {
            buffer.add(data)

            if (count == buffer.size) {
                val content = buffer.toList()
                buffer.clear()

                emit(content)
            }
        }
    }
}