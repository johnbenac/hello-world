package com.example.hello_world

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel


class SettingsViewModel : ViewModel() {

    val defaultProfiles = listOf(
        Profile("Profile 1", "You are an AI assistant named Jake.", 500, 0.9, 0.0, 0.1, "gpt-3.5-turbo"),
        Profile("Profile 2", "You are an AI assistant named Jane.", 550, 0.8, 0.1, 0.2, "gpt-3.5-turbo")
    )
    val editedProfile = mutableStateOf(Profile("", "", 100, 0.9, 0.0, 0.1, "gpt-3.5-turbo"))

    fun updateEditedProfileName(name: String) {
        Log.d("SettingsViewModel", "Profile name updated: $name")
        editedProfile.value = editedProfile.value.copy(name = name)
    }
    var profiles by mutableStateOf(defaultProfiles)
//    var selectedProfile by mutableStateOf<Profile?>(null)
    var selectedProfile by mutableStateOf<Profile?>(defaultProfiles.first())

    fun saveEditedProfile() {
        if (editedProfile.value.name.isNotBlank()) {
            Log.d("SettingsViewModel", "Saving edited profile: ${editedProfile.value}")
            saveCustomProfile(editedProfile.value)
        }
    }

    fun updateEditedProfileSystemMessage(systemMessage: String) {
        Log.d("SettingsViewModel", "System message updated: $systemMessage")
        editedProfile.value = editedProfile.value.copy(systemMessage = systemMessage)
    }

    fun updateEditedProfileMaxLength(maxLength: Int) {
        Log.d("SettingsViewModel", "Max length updated: $maxLength")
        editedProfile.value = editedProfile.value.copy(maxLength = maxLength)
    }

    fun updateEditedProfileTemperature(temperature: Double) {
        Log.d("SettingsViewModel", "Temperature updated: $temperature")
        editedProfile.value = editedProfile.value.copy(temperature = temperature)
    }

    fun updateEditedProfileFrequencyPenalty(frequencyPenalty: Double) {
        Log.d("SettingsViewModel", "Frequency Penalty updated: $frequencyPenalty")
        editedProfile.value = editedProfile.value.copy(frequencyPenalty = frequencyPenalty)
    }

    fun updateEditedProfilePresencePenalty(presencePenalty: Double) {
        Log.d("SettingsViewModel", "Presence Penalty updated: $presencePenalty")
        editedProfile.value = editedProfile.value.copy(presencePenalty = presencePenalty)
    }

    fun updateEditedProfileModel(model: String) {
        Log.d("SettingsViewModel", "Model updated: $model")
        editedProfile.value = editedProfile.value.copy(model = model)
    }

    fun saveCustomProfile(profile: Profile) {
        Log.d("SettingsViewModel", "Saving profile: $profile")
        profiles = profiles.filter { it.name != profile.name } + profile
    }


    fun deleteProfile(profile: Profile) {
        Log.d("SettingsViewModel", "Deleting profile: $profile")
        profiles = profiles.filter { it != profile }
    }

    fun applyProfile(profile: Profile) {
        Log.d("SettingsViewModel", "Applying profile: $profile")
        selectedProfile = profile
    }
}