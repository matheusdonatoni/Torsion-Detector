package com.donatoni.torsiondetector.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.donatoni.torsiondetector.ui.screens.device_details.DeviceDetailsScreen
import com.donatoni.torsiondetector.ui.screens.discover_devices.DiscoverDevicesScreen
import com.donatoni.torsiondetector.ui.screens.home.HomeScreen
import com.donatoni.torsiondetector.ui.screens.service_administration.ServiceAdministrationScreen

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    onDrawerIconClick: () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = Home.route,
        modifier = modifier
    ) {
        composable(route = Home.route) {
            HomeScreen(
                onDrawerIconClick = onDrawerIconClick,
                onConnectedDeviceClick = {
                    navController.navigateToDeviceDetails(it)
                },
            )
        }
        composable(route = DiscoverDevices.route) {
            DiscoverDevicesScreen(onDrawerIconClick = onDrawerIconClick)
        }
        composable(route = ServicesAdministration.route) {
            ServiceAdministrationScreen(onDrawerIconClick = onDrawerIconClick)
        }
        composable(
            route = DeviceDetails.routeWithArgs,
            arguments = DeviceDetails.arguments
        ) {
            val deviceAddress = it.arguments?.getString(DeviceDetails.deviceAddressArg)

            DeviceDetailsScreen(
                onBackButtonClick = navController::popBackStack,
                onAlertDialogConfirmClick = navController::popBackStack
            )
        }
    }
}

fun NavHostController.navigateSingleTop(route: String) = this.navigate(route) {
    popUpTo(this@navigateSingleTop.graph.findStartDestination().id) {
        saveState = true
    }

    launchSingleTop = true
    restoreState = true
}

fun NavHostController.navigateToDeviceDetails(deviceId: String) {
    navigateSingleTop("${DeviceDetails.route}/$deviceId")
}