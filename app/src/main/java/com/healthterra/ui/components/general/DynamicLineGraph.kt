package com.healthterra.ui.components.general

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent

@Composable
fun DynamicLineGraph(values: List<Int>, labels: List<String>) {
    // Builds the Cartesian Data Model using the mapped values
    val chartModel = remember(values) {
        CartesianChartModel(
            LineCartesianLayerModel.build {
                series(values)
            }
        )
    }

    // Draws the CartesianChart
    CartesianChartHost(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),

        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        // Makes the line 3 dp and uses the app's primary color
                        fill = LineCartesianLayer.LineFill.single(Fill(MaterialTheme.colorScheme.primary)),
                        stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 3.dp),

                        // Makes the line curved
                        interpolator = LineCartesianLayer.Interpolator.cubic(),

                        // Adds gradient below the line
                        areaFill = LineCartesianLayer.AreaFill.single(
                            Fill(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                        Color.Transparent
                                    )
                                )
                            )
                        )
                    )
                )
            ),

            // y-axis, removes tick, adds horizontal guidelines, adjusts value labels, only allows up to 5 value labels
            startAxis = VerticalAxis.rememberStart(
                tick = null,
                line = rememberLineComponent(fill = Fill(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))),
                guideline = rememberLineComponent(
                    fill = Fill(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                    thickness = 1.dp
                ),

                label = rememberAxisLabelComponent(
                    style = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 10.sp),
                    lineCount = 2
                ),

                itemPlacer = VerticalAxis.ItemPlacer.count({ 5 }),

                valueFormatter = CartesianValueFormatter { _, value, _ ->
                    value.toInt().toString()
                }
            ),

            // x-axis, adjusts colors, removes vertical guidelines, adjusts date labels and allows them to be up to 2 lines (only used in the
            // "Month" selection)
            bottomAxis = HorizontalAxis.rememberBottom(
                tick = rememberLineComponent(fill = Fill(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))),
                line = rememberLineComponent(fill = Fill(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))),
                guideline = null,

                label = rememberAxisLabelComponent(
                    style = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 10.sp),
                    lineCount = 2
                ),

                valueFormatter = CartesianValueFormatter { _, value, _ ->
                    labels.getOrNull(value.toInt()) ?: ""
                }
            )

        ),

        model = chartModel, scrollState = rememberVicoScrollState(scrollEnabled = false)
    )
}
