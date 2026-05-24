package com.kostas.gohealth.helpers

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize

@Composable
fun createGoldMetallicBrush(animationProgress: Float, componentSize: IntSize): Brush {
    // Defines the animated metallic gold color palette (Shadows, Base, Shine, Base, Shadows)
    val metallicGoldColors = listOf(
        Color(0xFF967B27),
        Color(0xFFD4AF37),
        Color(0xFFF5E6CC),
        Color(0xFFD4AF37),
        Color(0xFF967B27)
    )

    val componentWidth = componentSize.width.toFloat().coerceAtLeast(1f)
    val componentHeight = componentSize.height.toFloat().coerceAtLeast(1f)
    val shineWidth = 200f

    // Coordinate on the x-axis for the brush, it shifts from -shineWidth to componentWidth as animationProgress increases. It's relative to
    // the component, so it starts from the left of it and exits from the right of it
    val startX = (animationProgress * (componentWidth + shineWidth)) - shineWidth

    // Creates the brush, start and end define a diagonal gradient
    return Brush.linearGradient(
        colors = metallicGoldColors,
        start = Offset(startX, 0f),
        end = Offset(startX + shineWidth, componentHeight),
    )
}
