package com.donatoni.torsiondetector.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.donatoni.torsiondetector.R

interface BLEDestination {
    val route: String

    @get: StringRes
    val title: Int

    companion object {
        fun fromRoute(route: String?): BLEDestination {
            return when (route) {
                Home.route -> Home
                DiscoverDevices.route -> DiscoverDevices
                DeviceDetails.route -> DeviceDetails
                ServicesAdministration.route -> ServicesAdministration
                else -> Home
            }
        }
    }
}

interface BLEDrawerDestination : BLEDestination {
    @get: DrawableRes
    val icon: Int

    companion object {
        val destinations = listOf(
            Home, DiscoverDevices, ServicesAdministration,
        )
    }
}

object Home : BLEDrawerDestination {
    override val icon = R.drawable.ic_baseline_home_24
    override val route = "ble_communicator_route"
    override val title = R.string.app_name
}

object DiscoverDevices : BLEDrawerDestination {
    override val icon = R.drawable.ic_baseline_discover_devices_24
    override val route = "discover_devices_route"
    override val title = R.string.discover_devices
}



object DeviceDetails : BLEDestination {
    override val route = "device_details_route"
    const val deviceAddressArg = "addressArg"
    val routeWithArgs = "${route}/{$deviceAddressArg}"
    val arguments = listOf(
        navArgument(deviceAddressArg) { type = NavType.StringType }
    )
    override val title = R.string.device_details
}

object ServicesAdministration : BLEDrawerDestination {
    override val icon = R.drawable.ic_baseline_services_administration_24
    override val route = "services_administration_route"
    override val title = R.string.services_administration
}