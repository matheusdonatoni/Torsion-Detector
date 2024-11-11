package com.donatoni.torsiondetector.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.donatoni.torsiondetector.ui.components.*
import com.donatoni.torsiondetector.ui.navigation.BLEDestination
import com.donatoni.torsiondetector.ui.navigation.NavGraph
import com.donatoni.torsiondetector.ui.navigation.navigateSingleTop
import com.donatoni.torsiondetector.ui.theme.TorsionDetectorTheme
import com.donatoni.torsiondetector.ui.wrappers.bluetooth.BluetoothWrapper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BLECommunicationApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    TorsionDetectorTheme {
        BluetoothWrapper {
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = BLEDestination.fromRoute(
                backStackEntry?.destination?.route
            )

            BackHandler(enabled = drawerState.isOpen) {
                scope.launch {
                    drawerState.close()
                }
            }

            AppDrawer(
                drawerState = drawerState,
                currentDestination = currentDestination,
                onDestinationClick = {
                    navController.navigateSingleTop(it.route)
                    scope.launch { drawerState.close() }
                }
            ) {
                Surface(modifier=modifier) {
                    NavGraph(
                        navController = navController,
                        onDrawerIconClick = { scope.launch { drawerState.open() } }
                    )
                }
            }
        }
    }
}

