package com.kostas.gohealth.ui.components.general

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
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
fun InfoDialog(icon: ImageVector, iconColour: Color, title: String?, text: String, confirmText: String, showDismissButton: Boolean, onConfirm: () -> Unit, onDismiss: () -> Unit) {
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
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF6750A4))
            ) {
                Text(
                    text = confirmText,
                    fontWeight = FontWeight.Bold
                )
            }
        },

        dismissButton = if (showDismissButton) {
            {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF6750A4)
                    )
                ) {
                    Text(
                        text = "Cancel",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        else {
            null
        }
    )
}
