package com.donatoni.torsiondetector.ui.wrappers.bluetooth

import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.donatoni.torsiondetector.R
import com.donatoni.torsiondetector.permissions.Bluetooth
import com.donatoni.torsiondetector.ui.components.AppSkeleton
import com.donatoni.torsiondetector.utils.*
import com.donatoni.torsiondetector.utils.exts.askForTurningBluetoothOn
import com.donatoni.torsiondetector.utils.exts.navigateToAppSettings
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BluetoothWrapper(
    viewModel: BluetoothWrapperViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    val permissionsState = rememberMultiplePermissionsState(
        permissions = Bluetooth.permissions
    )

    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                if (!permissionsState.shouldShowRationale) {
                    permissionsState.launchMultiplePermissionRequest()
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (permissionsState.allPermissionsGranted) {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        if (uiState == BluetoothWrapperUiState.On) {
            content()
        } else {
            AskForTurningBluetoothOnDialog(
                onConfirmClick = context::askForTurningBluetoothOn
            )

            AppSkeleton()
        }
    } else {
        AppSkeleton()

        val revokedPermissions = permissionsState.revokedPermissions

        if (permissionsState.shouldShowRationale) {
            RationalePermissionDialog(
                permissions = revokedPermissions.map { it.permission },
                onConfirmClick = permissionsState::launchMultiplePermissionRequest,
            )
        } else {
            DeniedPermissionDialog(
                permissions = revokedPermissions.map { it.permission },
                onConfirmClick = context::navigateToAppSettings
            )
        }
    }
}

@Composable
fun RationalePermissionDialog(
    permissions: List<String>,
    onConfirmClick: () -> Unit,
) {
    RevokedPermissionDialog(
        informationText = stringResource(R.string.denied_permissions_text),
        permissions = permissions,
        title = stringResource(id = R.string.rationale_permissions),
        confirmButton = {
            TextButton(onClick = onConfirmClick) {
                Text(text = stringResource(id = R.string.ask_again))
            }
        },
    )
}

@Composable
fun DeniedPermissionDialog(
    permissions: List<String>,
    onConfirmClick: () -> Unit,
) {
    RevokedPermissionDialog(
        informationText = stringResource(R.string.permanently_denied_permissions_text),
        permissions = permissions,
        title = stringResource(id = R.string.denied_permissions),
        confirmButton = {
            TextButton(onClick = onConfirmClick) {
                Text(text = stringResource(id = R.string.open_settings))
            }
        },
    )
}

@Composable
fun RevokedPermissionDialog(
    title: String,
    informationText: String,
    permissions: List<String>,
    confirmButton: @Composable () -> Unit,
) {
    AlertDialog(
        modifier = Modifier.padding(12.dp),
        onDismissRequest = {},
        title = { Text(text = title) },
        text = {
            LazyColumn {
                item { Text(text = informationText, modifier = Modifier.padding(bottom = 8.dp)) }
                items(permissions) {
                    Text(text = "• ${Bluetooth.toFormattedString(it)}")
                }
            }
        },
        icon = {
            Icon(
                painter = painterResource(R.drawable.ic_outline_app_blocking_24),
                contentDescription = null,
            )
        },
        confirmButton = confirmButton,
    )
}

@Composable
fun AskForTurningBluetoothOnDialog(
    onConfirmClick: () -> Unit,
) {
    AlertDialog(
        modifier = Modifier.padding(12.dp),
        onDismissRequest = { },
        text = { Text(stringResource(id = R.string.enable_bluetooth_message)) },
        icon = {
            Icon(
                painter = painterResource(R.drawable.ic_baseline_bluetooth_disabled_24),
                contentDescription = null,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirmClick) {
                Text(stringResource(id = R.string.enable))
            }
        }
    )
}