package com.healthterra.ui.components.screen

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.healthterra.ui.components.general.CustomSupportingText

@Composable
fun CustomButtonDialog(
    metric: String,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add $metric") },
        containerColor = MaterialTheme.colorScheme.surface,
        text = {
            OutlinedTextField(
                value = inputText,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                onValueChange = {
                    inputText = it
                    isError = false
                },

                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true,

                isError = isError,
                supportingText = {
                    if (isError) {
                        CustomSupportingText(text = "Please enter a valid number")
                    }
                },
            )
        },

        confirmButton = {
            TextButton(
                onClick = {
                    val checkedInputText = inputText.toIntOrNull()

                    if (checkedInputText != null && checkedInputText > 0 && checkedInputText < 10000) {
                        onConfirm(checkedInputText)
                        onDismiss()
                        inputText = ""
                    }

                    else {
                        isError = true
                    }
                }
            ) {
                Text(
                    text = "Confirm",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Dismiss",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}
