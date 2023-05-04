package com.example.hello_world.data.repository

import com.example.hello_world.models.ConfigPack

interface IConfigPackRepository {
    suspend fun addConfigPack(configPack: ConfigPack)
    suspend fun removeConfigPack(configPack: ConfigPack)
    suspend fun getAllConfigPacks(): List<ConfigPack>
}