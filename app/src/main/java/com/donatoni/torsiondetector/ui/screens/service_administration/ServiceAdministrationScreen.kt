package com.donatoni.torsiondetector.ui.screens.service_administration

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.donatoni.torsiondetector.R
import com.donatoni.torsiondetector.ui.navigation.ServicesAdministration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceAdministrationScreen(
    modifier: Modifier = Modifier,
    onDrawerIconClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(ServicesAdministration.title)) },
                navigationIcon = {
                    IconButton(onClick = onDrawerIconClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(R.string.drawer_button)
                        )
                    }
                }
            )
        }
    ) {
        Text(text = "Service Administration!", modifier.padding(it))
    }
}