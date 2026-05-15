package com.kostas.gohealth.ui.components.general

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp

@Composable
fun BulletGraph(currentValue: Int, minValue: Int, maxValue: Int, colour: Color) {
    val errorColour = MaterialTheme.colorScheme.error

    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(20.dp)
    ) {
        val width = size.width
        val height = size.height

        // The bar needs X coordinates, they are calculated here based on ratios
        val maxDisplayValue = maxValue + 500
        val currentRatio = (currentValue.toFloat() / maxDisplayValue.toFloat()).coerceIn(0f, 1f)
        val minRatio = (minValue.toFloat() / maxDisplayValue.toFloat()).coerceIn(0f, 1f)
        val maxRatio = (maxValue.toFloat() / maxDisplayValue.toFloat()).coerceIn(0f, 1f)

        val currentX = currentRatio * width
        val minX = minRatio * width
        val maxX = maxRatio * width

        // The background track
        drawRoundRect(
            color = colour.copy(alpha = 0.2f),
            cornerRadius = CornerRadius(height / 2, height / 2)
        )

        // The target range
        drawRect(
            color = Color(0xFF4CAF50),
            topLeft = Offset(x = minX, y = 0f),
            size = Size(maxX - minX, height)
        )

        // The current progress
        val cornerRadius = CornerRadius(height / 2, height / 2)
        val progressPath = Path().apply {
            addRoundRect(
                RoundRect(
                    left = 0f,
                    top = 0f,
                    right = currentX,
                    bottom = height,
                    topLeftCornerRadius = cornerRadius,
                    bottomLeftCornerRadius = cornerRadius,
                    topRightCornerRadius = CornerRadius.Zero,
                    bottomRightCornerRadius = CornerRadius.Zero
                )
            )
        }

        drawPath(
            path = progressPath,
            color = colour
        )

        val markerColour = if (currentValue in minValue..maxValue) Color(0xFF4CAF50) else errorColour

        // The progress marker
        drawLine(
            color = markerColour,
            strokeWidth = 3.dp.toPx(),
            start = Offset(x = currentX, y = -24f), // Extend slightly above the bar
            end = Offset(x = currentX, y = height + 24f) // Extend slightly below the bar
        )

        // The arrowhead on top of the line
        val arrowPath = Path().apply {
            moveTo(currentX - 12f, -32f) // Top Left
            lineTo(currentX + 12f, -32f) // Top Right
            lineTo(currentX, 0f) // Bottom Point
            close()
        }

        drawPath(path = arrowPath, color = markerColour)
    }
}
