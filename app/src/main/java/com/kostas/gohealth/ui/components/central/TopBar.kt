package com.kostas.gohealth.ui.components.central

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kostas.gohealth.R

// Top bar with the drawer menu button, the title and the GoHealth logo
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(title: String, onMenuClick: () -> Unit, onLogoClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHome = title == "Home"

    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Drawer Menu"
                )
            }
        },

        title = { Text(text = title) },

        actions = {
            val healthColor = if (isPressed && !isHome) Color(0xFF059669).copy(alpha = 0.6f) else Color(0xFF059669)
            val terraColor = if (isPressed && !isHome) Color(0xFF55403E).copy(alpha = 0.6f) else Color(0xFF55403E)

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = healthColor)) { append("Health") }
                    withStyle(style = SpanStyle(color = terraColor)) { append("terra") }
                },

                modifier = Modifier
                    .padding(end = 12.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onLogoClick() }
                    ),

                fontSize = 28.sp,
                fontFamily = FontFamily(Font(R.font.fredoka_bold))
            )
        }
    )
}
