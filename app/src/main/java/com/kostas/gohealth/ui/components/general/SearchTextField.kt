package com.kostas.gohealth.ui.components.general

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun SearchTextField(modifier: Modifier, placeholder: String, buttonColor: Color, minCharacters: Int, onInputChange: () -> Unit, onSearch: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    var showError by rememberSaveable { mutableStateOf(false) }
    val onSearchAction = {
        if (query.length < minCharacters) {
            showError = true
        }

        else {
            showError = false
            onSearch(query)
            keyboardController?.hide()
        }
    }

    OutlinedTextField(
        placeholder = { Text(text = placeholder) },
        value = query,

        onValueChange = {
            query = it
            onInputChange()

            if (showError && it.length >= minCharacters) {
                showError = false
            }
        },

        singleLine = true,
        isError = showError,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),

        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            errorContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),

        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearchAction() }),

        trailingIcon = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (query.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear Button",
                        modifier = Modifier.clickable {
                            if (showError) {
                                showError = false
                            }

                            query = ""
                            onInputChange()
                        }
                    )
                }

                val interactionSource = remember { MutableInteractionSource() }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(56.dp) // Matches the minimum size of the text field
                        .clip(RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
                        .background(buttonColor)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = ripple(color = Color.White),
                            onClick = { onSearchAction() }
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Button",
                        tint = Color.White
                    )
                }
            }
        },

        supportingText = if (showError) {
            { CustomSupportingText(Modifier.padding(bottom = 5.dp), "Minimum $minCharacters characters") }
        }

        // Doesn't generate padding if null
        else {
            null
        }
    )
}
