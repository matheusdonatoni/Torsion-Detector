package com.donatoni.torsiondetector.ui.screens.home


import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.donatoni.torsiondetector.R
import com.donatoni.torsiondetector.ui.components.EmptyDeviceList
import com.donatoni.torsiondetector.ui.navigation.Home
import com.donatoni.torsiondetector.ui.screens.discover_devices.DeviceTile


@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onDrawerIconClick: () -> Unit,
    onConnectedDeviceClick: (deviceAddress: String) -> Unit,
) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(stringResource(Home.title)) },
            navigationIcon = {
                IconButton(onClick = onDrawerIconClick) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = stringResource(R.string.drawer_button)
                    )
                }
            },
        )
    }) {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        if (uiState is HomeUiState.HasDevices) {
            val connectedDevices = (uiState as HomeUiState.HasDevices).connectedDevices

            LazyColumn(modifier.padding(it)) {
                items(connectedDevices) { device ->
                    DeviceTile(
                        name = device.name,
                        address = device.address,
                        onClick = { onConnectedDeviceClick(device.address) },
                    )
                }

            }
        } else {
            EmptyDeviceList(
                modifier.padding(it),
                message = stringResource(id = R.string.no_connected_devices),
            )
        }
    }
}

