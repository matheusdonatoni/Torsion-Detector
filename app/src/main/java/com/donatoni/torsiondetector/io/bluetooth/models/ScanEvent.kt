package com.donatoni.torsiondetector.io.bluetooth.models

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

sealed interface ScanEvent {
    data class OnScanResult(
        val callbackType: Int,
        val result: ScanResult,
    ) : ScanEvent

    data class OnBatchScanResults(
        val results: MutableList<ScanResult>,
    ) : ScanEvent

    data class OnScanFailed(val errorCode: Int) : ScanEvent
}

class ScanCallbackFlow(private val scope: CoroutineScope) : ScanCallback() {
    private val _emitter = MutableSharedFlow<ScanEvent>()
    val emitter: SharedFlow<ScanEvent> = _emitter

    override fun onScanResult(callbackType: Int, result: ScanResult) {
        super.onScanResult(callbackType, result)

        scope.launch {
            _emitter.emit(
                ScanEvent.OnScanResult(callbackType, result)
            )
        }
    }

    override fun onBatchScanResults(results: MutableList<ScanResult>) {
        super.onBatchScanResults(results)

        scope.launch {
            _emitter.emit(
                ScanEvent.OnBatchScanResults(results)
            )
        }
    }

    override fun onScanFailed(errorCode: Int) {
        super.onScanFailed(errorCode)

        scope.launch {
            _emitter.emit(
                ScanEvent.OnScanFailed(errorCode)
            )
        }
    }
}