package com.example.hello_world

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel


class SettingsViewModel : ViewModel() {
    val defaultProfiles = listOf(
        Profile("Profile 1", "You are an AI assistant named Jake.", 100, 0.9, 0.0, 0.1, "gpt-3.5-turbo"),
        Profile("Profile 2", "You are an AI assistant named Jane.", 150, 0.8, 0.1, 0.2, "gpt-3.5-turbo")
    )
    val editedProfile = mutableStateOf(Profile("", "", 100, 0.9, 0.0, 0.1, "gpt-3.5-turbo"))

    fun updateEditedProfileName(name: String) {
        editedProfile.value = editedProfile.value.copy(name = name)
    }
    var profiles by mutableStateOf(defaultProfiles)
    var selectedProfile by mutableStateOf<Profile?>(null)

    fun saveEditedProfile() {
        if (editedProfile.value.name.isNotBlank()) {
            saveCustomProfile(editedProfile.value)
        }
    }

    fun updateEditedProfileSystemMessage(systemMessage: String) {
        editedProfile.value = editedProfile.value.copy(systemMessage = systemMessage)
    }

    fun updateEditedProfileMaxLength(maxLength: Int) {
        editedProfile.value = editedProfile.value.copy(maxLength = maxLength)
    }

    fun updateEditedProfileTemperature(temperature: Double) {
        editedProfile.value = editedProfile.value.copy(temperature = temperature)
    }

    fun updateEditedProfileFrequencyPenalty(frequencyPenalty: Double) {
        editedProfile.value = editedProfile.value.copy(frequencyPenalty = frequencyPenalty)
    }

    fun updateEditedProfilePresencePenalty(presencePenalty: Double) {
        editedProfile.value = editedProfile.value.copy(presencePenalty = presencePenalty)
    }

    fun updateEditedProfileModel(model: String) {
        editedProfile.value = editedProfile.value.copy(model = model)
    }

    fun saveCustomProfile(profile: Profile) {
        profiles = profiles.filter { it.name != profile.name } + profile
    }


    fun deleteProfile(profile: Profile) {
        profiles = profiles.filter { it != profile }
    }

    fun applyProfile(profile: Profile) {
        selectedProfile = profile
    }
}