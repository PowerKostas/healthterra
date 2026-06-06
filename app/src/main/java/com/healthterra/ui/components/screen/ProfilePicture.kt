package com.healthterra.ui.components.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import com.healthterra.R

// Map is public so other functions can use it too
val avatarMap = mapOf(
    "alien" to R.drawable.alien,
    "cat" to R.drawable.cat,
    "dinosaur" to R.drawable.dinosaur,
    "dog" to R.drawable.dog,
    "dolphin" to R.drawable.dolphin,
    "dragon" to R.drawable.dragon,
    "duck" to R.drawable.duck,
    "eagle" to R.drawable.eagle,
    "elephant" to R.drawable.elephant,
    "flamingo" to R.drawable.flamingo,
    "horse" to R.drawable.horse,
    "lion" to R.drawable.lion,
    "monkey" to R.drawable.monkey,
    "mouse" to R.drawable.mouse,
    "octopus" to R.drawable.octopus,
    "panda" to R.drawable.panda,
    "penguin" to R.drawable.penguin,
    "pig" to R.drawable.pig,
    "sheep" to R.drawable.sheep,
    "snail" to R.drawable.snail,
    "turtle" to R.drawable.turtle
).withDefault { R.drawable.fox } // Only triggers on unexpected/cheating circumstances

// Adds a profile picture and a button below it that opens a menu to optionally select a new picture
@Composable
fun ProfilePicture(profilePictureString: String, onImageSelected: (String) -> Unit) {
    var showMenu by rememberSaveable { mutableStateOf(false) }

    // Gets the profile picture from the profile picture string
    val profilePicture = avatarMap.getValue(profilePictureString)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Coil's AsyncImage is faster for image loading
        AsyncImage(
            model = profilePicture,
            contentDescription = "Animal Avatar",
            modifier = Modifier
                .size(120.dp)
                .border(width = 2.dp, color = MaterialTheme.colorScheme.onPrimary, shape = CircleShape)
                .clip(CircleShape)
        )

        Button(
            border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.onPrimary),
            onClick = { showMenu = true },
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = "Select Profile Picture",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }

    if (showMenu) {
        Dialog(onDismissRequest = { showMenu = false }) {
            Surface(shape = RoundedCornerShape(16.dp)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) { items(avatarMap.entries.toList()) { (imageString, image) ->
                    AsyncImage(
                        model = image,
                        contentDescription = "Animal Choice",
                        modifier = Modifier
                            .aspectRatio(1f)
                            .border(width = 2.dp, color = MaterialTheme.colorScheme.onPrimary, shape = CircleShape)
                            .clip(CircleShape)
                            .clickable {
                                onImageSelected(imageString)
                                showMenu = false
                            }
                    )
                    }
                }
            }
        }
    }
}
