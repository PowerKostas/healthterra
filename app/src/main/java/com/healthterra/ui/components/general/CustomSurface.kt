package com.healthterra.ui.components.general

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CustomSurface(modifier: Modifier = Modifier, startPadding: Dp = 16.dp, topPadding: Dp = 0.dp, endPadding: Dp = 16.dp, bottomPadding: Dp = 0.dp, borderWidth: Dp = 1.dp, borderColor: Color = MaterialTheme.colorScheme.onSurface, roundedCornerShape: RoundedCornerShape = RoundedCornerShape(16.dp), content: @Composable () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = roundedCornerShape,
        modifier = Modifier
            .padding(start = startPadding, top = topPadding, end = endPadding, bottom = bottomPadding)
            .clip(roundedCornerShape)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = roundedCornerShape
            )

            // Put the custom modifier last, so if the surface is clickable, it will only be clickable in the space created by the
            // other modifiers
            .then(modifier)
    ) {
        content()
    }
}
