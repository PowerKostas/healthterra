package com.healthterra.ui.components.general

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.DialogProperties

// Uses AnnotatedString instead of String because one instance needs it, don't want to do an overloaded method
@Composable
fun InfoDialog(icon: ImageVector?, iconColour: Color, title: String?, text: AnnotatedString, confirmText: String, dismissText: String?, isCancelable: Boolean, fontWeight: FontWeight, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,

        // Block back presses and outside clicks
        properties = DialogProperties(
            dismissOnBackPress = isCancelable,
            dismissOnClickOutside = isCancelable
        ),

        // Optional icon
        icon = icon?.let { safeIcon ->
            {
                Icon(
                    imageVector = safeIcon,
                    contentDescription = "Dialog Icon",
                    tint = iconColour
                )
            }
        },

        title = title?.let { safeTitle ->
            {
                Text(
                    text = safeTitle,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },

        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = fontWeight,
                textAlign = TextAlign.Center
            )
        },

        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmText,
                    fontWeight = FontWeight.Bold
                )
            }
        },

        dismissButton = dismissText?.let { safeDismissText ->
            {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = safeDismissText,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    )
}
