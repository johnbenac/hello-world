package com.example.hello_world.ui.ConfigPacks.viewmodel

import android.content.Context
import com.example.hello_world.models.ConfigPack
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hello_world.data.repository.ConfigPackRepository
import com.example.hello_world.data.repository.IConfigPackRepository
import com.example.hello_world.data.repository.LocalRoomConfigPackRepository
import kotlinx.coroutines.launch
import java.util.UUID


class ConfigPacksViewModel(private val configPackRepository: IConfigPackRepository) : ViewModel() {

    val defaultConfigPacks = listOf(
        ConfigPack(UUID.randomUUID(),"GPT3.5", "I am an AI assistant named Jake.", 1000, 0.9, 0.0, 0.1, "gpt-3.5-turbo"),
        ConfigPack(UUID.randomUUID(),"GPT4", "I am here to help you today. What do you need?", 1500, 0.8, 0.1, 0.2, "gpt-4")
    )
    val editedConfigPack = mutableStateOf(ConfigPack(UUID.randomUUID(), "default","I am an AI assistant", 1000, 0.9, 0.0, 0.1, "gpt-3.5-turbo"))

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

    suspend fun addDefaultConfigPacksIfNeeded() {
        val existingConfigPacks = configPackRepository.getAllConfigPacks()
        defaultConfigPacks.forEach { defaultConfigPack ->
            if (existingConfigPacks.none { it.name == defaultConfigPack.name }) {
                configPackRepository.addConfigPack(defaultConfigPack)
            }
        }
    }

    init {
        viewModelScope.launch {
            addDefaultConfigPacksIfNeeded()
            loadConfigPacks()
        }
    }

    private suspend fun loadConfigPacks() {
        Log.d("ConfigPacksViewModel", "Loading config packs from repository")
        profiles = configPackRepository.getAllConfigPacks()
        Log.d("ConfigPacksViewModel", "Loaded config packs: $profiles")
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
        viewModelScope.launch {
            configPackRepository.addConfigPack(configPack)
            loadConfigPacks()
        }
    }
    fun deleteProfile(configPack: ConfigPack) {
        Log.d("SettingsViewModel", "Deleting profile: $configPack")
        viewModelScope.launch {
            configPackRepository.removeConfigPack(configPack)
            profiles = configPackRepository.getAllConfigPacks()
        }
    }

    fun applyProfile(configPack: ConfigPack) {
        Log.d("SettingsViewModel", "Applying profile: $configPack")
        selectedConfigPack = configPack
    }
}

class ConfigPacksViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConfigPacksViewModel::class.java)) {
            return ConfigPacksViewModel(LocalRoomConfigPackRepository(context) as IConfigPackRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}