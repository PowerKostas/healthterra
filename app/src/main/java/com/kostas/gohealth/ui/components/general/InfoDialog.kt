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
fun InfoDialog(icon: ImageVector, title: String?, text: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,

        icon = {
            Icon(
                icon,
                contentDescription = "Dialog Icon",
                tint = Color(0xFFD4AF37)
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
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        },

        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Got it",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFD4AF37),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}
