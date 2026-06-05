package com.kostas.gohealth.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.kostas.gohealth.helpers.generateRandomProfilePictureString
import com.kostas.gohealth.helpers.generateRandomUsername
import com.kostas.gohealth.ui.components.general.ActionButton
import com.kostas.gohealth.ui.components.general.CustomSurface
import com.kostas.gohealth.ui.components.general.DropdownMenu
import com.kostas.gohealth.ui.components.general.InfoDialog
import com.kostas.gohealth.ui.components.general.NumberTextField
import com.kostas.gohealth.ui.components.general.RadioButtonGroup
import com.kostas.gohealth.ui.components.screen.ProfilePicture
import com.kostas.gohealth.ui.components.screen.WeightGoalSelector
import com.kostas.gohealth.ui.viewModels.CharacteristicsViewModel
import com.kostas.gohealth.ui.viewModels.SettingsViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val characteristicsViewModel: CharacteristicsViewModel = viewModel(factory = CharacteristicsViewModel.Factory)
    val userCharacteristicsList by characteristicsViewModel.characteristics.collectAsState()
    val userCharacteristics = userCharacteristicsList.firstOrNull()

    val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
    val userSettingsList by settingsViewModel.settings.collectAsState()
    val userSettings = userSettingsList.firstOrNull()

    // Waits for the database to load
    if (userCharacteristics == null || userSettings == null) {
        return
    }

    val formatNumber = { num: Float? ->
        when {
            num == null -> ""
            num % 1 == 0f -> num.toInt().toString()
            else -> num.toString()
        }
    }

    // Initializes the variables with the values from the database
    var profilePictureString by remember { mutableStateOf(userSettings.profilePictureString) }
    var username by remember { mutableStateOf(userSettings.username) }
    var gender by remember { mutableStateOf(userCharacteristics.gender ?: "") }
    var age by remember { mutableStateOf(formatNumber(userCharacteristics.age)) }
    var height by remember { mutableStateOf(formatNumber(userCharacteristics.height)) }
    var weight by remember { mutableStateOf(formatNumber(userCharacteristics.weight)) }
    var activityLevel by remember { mutableStateOf(userCharacteristics.activityLevel ?: "") }

    var showDeleteDialog by remember { mutableStateOf(false) }

    var uidText by remember { mutableStateOf(Firebase.auth.currentUser?.uid ?: "None") }

    // Draws the screen
    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, top = 24.dp, end = 16.dp)
        ) {
            ProfilePicture(profilePictureString) {
                // Function that triggers when a new profile picture is tapped, it makes sure that a user is actually loaded on the screen, updates
                // the UI instantly, creates a copy of the user and only updates the profile picture String in the local database
                newProfilePictureString ->
                    profilePictureString = newProfilePictureString
                    userSettings.let { settings ->
                        settingsViewModel.updateUserSettings(
                            settings.copy(profilePictureString = newProfilePictureString)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Personal Details",
                modifier = Modifier
                    .align(Alignment.Start)
            )

            CustomSurface(startPadding = 0.dp, topPadding = 4.dp, endPadding = 0.dp) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    var isError = username.length < 5
                    OutlinedTextField(
                        value = username,
                        label = { Text("Username") },
                        isError = isError,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused) {
                                    if (username.length < 5) {
                                        isError = true
                                    }

                                    // Local database update
                                    else {
                                        settingsViewModel.updateUserSettings(userSettings.copy(username = username))
                                    }
                                }
                            },

                        // Adds an error/counting text below the field
                        supportingText = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (isError) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.offset(x = (-16).dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Error,
                                            contentDescription = "Error Icon",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )

                                        Text(text = "Minimum 5 characters")
                                    }
                                }

                                // Empty space to push the counter to the right
                                else {
                                    Text(text = "")
                                }

                                Text(
                                    text = "${username.length} / 15",
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.offset(x = 16.dp)
                                )
                            }
                        },

                        // Updates the UI, every time the text changes
                        onValueChange = { newValue ->
                            if (newValue.length <= 15) {
                                username = newValue

                                if (newValue.length >= 5) {
                                    isError = false
                                }
                            }
                        }
                    )

                    DropdownMenu(
                        "Gender", listOf("Male", "Female"),
                        gender
                    ) { newValue ->
                        gender = newValue
                        userCharacteristics.let { characteristics ->
                            characteristicsViewModel.updateUserCharacteristics(
                                characteristics.copy(gender = newValue)
                            )
                        }
                    }

                    NumberTextField(
                        "Age",
                        16f,
                        130f,
                        age,

                        onFocusLost = {
                            val ageValue = age.toFloatOrNull()
                            if (ageValue == null || ageValue >= 16f) {
                                userCharacteristics.let { characteristics ->
                                    characteristicsViewModel.updateUserCharacteristics(
                                        characteristics.copy(age = ageValue)
                                    )
                                }
                            }
                        },

                        onValueChange = { newValue -> age = newValue }
                    )

                    NumberTextField(
                        "Height (cm)",
                        50f,
                        280f,
                        height,

                        onFocusLost = {
                            val heightValue = height.toFloatOrNull()
                            if (heightValue == null || heightValue >= 50f) {
                                userCharacteristics.let { characteristics ->
                                    characteristicsViewModel.updateUserCharacteristics(
                                        characteristics.copy(height = heightValue)
                                    )
                                }
                            }
                        },

                        onValueChange = { newValue -> height = newValue }
                    )

                    NumberTextField(
                        "Weight (kg)",
                        20f,
                        700f,
                        weight,

                        onFocusLost = {
                            val weightValue = weight.toFloatOrNull()
                            if (weightValue == null || weightValue >= 20f) {
                                userCharacteristics.let { characteristics ->
                                    characteristicsViewModel.updateUserCharacteristics(
                                        characteristics.copy(weight = weightValue)
                                    )
                                }
                            }
                        },

                        onValueChange = { newValue -> weight = newValue }
                    )

                    DropdownMenu(
                        "Activity Level", listOf("Sedentary", "Moderate", "High"),
                        activityLevel
                    ) { newValue ->
                        activityLevel = newValue
                        userCharacteristics.let { characteristics ->
                            characteristicsViewModel.updateUserCharacteristics(
                                characteristics.copy(activityLevel = newValue)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)){
                        Text(text = "Weight Goal")

                        WeightGoalSelector(
                            userCharacteristics,
                            userSettings
                        ) { newWeightGoal, newKgGoal, newDaysGoal ->
                            userCharacteristics.let { characteristics ->
                                characteristicsViewModel.updateUserCharacteristics(
                                    characteristics.copy(weightGoal = newWeightGoal, kgGoal = newKgGoal, daysGoal = newDaysGoal)
                                )

                                // Also updates initialWeightGoalDate so it starts the count again if the user changes the weight goal or
                                // moves the sliders
                                settingsViewModel.updateUserSettings(
                                    userSettings.copy(initialWeightGoalDate = LocalDate.now().toString())
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Step Tracking",
                modifier = Modifier
                    .align(Alignment.Start)
            )

            CustomSurface(startPadding = 0.dp, topPadding = 4.dp, endPadding = 0.dp) {
                RadioButtonGroup(
                    listOf("Enabled", "Disabled"),
                    userSettings.stepTracking
                ) { newSetting ->
                    userSettings.let { settings ->
                        settingsViewModel.updateUserSettings(
                            settings.copy(stepTracking = newSetting)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Appearance",
                modifier = Modifier
                    .align(Alignment.Start)
            )

            CustomSurface(startPadding = 0.dp, topPadding = 4.dp, endPadding = 0.dp) {
                RadioButtonGroup(
                    listOf("Light", "Dark", "Dynamic"),
                    userSettings.appearance
                ) { newAppearance ->
                    userSettings.let { settings ->
                        settingsViewModel.updateUserSettings(
                            settings.copy(appearance = newAppearance)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(64.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment =  Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "UID: $uidText",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(start = 2.dp)
                    )

                    val siteHomeUrl = "https://powerkostas.github.io/gohealth-web/"
                    val uriHandler = LocalUriHandler.current
                    Text(
                        text = "Support & Legal",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF0645AD),
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .clickable {
                                uriHandler.openUri(siteHomeUrl)
                            },
                    )
                }

                ActionButton(
                    colour = Color(0xFFE53935),
                    text = "Delete Account",
                    fontSize = 12.sp
                ) {
                    showDeleteDialog = true
                }
            }
        }
    }

    if (showDeleteDialog) {
        InfoDialog(
            icon = Icons.Default.Error,
            iconColour = MaterialTheme.colorScheme.error,
            title = null,
            text = AnnotatedString("Your account and all associated data will be permanently deleted. This action is irreversible. Are you sure you want to proceed?"),
            confirmText = "Confirm",
            dismissText = "Cancel",
            isCancelable = true,

            onConfirm = {
                showDeleteDialog = false

                // UI delete
                val randomProfilePictureString = generateRandomProfilePictureString()
                val randomUsername = generateRandomUsername()
                profilePictureString = randomProfilePictureString
                username = randomUsername
                gender = ""
                age = ""
                height = ""
                weight = ""
                activityLevel = ""

                // Local database delete
                userCharacteristics.let { characteristics ->
                    characteristicsViewModel.updateUserCharacteristics(
                        characteristics.copy(
                            gender = null,
                            age = null,
                            height = null,
                            weight = null,
                            activityLevel = null,

                            // Does the UI delete too, different from the others because WeightGoalSelector handles its own variables
                            weightGoal = "Maintain",
                            kgGoal = 0,
                            daysGoal = 0
                        )
                    )
                }

                userSettings.let { settings ->
                    settingsViewModel.updateUserSettings(
                        settings.copy(
                            profilePictureString = randomProfilePictureString,
                            username = randomUsername,
                        )
                    )
                }

                // First Firestore delete, then Firebase Authentication delete and UI text change, the user would have to open the app again
                // to get a new empty account
                Firebase.firestore.collection("leaderboards").document(Firebase.auth.currentUser?.uid.toString()).delete().addOnSuccessListener {
                    Firebase.auth.currentUser?.delete()
                    showDeleteDialog = false
                    uidText = "None"
                }
            },

            onDismiss = { showDeleteDialog = false }
        )
    }
}
