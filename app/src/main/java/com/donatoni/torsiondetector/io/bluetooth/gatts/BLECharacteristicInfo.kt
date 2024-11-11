package com.donatoni.torsiondetector.io.bluetooth.gatts


import com.donatoni.torsiondetector.utils.exts.toBoolean
import com.donatoni.torsiondetector.utils.exts.toInt
import com.donatoni.torsiondetector.utils.exts.toUInt
import java.util.*

interface BLECharacteristicInfo {
    val uuid: UUID
    val name: String
}

interface BLECharacteristicStruct

interface BLECharacteristicInfoWithStructParser : BLECharacteristicInfo {
    fun valueFromBytes(data: ByteArray): BLECharacteristicStruct
}

interface BLECommandCharacteristicInfo : BLECharacteristicInfo

data class ScaleCharacteristicStruct(
    val milliVolts: Float,
    val milliSeconds: UInt,
) : BLECharacteristicStruct

data class ScaleCharacteristicInfo(
    override val uuid: UUID = UUID.fromString("53d33a98-988e-4a98-812a-48da102f9c5b"),
    override val name: String = "Scale characteristic",
) : BLECharacteristicInfoWithStructParser {

    override fun valueFromBytes(data: ByteArray): ScaleCharacteristicStruct {
        val milliVolts = data.sliceArray(
            IntRange(0, 1)
        ).toInt().toFloat() / 1e3f

        val timeInMillis = data.sliceArray(
            IntRange(2, 5)
        ).toUInt()

        return ScaleCharacteristicStruct(
            milliVolts,
            timeInMillis
        )
    }
}

data class ReadModeCharacteristicStruct(
    val autoMode: Boolean,
) : BLECharacteristicStruct

data class AutoModeCharacteristicInfo(
    override val uuid: UUID = UUID.fromString("96d56383-b5b7-4c1f-8265-cad11350c802"),
    override val name: String = "Read mode characteristic",
) : BLECharacteristicInfoWithStructParser {
    override fun valueFromBytes(data: ByteArray): ReadModeCharacteristicStruct {
        return ReadModeCharacteristicStruct(
            data.toBoolean()
        )
    }
}

data class ScaleCommandCharacteristicInfo(
    override val uuid: UUID = UUID.fromString("86d822f0-19ad-46f5-a158-d2bb5ad4d089"),
    override val name: String = "Scale command characteristic"
) : BLECommandCharacteristicInfo {
    companion object {
        val READ_SCALE_ONCE = byteArrayOf(0x01)
        val READ_SCALE_AUTOMATICALLY_FALSE = byteArrayOf(0x02, 0x00)
        val READ_SCALE_AUTOMATICALLY_TRUE = byteArrayOf(0x02, 0x01)
        val RESTART_MEASUREMENTS = byteArrayOf(0x03)
    }
}