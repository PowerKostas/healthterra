package com.kostas.gohealth.ui.components.general

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

// Custom button to do any action
@Composable
fun ActionButton(modifier: Modifier = Modifier, colour: Color, icon: Int? = null, text: String, fontSize: TextUnit, action: () -> Unit) {
    Button(
        onClick = action,

        colors = ButtonDefaults.buttonColors(
            containerColor = colour,
            contentColor = Color.White
        ),

        shape = RoundedCornerShape(16.dp),

        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 12.dp),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        ) {
            if (icon != null) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = "Button Icon",
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = text,
                fontSize = fontSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
