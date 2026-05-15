package com.kostas.gohealth.ui.components.screen

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kostas.gohealth.ui.components.general.ProgressBar

@Composable
fun ProgressBox(iconId: Int, category: String, progressBarColour: Color, progress: Int, goal: Int, onClick: () -> Unit) {
    val progressPercentage = (progress.toFloat() / goal).coerceAtMost(1.0f)

    // To disable button ripple effect
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .border(
                2.dp,
                MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(12.dp)
            )

            .padding(12.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onClick() }
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = category,
                tint = Color.Unspecified,
                modifier = Modifier.size(80.dp)
            )

            Text(
                text = category,
                style = MaterialTheme.typography.labelLarge
            )

            ProgressBar(12.dp, progressBarColour, progressPercentage)

            // Special message if the user passes the calories range
            if (category == "Calories" && progress > goal + 200) {
                Text(
                    text = "Calories Exceeded!",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }

            else {
                Text(
                    text = "${"%.1f".format(progressPercentage * 100)}%", // Percentage, out of 100, rounded to 1 decimal place
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
