package com.donatoni.torsiondetector.ui.screens.device_details

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.donatoni.torsiondetector.R
import com.donatoni.torsiondetector.models.TorsionData
import com.donatoni.torsiondetector.ui.components.ComposableLineChart
import com.donatoni.torsiondetector.ui.navigation.DeviceDetails
import com.donatoni.torsiondetector.utils.exts.generateFileNameByDateTime
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "DeviceDetailsScreen"

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun DeviceDetailsScreen(
    modifier: Modifier = Modifier,
    viewModel: DeviceDetailsViewModel = hiltViewModel(),
    onBackButtonClick: () -> Unit,
    onAlertDialogConfirmClick: () -> Unit,
) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(text = stringResource(DeviceDetails.title)) },
            navigationIcon = {
                IconButton(onClick = onBackButtonClick) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.back_button),
                    )
                }
            },
        )
    }) {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        Log.i(TAG, "Has error: ${uiState.hasError}")

        if (uiState.hasError) {
            Text(
                modifier = modifier.padding(it), text = uiState.errorMessage!!
            )
        } else {
            if (uiState is DeviceDetailsUiState.NoDevice) {
                AlertDialog(modifier = modifier
                    .padding(it)
                    .padding(12.dp),
                    onDismissRequest = {},
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = stringResource(id = R.string.warning)
                        )
                    },
                    title = { Text(text = stringResource(id = R.string.ops_something_went_wrong)) },
                    text = {
                        Text(text = stringResource(id = R.string.device_not_found))
                    },
                    confirmButton = {
                        TextButton(onClick = onAlertDialogConfirmClick) {
                            Text(text = stringResource(id = R.string.okay))
                        }
                    })
            } else {
                val dataFlow = (uiState as DeviceDetailsUiState.HasDevice).dataFlow
                val autoMode = (uiState as DeviceDetailsUiState.HasDevice).autoMode

                if (dataFlow != null) {
                    val data by dataFlow.collectAsStateWithLifecycle()
                    var autoScroll by rememberSaveable {
                        mutableStateOf(true)
                    }

                    Column(modifier = Modifier.padding(it)) {
                        ComposableLineChart(
                            Modifier
                                .weight(.5f)
                                .fillMaxWidth(), lineDataSet = LineDataSet(
                                data.map { scaleStruct ->
                                    Entry(
                                        scaleStruct.milliSeconds.toFloat(),
                                        scaleStruct.value.toFloat()
                                    )
                                }, "m/m"
                            )
                        )

                        Table(modifier = Modifier.weight(.5f), data, autoScroll)

                        Row(
                            Modifier.padding(all = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = autoScroll, onCheckedChange = { checked ->
                                autoScroll = checked
                            })
                            Text(text = "Auto scroll")
                        }

                        Row(Modifier.padding(all = 12.dp)) {
                            TextButton(
                                onClick = viewModel::requestScaleSingleRead, enabled = !autoMode
                            ) {
                                Text("Single read")
                            }

                            TextButton(viewModel::requestScaleAutoRead) {
                                Text(if (autoMode) "Disable auto read" else "Enable auto read")
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            TextButton(onClick = viewModel::requestRestart) {
                                Text(text = "Restart")
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            val context = LocalContext.current

                            val launcher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.CreateDocument(
                                    stringResource(id = R.string.any_mime_type),
                                )
                            ) { uri ->
                                uri?.let {
                                    viewModel.onSaveLocallyClick(
                                        context,
                                        uri,
                                        data
                                    )
                                }
                            }

                            TextButton(onClick = {
                                launcher.launch(generateFileNameByDateTime())
                            }) {
                                Text(
                                    text = stringResource(id = R.string.save_locally)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    color: Color? = null,
) {
    Text(
        text = text,
        color = color ?: MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.onBackground)
            .weight(weight)
            .padding(8.dp)

    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Table(
    modifier: Modifier = Modifier,
    data: List<TorsionData>,
    autoScroll: Boolean = true,
) {

    val tableState = rememberLazyListState()

    LaunchedEffect(data) {
        val lastIndex = data.size - 1

        if (lastIndex >= 0 && autoScroll) {
            tableState.animateScrollToItem(lastIndex)
        }
    }

    // Each cell of a column must have the same weight.
    val weight = 1f / 3f

    // The LazyColumn will be our table. Notice the use of the weights below
    LazyColumn(
        modifier
            .fillMaxWidth()
            .padding(16.dp), state = tableState
    ) {
        stickyHeader {
            Row(Modifier.background(MaterialTheme.colorScheme.primary)) {
                TableCell(
                    text = "Time [ms]", weight = weight, color = MaterialTheme.colorScheme.onPrimary
                )
                TableCell(
                    text = "Raw value [mV]",
                    weight = weight,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                TableCell(
                    text = "Displacement [m/m]",
                    weight = weight,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        val format = DecimalFormat("#.000E0")

        // Here are all the lines of your table.
        items(data) {
            Row(Modifier.fillMaxWidth()) {
                TableCell(text = it.milliSeconds.toString(), weight = weight)
                TableCell(text = "%.3f".format(it.milliVolts), weight = weight)
                TableCell(text = format.format(it.value), weight = weight)
            }
        }
    }
}

