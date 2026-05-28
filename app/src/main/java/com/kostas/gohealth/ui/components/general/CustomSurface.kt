package com.kostas.gohealth.ui.components.general

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CustomSurface(startPadding: Dp, topPadding: Dp, endPadding: Dp, bottomPadding: Dp, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .padding(start = startPadding, top = topPadding, end = endPadding, bottom = bottomPadding)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(16.dp)
            )

            // Put the custom modifier last, so if the surface is clickable, it will only be clickable in the space created by the
            // other modifiers
            .then(modifier)
    ) {
        content()
    }
}
