package com.donatoni.torsiondetector.utils.exts

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout

suspend fun <T> Mutex.lockWithTimeout(
    timeout: Long = 100L,
    block: suspend CoroutineScope.() -> T,
): T {
    return try {
        withLock {
            withTimeout(timeout) {
                block()
            }
        }
    } catch (exception: Exception) {
        throw exception
    }
}