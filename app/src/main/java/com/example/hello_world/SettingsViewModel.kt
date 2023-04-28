package com.example.hello_world

import ConfigPack
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel


class SettingsViewModel : ViewModel() {

    val defaultConfigPacks = listOf(
        ConfigPack("Profile 1", "You are an AI assistant named Jake.", 100, 0.9, 0.0, 0.1, "gpt-3.5-turbo"),
        ConfigPack("Profile 2", "You are an AI assistant named Jane.", 150, 0.8, 0.1, 0.2, "gpt-3.5-turbo")
    )
    val editedConfigPack = mutableStateOf(ConfigPack("", "", 100, 0.9, 0.0, 0.1, "gpt-3.5-turbo"))

    fun updateEditedProfileName(name: String) {
        Log.d("SettingsViewModel", "Profile name updated: $name")
        editedConfigPack.value = editedConfigPack.value.copy(name = name)
    }
    var profiles by mutableStateOf(defaultConfigPacks)
//    var selectedProfile by mutableStateOf<Profile?>(null)
    var selectedConfigPack by mutableStateOf<ConfigPack?>(defaultConfigPacks.first())

    fun saveEditedProfile() {
        if (editedConfigPack.value.name.isNotBlank()) {
            Log.d("SettingsViewModel", "Saving edited profile: ${editedConfigPack.value}")
            saveCustomProfile(editedConfigPack.value)
        }
    }

    fun updateEditedProfileSystemMessage(systemMessage: String) {
        Log.d("SettingsViewModel", "System message updated: $systemMessage")
        editedConfigPack.value = editedConfigPack.value.copy(systemMessage = systemMessage)
    }

    fun updateEditedProfileMaxLength(maxLength: Int) {
        Log.d("SettingsViewModel", "Max length updated: $maxLength")
        editedConfigPack.value = editedConfigPack.value.copy(maxLength = maxLength)
    }

    fun updateEditedProfileTemperature(temperature: Double) {
        Log.d("SettingsViewModel", "Temperature updated: $temperature")
        editedConfigPack.value = editedConfigPack.value.copy(temperature = temperature)
    }

    fun updateEditedProfileFrequencyPenalty(frequencyPenalty: Double) {
        Log.d("SettingsViewModel", "Frequency Penalty updated: $frequencyPenalty")
        editedConfigPack.value = editedConfigPack.value.copy(frequencyPenalty = frequencyPenalty)
    }

    fun updateEditedProfilePresencePenalty(presencePenalty: Double) {
        Log.d("SettingsViewModel", "Presence Penalty updated: $presencePenalty")
        editedConfigPack.value = editedConfigPack.value.copy(presencePenalty = presencePenalty)
    }

    fun updateEditedProfileModel(model: String) {
        Log.d("SettingsViewModel", "Model updated: $model")
        editedConfigPack.value = editedConfigPack.value.copy(model = model)
    }

    fun saveCustomProfile(configPack: ConfigPack) {
        Log.d("SettingsViewModel", "Saving profile: $configPack")
        profiles = profiles.filter { it.name != configPack.name } + configPack
    }


    fun deleteProfile(configPack: ConfigPack) {
        Log.d("SettingsViewModel", "Deleting profile: $configPack")
        profiles = profiles.filter { it != configPack }
    }

    fun applyProfile(configPack: ConfigPack) {
        Log.d("SettingsViewModel", "Applying profile: $configPack")
        selectedConfigPack = configPack
    }
}