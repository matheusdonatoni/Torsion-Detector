package com.donatoni.torsiondetector.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.donatoni.torsiondetector.io.bluetooth.BluetoothStateBroadcast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var bluetoothStateBroadcast: BluetoothStateBroadcast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BLECommunicationApp()
        }
    }

    override fun onStart() {
        super.onStart()

        bluetoothStateBroadcast.register(this)
    }

    override fun onStop() {
        bluetoothStateBroadcast.unregister(this)

        super.onStop()
    }
}

