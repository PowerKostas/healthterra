package com.healthterra.ui.components.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.healthterra.helpers.drawCardShape
import com.healthterra.helpers.drawShieldShape
import com.healthterra.helpers.drawStarburstShape

@Composable
fun AchievementItem(icon: ImageVector, title: String, description: String, tier: String, isUnlocked: Boolean) {
    var showDialog by remember { mutableStateOf(false) }

    val shape = when (tier) {
        "Common" -> CircleShape
        "Rare" -> RoundedCornerShape(16.dp)
        "Epic" -> drawCardShape()
        "Legendary" -> drawShieldShape()
        "Impossible" -> drawStarburstShape()
        else -> CircleShape
    }

    val brush = when (tier) {
        "Common" -> SolidColor(Color(0xFF2883fe))
        "Rare" -> SolidColor(Color(0xFFd97f40))
        "Epic" -> SolidColor(Color(0xFFbe51f1))
        "Legendary" -> Brush.linearGradient(colors = listOf(Color(0xFF89ef82), Color(0xFFedc8a0), Color(0xFFdb449e)))
        else -> SolidColor(Color(0xFFf5a302))
    }

    // Makes all locked achievements gray, used in Modifier.graphicsLayer
    val colorMatrix = remember {
        ColorMatrix().apply {
            if (!isUnlocked) setToSaturation(0f)
        }
    }

    Box(modifier = Modifier.size(68.dp)) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()

                .graphicsLayer {
                    colorFilter = ColorFilter.colorMatrix(colorMatrix)
                    alpha = if (isUnlocked) 1f else 0.4f
                }

                .clip(shape)
                .background(brush)
                .clickable { showDialog = true }
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Achievement Icon",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }

    if (showDialog) {
        AchievementDialog(icon, title, description) { showDialog = false }
    }
}
