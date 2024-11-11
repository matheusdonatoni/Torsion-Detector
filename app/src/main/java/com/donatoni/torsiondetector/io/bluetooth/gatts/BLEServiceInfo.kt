package com.donatoni.torsiondetector.io.bluetooth.gatts

import java.util.UUID

interface BLEServiceInfo {
    val uuid: UUID
    val name: String
}

data class ScaleServiceInfo(
    override val uuid: UUID = UUID.fromString(
        "12c3916f-6768-40b8-a394-0ce8654c34be"
    ),
    override val name: String = "Scale service"
) : BLEServiceInfo