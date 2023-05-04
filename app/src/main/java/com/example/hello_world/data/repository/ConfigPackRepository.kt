package com.example.hello_world.data.repository

import com.example.hello_world.models.ConfigPack
import java.util.UUID

interface ConfigPackRepository {
    fun addConfigPack(configPack: ConfigPack)
    fun removeConfigPack(configPack: ConfigPack)
    fun getAllConfigPacks(): List<ConfigPack>
    fun findConfigPackByName(name: String): ConfigPack?
}


class DefaultConfigPackRepository : ConfigPackRepository {
    private val configPacks = mutableListOf<ConfigPack>()

    init {
        // Add default ConfigPacks here
        configPacks.add(ConfigPack(UUID.randomUUID(),"GPT3.5", "I am an AI assistant named Jake.", 1000, 0.9, 0.0, 0.1, "gpt-3.5-turbo"))
        configPacks.add(ConfigPack(UUID.randomUUID(),"GPT4", "I am here to help you today. What do you need?", 1500, 0.8, 0.1, 0.2, "gpt-4"))
    }

    override fun addConfigPack(configPack: ConfigPack) {
        configPacks.add(configPack)
    }

    override fun removeConfigPack(configPack: ConfigPack) {
        configPacks.remove(configPack)
    }

    override fun getAllConfigPacks(): List<ConfigPack> {
        return configPacks.toList()
    }

    override fun findConfigPackByName(name: String): ConfigPack? {
        return configPacks.firstOrNull { it.name == name }
    }
}