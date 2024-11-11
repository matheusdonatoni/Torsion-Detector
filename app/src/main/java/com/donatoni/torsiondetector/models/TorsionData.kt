package com.donatoni.torsiondetector.models

import com.donatoni.torsiondetector.io.bluetooth.gatts.ScaleCharacteristicStruct

data class TorsionData(
    val initialCondition: ScaleCharacteristicStruct,
    val currentCondition: ScaleCharacteristicStruct,
) {
    val milliVolts: Float
        get() = currentCondition.milliVolts

    val milliSeconds: UInt
        get() = currentCondition.milliSeconds

    val value: Double
        get() {
            return (milliVolts - initialCondition.milliVolts) * 1e-3 * 4F  / (U * K)
        }


    val K = 2F
    val U = 3.23

//    val L = 90F
//    val di = 3.1e-2
//    val de = 3.2e-2
//    val r = 0.32
//
//    val E = 2e12
//    val G = 8e10
}