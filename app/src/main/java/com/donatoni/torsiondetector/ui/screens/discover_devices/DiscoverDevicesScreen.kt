package com.donatoni.torsiondetector.ui.screens.discover_devices

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.donatoni.torsiondetector.R
import com.donatoni.torsiondetector.ui.components.EmptyDeviceList
import com.donatoni.torsiondetector.ui.navigation.DiscoverDevices

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverDevicesScreen(
    modifier: Modifier = Modifier,
    viewModel: DiscoverDevicesViewModel = hiltViewModel(),
    onDrawerIconClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = {
        TopAppBar(title = { Text(stringResource(DiscoverDevices.title)) }, navigationIcon = {
            IconButton(onClick = onDrawerIconClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = stringResource(R.string.drawer_button)
                )
            }
        }, actions = {
            ScanStopButton(
                scanning = uiState.scanning,
                onScanClick = viewModel::startScan,
                onStopClick = viewModel::stopScan,
            )
        })
    }) {
        if (uiState is DiscoverDevicesUiState.HasDevices) {
            val scanResults = (uiState as DiscoverDevicesUiState.HasDevices).scanResults
            val connectedDevices = (uiState as DiscoverDevicesUiState.HasDevices).connectedDevices

            LazyColumn(modifier = modifier.padding(it), state = rememberLazyListState()) {
                items(scanResults) { scanResult ->
                    DeviceTile(
                        name = scanResult.device.name,
                        address = scanResult.device.address,
                        rssi = scanResult.rssi,
                        connected = connectedDevices.any { device ->
                            device.address == scanResult.device.address
                        },
                        onClick = { viewModel.onDeviceTileClick(scanResult.device) },
                    )
                }
            }
        } else {
            EmptyDeviceList(
                modifier = modifier.padding(it),
                message = stringResource(id = R.string.no_devices_found_message)
            )
        }
    }
}

@Composable
fun ScanStopButton(
    scanning: Boolean, onScanClick: () -> Unit, onStopClick: () -> Unit
) {

    TextButton(onClick = if (scanning) onStopClick else onScanClick) {
        Text(
            text = stringResource(id = if (scanning) R.string.stop else R.string.scan)
        )
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceTile(
    modifier: Modifier = Modifier,
    name: String?,
    address: String?,
    rssi: Int? = null,
    connected: Boolean = false,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(name ?: stringResource(id = R.string.unknown)) },
        supportingContent = { Text(address ?: stringResource(id = R.string.unknown)) },
        modifier = modifier.clickable(onClick = onClick),
        leadingContent = {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_devices_other_24),
                contentDescription = null
            )
        },
        trailingContent = {
            if (rssi != null) {
                Text(text = rssi.toString())
            }
        },
        colors = ListItemDefaults.colors(
            headlineColor = if (connected) MaterialTheme.colorScheme.primary else ListItemDefaults.contentColor,
            supportingColor = if (connected) MaterialTheme.colorScheme.primary else ListItemDefaults.contentColor,
            leadingIconColor = if (connected) MaterialTheme.colorScheme.primary else ListItemDefaults.contentColor,
            trailingIconColor = if (connected) MaterialTheme.colorScheme.primary else ListItemDefaults.contentColor,
        )
    )
}


