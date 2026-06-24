package com.healthterra.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.healthterra.helpers.generateRandomUsername
import com.healthterra.services.FirebaseDeleteWorker
import com.healthterra.services.roomDelete
import com.healthterra.ui.components.general.ActionButton
import com.healthterra.ui.components.general.CustomSurface
import com.healthterra.ui.components.general.DropdownMenu
import com.healthterra.ui.components.general.InfoDialog
import com.healthterra.ui.components.general.NumberTextField
import com.healthterra.ui.components.general.RadioButtonGroup
import com.healthterra.ui.components.screen.ProfilePicture
import com.healthterra.ui.components.screen.WeightGoalSelector
import com.healthterra.ui.viewModels.CharacteristicsViewModel
import com.healthterra.ui.viewModels.DailyTrackingsViewModel
import com.healthterra.ui.viewModels.SettingsViewModel
import com.healthterra.ui.viewModels.TrackingsViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val characteristicsViewModel: CharacteristicsViewModel = viewModel(factory = CharacteristicsViewModel.Factory)
    val userCharacteristicsList by characteristicsViewModel.characteristics.collectAsState()
    val userCharacteristics = userCharacteristicsList.firstOrNull()
    val dailyTrackingsViewModel: DailyTrackingsViewModel = viewModel(factory = DailyTrackingsViewModel.Factory)

    // Uses the same instance of SettingsViewModel as MainActivity to fix bug on non-essential user settings Firestore syncing
    val context = LocalContext.current
    val activity = context as ComponentActivity

    val settingsViewModel: SettingsViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = SettingsViewModel.Factory
    )

    val userSettingsList by settingsViewModel.settings.collectAsState()
    val userSettings = userSettingsList.firstOrNull()

    val trackingsViewModel: TrackingsViewModel = viewModel(factory = TrackingsViewModel.Factory)
    val userTrackingsList by trackingsViewModel.trackings.collectAsState()
    val userTrackings = userTrackingsList.firstOrNull()

    // Waits for the database to load
    if (userCharacteristics == null || userSettings == null || userTrackings == null) {
        return
    }

    val formatNumber = { num: Float? ->
        when {
            num == null -> ""
            num % 1 == 0f -> num.toInt().toString()
            else -> num.toString()
        }
    }

    // Initializes the variables that are used in text fields with the values from the database
    var username by remember { mutableStateOf(userSettings.username) }
    var age by remember { mutableStateOf(formatNumber(userCharacteristics.age)) }
    var height by remember { mutableStateOf(formatNumber(userCharacteristics.height)) }
    var weight by remember { mutableStateOf(formatNumber(userCharacteristics.weight)) }

    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    var uidText by remember { mutableStateOf(Firebase.auth.currentUser?.uid ?: "None") }

    val focusManager = LocalFocusManager.current

    // Draws the screen
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 4.dp)
    ) {
        ProfilePicture(userSettings.profilePictureString) { newProfilePictureString ->
            // Function that triggers when a new profile picture is tapped, it makes sure that a user is actually loaded on the screen, updates
            // the UI instantly, creates a copy of the user and only updates the profile picture String in the local database
            userSettings.let { settings ->
                settingsViewModel.updateUserSettings(settings.copy(profilePictureString = newProfilePictureString), context)
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
                val isError = username.length < 5
                OutlinedTextField(
                    value = username,
                    label = { Text("Username") },
                    isError = isError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused) {
                                // Local database update
                                if (username.length >= 5) {
                                    settingsViewModel.updateUserSettings(userSettings.copy(username = username), context)
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
                                        tint = Color(0xFFE53935),
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
                        }
                    }
                )

                DropdownMenu(
                    "Gender", listOf("Male", "Female"),
                    userCharacteristics.gender ?: ""
                ) { newValue ->
                    userCharacteristics.let { characteristics ->
                        characteristicsViewModel.updateUserCharacteristics(characteristics.copy(gender = newValue), context)
                    }

                    focusManager.clearFocus() // Clears focus upon selection
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
                                characteristicsViewModel.updateUserCharacteristics(characteristics.copy(age = ageValue), context)
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
                                characteristicsViewModel.updateUserCharacteristics(characteristics.copy(height = heightValue), context)
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
                                characteristicsViewModel.updateUserCharacteristics(characteristics.copy(weight = weightValue), context)
                            }
                        }
                    },

                    onValueChange = { newValue -> weight = newValue }
                )

                DropdownMenu(
                    "Activity Level", listOf("Sedentary", "Moderate", "High"),
                    userCharacteristics.activityLevel ?: ""
                ) { newValue ->
                    userCharacteristics.let { characteristics ->
                        characteristicsViewModel.updateUserCharacteristics(characteristics.copy(activityLevel = newValue), context)
                    }

                    focusManager.clearFocus()
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "Weight Goal")

                    WeightGoalSelector(
                        userCharacteristics,
                        userSettings
                    ) { newWeightGoal, newKgGoal, newDaysGoal ->
                        userCharacteristics.let { characteristics ->
                            characteristicsViewModel.updateUserCharacteristics(characteristics.copy(weightGoal = newWeightGoal, kgGoal = newKgGoal, daysGoal = newDaysGoal), context)

                            // Also updates initialWeightGoalDate so it starts the count again, it becomes null if maintain is selected
                            settingsViewModel.updateUserSettings(
                                if (newWeightGoal == "Maintain") {
                                    userSettings.copy(initialWeightGoalDate = null)
                                }

                                else {
                                    userSettings.copy(initialWeightGoalDate = LocalDate.now().toString())
                                },

                                context
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Settings",
            modifier = Modifier
                .align(Alignment.Start)
        )

        CustomSurface(startPadding = 0.dp, topPadding = 4.dp, endPadding = 0.dp) {
            Column(
                verticalArrangement = Arrangement.spacedBy(32.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Appearance",
                        style = MaterialTheme.typography.labelLarge
                    )

                    RadioButtonGroup(
                        listOf("Light", "Dark", "Dynamic"),
                        userSettings.appearance
                    ) { newAppearance ->
                        userSettings.let { settings ->
                            settingsViewModel.updateUserSettings(settings.copy(appearance = newAppearance), context)
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Leaderboards Visibility",
                        style = MaterialTheme.typography.labelLarge
                    )

                    RadioButtonGroup(
                        listOf("Public", "Anonymous"),
                        userSettings.leaderboardsVisibility
                    ) { newSetting ->
                        userSettings.let { settings ->
                            settingsViewModel.updateUserSettings(settings.copy(leaderboardsVisibility = newSetting), context)
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Step Tracking",
                        style = MaterialTheme.typography.labelLarge
                    )

                    RadioButtonGroup(
                        listOf("Enabled", "Disabled"),
                        userSettings.stepTracking
                    ) { newSetting ->
                        userSettings.let { settings ->
                            settingsViewModel.updateUserSettings(settings.copy(stepTracking = newSetting), context)
                        }
                    }
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

                val siteHomeUrl = "https://powerkostas.github.io/healthterra-web/"
                val uriHandler = LocalUriHandler.current
                Text(
                    text = "Support & Legal",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF0645AD),
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .padding(start = 2.dp)
                        .clickable {
                            uriHandler.openUri(siteHomeUrl)
                        }
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

    if (showDeleteDialog) {
        InfoDialog(
            icon = Icons.Default.Error,
            iconColour = Color(0xFFE53935),
            title = null,
            text = AnnotatedString("Your account and all associated data will be permanently deleted. This action is irreversible. Are you sure you want to proceed?"),
            confirmText = "Confirm",
            dismissText = "Cancel",
            isCancelable = true,
            fontWeight = FontWeight.Bold,

            onConfirm = {
                showDeleteDialog = false

                // Text fields UI delete
                val randomUsername = generateRandomUsername()
                username = randomUsername
                age = ""
                height = ""
                weight = ""

                // UI and local database delete
                roomDelete(characteristicsViewModel, settingsViewModel, trackingsViewModel, dailyTrackingsViewModel, randomUsername, context)

                // Firebase delete, needs network
                val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

                val deleteRequest = OneTimeWorkRequestBuilder<FirebaseDeleteWorker>()
                    .setConstraints(constraints)
                    .build()

                WorkManager.getInstance(context).enqueue(deleteRequest)

                showDeleteDialog = false
                uidText = "None"
            },

            onDismiss = { showDeleteDialog = false }
        )
    }
}
