package com.donatoni.torsiondetector.di

import android.bluetooth.BluetoothManager
import android.content.Context
import com.donatoni.torsiondetector.io.bluetooth.BluetoothStateBroadcast
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class BLEModule {
    @Provides
    @Singleton
    fun provideBluetoothManager(@ApplicationContext context: Context): BluetoothManager {
        return context.getSystemService(BluetoothManager::class.java)
    }

    @Provides
    @Singleton
    fun provideBluetoothStateBroadcast(@ApplicationContext context: Context): BluetoothStateBroadcast {
        val manager = provideBluetoothManager(context)

        return BluetoothStateBroadcast(
            initialState = manager.adapter.isEnabled,
        )
    }
}