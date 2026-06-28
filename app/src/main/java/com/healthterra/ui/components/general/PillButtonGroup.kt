package com.healthterra.ui.components.general

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PillButtonGroup(options: List<String>, selectedRange: String, onOptionSelected: (String) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            //.fillMaxWidth()
            .clip(RoundedCornerShape(48))
            .background(Color.LightGray.copy(alpha = 0.5f))
    ) {
        options.forEach { option ->
            val isSelected = selectedRange == option

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    //.weight(1f)
                    //.fillMaxHeight()
                    .clip(RoundedCornerShape(48))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)

                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onOptionSelected(option) }

                    .padding(10.dp)
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                )
            }
        }
    }
}
