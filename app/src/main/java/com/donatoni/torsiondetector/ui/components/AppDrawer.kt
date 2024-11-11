package com.donatoni.torsiondetector.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.donatoni.torsiondetector.ui.navigation.BLEDestination
import com.donatoni.torsiondetector.ui.navigation.BLEDrawerDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawer(
    modifier: Modifier = Modifier,
    drawerState: DrawerState,
    currentDestination: BLEDestination,
    onDestinationClick: (BLEDestination) -> Unit,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            val configuration = LocalConfiguration.current
            val screenWidth = configuration.screenWidthDp.dp
            val drawerWidth = if (screenWidth <= 360.dp) screenWidth - 56.dp else 360.dp

            ModalDrawerSheet(
                modifier = Modifier.width(drawerWidth)
            ) {
                Spacer(Modifier.height(12.dp))
                BLEDrawerDestination.destinations.forEach {
                    NavigationDrawerItem(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        icon = {
                            Icon(
                                painter = painterResource(id = it.icon),
                                contentDescription = null
                            )
                        },
                        label = { Text(stringResource(it.title)) },
                        selected = it == currentDestination,
                        onClick = { onDestinationClick(it) }
                    )
                }
            }
        },
        content = content
    )
}

