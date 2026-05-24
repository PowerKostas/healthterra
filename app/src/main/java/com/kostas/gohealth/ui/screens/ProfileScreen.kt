package com.kostas.gohealth.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kostas.gohealth.ui.components.general.CustomSurface
import com.kostas.gohealth.ui.components.general.DropdownMenu
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
    var username by remember { mutableStateOf(userSettings.username ?: "") }
    var gender by remember { mutableStateOf(userCharacteristics.gender ?: "") }
    var age by remember { mutableStateOf(formatNumber(userCharacteristics.age)) }
    var height by remember { mutableStateOf(formatNumber(userCharacteristics.height)) }
    var weight by remember { mutableStateOf(formatNumber(userCharacteristics.weight)) }
    var activityLevel by remember { mutableStateOf(userCharacteristics.activityLevel ?: "") }

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
        ) {
            Spacer(modifier = Modifier.height(24.dp))

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
                    .padding(start = 16.dp)
            )

            CustomSurface {
                Column(
                    verticalArrangement = Arrangement.spacedBy(space = 24.dp),
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
                                    Text(
                                        text = "Username must be at least 5 characters",
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.offset(x = (-16).dp)
                                    )
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
                        150f,
                        age
                    ) { newValue ->
                        age = newValue
                        userCharacteristics.let { characteristics ->
                            characteristicsViewModel.updateUserCharacteristics(
                                characteristics.copy(age = newValue.toFloatOrNull())
                            )
                        }
                    }

                    NumberTextField(
                        "Height (cm)",
                        300f,
                        height
                    ) { newValue ->
                        height = newValue
                        userCharacteristics.let { characteristics ->
                            characteristicsViewModel.updateUserCharacteristics(
                                characteristics.copy(height = newValue.toFloatOrNull())
                            )
                        }
                    }

                    NumberTextField(
                        "Weight (kg)",
                        700f,
                        weight
                    ) { newValue ->
                        weight = newValue
                        userCharacteristics.let { characteristics ->
                            characteristicsViewModel.updateUserCharacteristics(
                                characteristics.copy(weight = newValue.toFloatOrNull())
                            )
                        }
                    }

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
                    .padding(start = 16.dp)
            )

            CustomSurface {
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
                    .padding(start = 16.dp)
            )

            CustomSurface {
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
        }
    }
}
