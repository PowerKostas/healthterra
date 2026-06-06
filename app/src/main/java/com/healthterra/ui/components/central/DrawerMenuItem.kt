package com.healthterra.ui.components.central

import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

// Drawer menu item with an item image and title
@Composable
fun DrawerMenuItem(appIcon: Int, title: String, currentScreen: String, onItemClick: (String) -> Unit) {
    NavigationDrawerItem(
        icon = { Icon(painter = painterResource(id = appIcon), contentDescription = title, tint = Color.Unspecified) },
        label = { Text(title) },
        selected = currentScreen == title,
        shape = RectangleShape,
        modifier = Modifier.height(80.dp),
        onClick = { onItemClick(title) }
    )
}
