package com.example.hello_world

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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

@Composable
fun SettingsScreen(settingsViewModel: SettingsViewModel, onProfileApplied: () -> Unit, navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()) {
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
                    Button(onClick = { onProfileApplied() }) {
                        Text("Apply")
                    }
                    Button(
                        onClick = { navController.navigate("edit-settings") },
//                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Edit")
                    }
                    if (profile !in settingsViewModel.defaultProfiles) {
                        Button(onClick = { settingsViewModel.deleteProfile(profile) }) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}