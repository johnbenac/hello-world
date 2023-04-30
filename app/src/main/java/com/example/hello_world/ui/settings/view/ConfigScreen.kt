package com.example.hello_world

import com.example.hello_world.models.ConfigPack
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hello_world.ui.settings.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(settingsViewModel: SettingsViewModel, onProfileApplied: () -> Unit, navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Current Settings", modifier = Modifier.padding(16.dp))
        CurrentSettings(settingsViewModel.selectedConfigPack)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Select a profile", modifier = Modifier.padding(16.dp))

        settingsViewModel.profiles.forEach { profile ->
            Card(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .clickable { settingsViewModel.applyProfile(profile) }
                    .shadow(elevation = 4.dp) // Add shadow with the 4.dp elevation
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = profile.name)
                    Button(onClick = {
                        Log.d("SettingsScreen", "Apply button clicked for profile: $profile")
                        settingsViewModel.applyProfile(profile)
                        onProfileApplied()
                    }) {
                        Text("Apply")
                    }
                    Button(
                        onClick = {
                            Log.d("SettingsScreen", "Edit button clicked for profile: $profile")
                            navController.navigate("edit-settings")
                        }
                    ) {
                        Text("Edit")
                    }
                    if (profile !in settingsViewModel.defaultConfigPacks) {
                        Button(onClick = {
                            Log.d("SettingsScreen", "Delete button clicked for profile: $profile")
                            settingsViewModel.deleteProfile(profile)
                        }) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CurrentSettings(selectedConfigPack: ConfigPack?) {
    selectedConfigPack?.let { profile ->
        Card(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .shadow(elevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(text = "Model: ${profile.model}")
                Text(text = "System Message: ${profile.systemMessage}")
                Text(text = "Max Length: ${profile.maxLength}")
                Text(text = "Temperature: ${profile.temperature}")
                Text(text = "Frequency Penalty: ${profile.frequencyPenalty}")
                Text(text = "Presence Penalty: ${profile.presencePenalty}")
            }
        }
    }
}