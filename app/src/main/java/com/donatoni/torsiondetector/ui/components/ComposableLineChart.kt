package com.donatoni.torsiondetector.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

private const val TAG = "ComposableLineChart"

@Composable
fun ComposableLineChart(
    modifier: Modifier = Modifier,
    lineDataSet: LineDataSet,
) {
    val labelsColor = MaterialTheme.colorScheme.onBackground
    val lineColor = MaterialTheme.colorScheme.primary

    AndroidView(
        modifier = modifier,
        factory = { context ->
            LineChart(context).apply {
                data = LineData(
                    lineDataSet.formatWithoutDotAndValues(
                        lineColor = lineColor,
                        textColor = labelsColor,
                    )
                )

                axisLeft.textColor = labelsColor.toArgb()
                axisRight.isEnabled = false

                xAxis.setDrawLabels(true)
                xAxis.textColor = labelsColor.toArgb()
                xAxis.position = XAxis.XAxisPosition.BOTTOM

                legend.textColor = labelsColor.toArgb()

                description.textColor = labelsColor.toArgb()
                description.isEnabled = false
            }
        },
        update = {
            it.data = LineData(
                lineDataSet.formatWithoutDotAndValues(
                    lineColor = lineColor,
                    textColor = labelsColor,
                )
            )

            it.notifyDataSetChanged()
            it.invalidate()
        }
    )
}

private fun LineDataSet.formatWithoutDotAndValues(lineColor: Color, textColor: Color): LineDataSet {
    return apply {
        valueTextColor = textColor.toArgb()

        color = lineColor.toArgb()

        setDrawCircles(false)
        setDrawValues(false)
    }
}