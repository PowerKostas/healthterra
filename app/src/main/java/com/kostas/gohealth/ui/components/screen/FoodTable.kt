package com.kostas.gohealth.ui.components.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kostas.gohealth.data.entities.Food

@Composable
fun FoodTable(rows: List<Food>, onFoodSelected: (Int) -> Unit) {
    if (rows.isEmpty()) {
        Text(
            text = "No results found",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        return
    }

    val columnWeights = listOf(0.7f, 0.2f, 0.1f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(12.dp))
    ) {
        // Header row
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp), // Minimum spacing between columns
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Text("Item", modifier = Modifier.weight(columnWeights[0]), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text("Size", modifier = Modifier.weight(columnWeights[1]), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text("kcal", modifier = Modifier.weight(columnWeights[2]), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface)

        // Data rows
        rows.forEachIndexed { index, food ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .background(
                        if (index % 2 == 0) {
                            MaterialTheme.colorScheme.surface
                        }

                        else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                    )

                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    .clickable { onFoodSelected(food.calories) }
            ) {
                // Scrollable food column
                Box(modifier = Modifier.weight(columnWeights[0])) {
                    Text(food.item, modifier = Modifier.horizontalScroll(rememberScrollState()), style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false)
                }

                Text(food.size, modifier = Modifier.weight(columnWeights[1]), style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${food.calories}", modifier = Modifier.weight(columnWeights[2]), style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.End)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }

    Text(
        text = "${rows.size} result${if (rows.size == 1) "" else "s"} found",
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = (-2).dp),

        textAlign = TextAlign.End
    )
}
