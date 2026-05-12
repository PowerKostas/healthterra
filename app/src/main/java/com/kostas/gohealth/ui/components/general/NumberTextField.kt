package com.kostas.gohealth.ui.components.general

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun NumberTextField(
    text: String,
    maxValue: Float,
    selectedValue: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = selectedValue,
        label = { Text(text = text) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        onValueChange = { newValue ->
            // Allow the field to be empty so the user can delete their input
            if (newValue.isEmpty()) {
                onValueChange(newValue)
            }

            // Ensure the input only contains a maximum of one fractional number and one . and digits from 0 to maxValue
            else if (newValue.count{ it == '.' } <= 1 && newValue.all{ it.isDigit() || it == '.' }) {
                // It's valid if there is no decimal (-1) or if the characters after the decimal are <= 1
                val decimalIndex = newValue.indexOf('.')
                val isValid = decimalIndex == -1 || (newValue.length - decimalIndex - 1 <= 1)

                // Handle the case where the user starts by typing just "."
                if (isValid) {
                    if (newValue == ".") {
                        onValueChange(newValue)
                    }

                    else {
                        if (newValue.toFloat() in 0f..maxValue) {
                            onValueChange(newValue)
                        }
                    }
                }
            }
        }
    )
}
