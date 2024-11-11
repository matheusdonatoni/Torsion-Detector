package com.donatoni.torsiondetector.ui.components

import androidx.compose.foundation.layout.PaddingValues
import com.donatoni.torsiondetector.R
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSkeleton(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit = { Text(text = stringResource(id = R.string.app_name)) },
    content: @Composable (PaddingValues) -> Unit = { }
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = title) },
        content = content,
    )
}