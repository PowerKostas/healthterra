package com.healthterra.ui.components.general

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun NumberTextField(text: String, minValue: Float, maxValue: Float, selectedValue: String, onFocusLost: () -> Unit, onValueChange: (String) -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    val currentValue = selectedValue.toFloatOrNull()
    val isError = currentValue != null && currentValue < minValue && !isFocused

    OutlinedTextField(
        value = selectedValue,
        label = { Text(text = text) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),

        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                if (isFocused && !focusState.isFocused) {
                    onFocusLost()
                }

                isFocused = focusState.isFocused
            },

        isError = isError,
        supportingText = {
            if (isError) {
                CustomSupportingText(Modifier.padding(bottom = 5.dp), "Please enter a larger number")
            }
        },

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
