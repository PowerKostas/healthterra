package com.healthterra.ui.components.general

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RadioButtonGroup(options: List<String>, selectedOption: String, showBorder: Boolean = true, onOptionSelected: (String) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (showBorder) {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(16.dp)
                    )
                }

                else {
                    Modifier
                }
            )
    ) {
        options.forEach { option ->
            val interactionSource = remember { MutableInteractionSource() }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(8.dp)

                    // It makes the whole column selectable so the user doesn't have to precisely tap the tiny circle
                    .selectable(
                        selected = (option == selectedOption),
                        onClick = {
                            onOptionSelected(option)
                        },

                        // Disables the tapping effect
                        interactionSource = interactionSource,
                        indication = null
                    )

            ) {
                RadioButton(
                    selected = (option == selectedOption),
                    onClick = null // Set to null because the column handles the click
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(text = option)
            }
        }
    }
}
