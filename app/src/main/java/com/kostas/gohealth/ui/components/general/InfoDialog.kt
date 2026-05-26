package com.kostas.gohealth.ui.components.general

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

@Composable
fun InfoDialog(icon: ImageVector, iconColour: Color, title: String?, text: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,

        icon = {
            Icon(
                icon,
                contentDescription = "Dialog Icon",
                tint = iconColour
            )
        },

        // Optional title
        title = title?.let { safeTitle ->
            {
                Text(
                    text = safeTitle,
                    textAlign = TextAlign.Center
                )
            }
        },

        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },

        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Got it",
                    color = Color(0xFF6750A4),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}
