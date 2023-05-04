package com.example.hello_world.data.repository


import com.example.hello_world.models.ConfigPack
import android.content.Context
import com.example.hello_world.data.local.configpack.entities.LocalConfigPackEntity
import com.example.hello_world.data.local.conversation.database.LocalConversationDatabase
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.UUID

class LocalRoomConfigPackRepository(context: Context) : IConfigPackRepository {
    private val configPackDao = LocalConversationDatabase.getInstance(context).configPackDao()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    override suspend fun addConfigPack(configPack: ConfigPack) {
        val localConfigPack = LocalConfigPackEntity(
            id = configPack.id.toString(),
            name = configPack.name,
            systemMessage = configPack.systemMessage,
            maxLength = configPack.maxLength,
            temperature = configPack.temperature,
            frequencyPenalty = configPack.frequencyPenalty,
            presencePenalty = configPack.presencePenalty,
            model = configPack.model
        )
        configPackDao.insertConfigPack(localConfigPack)
    }

    override suspend fun removeConfigPack(configPack: ConfigPack) {
        configPackDao.deleteConfigPack(configPack.id.toString())
    }

    override suspend fun getAllConfigPacks(): List<ConfigPack> {
        val localConfigPacks = configPackDao.getAllConfigPacks()
        return localConfigPacks.map { localConfigPack ->
            ConfigPack(
                id = UUID.fromString(localConfigPack.id),
                name = localConfigPack.name,
                systemMessage = localConfigPack.systemMessage,
                maxLength = localConfigPack.maxLength,
                temperature = localConfigPack.temperature,
                frequencyPenalty = localConfigPack.frequencyPenalty,
                presencePenalty = localConfigPack.presencePenalty,
                model = localConfigPack.model
            )
        }
    }

//    override suspend fun findConfigPackByName(name: String): ConfigPack? {
//        // TODO: Implement finding a configPack by name in the database
//    }
}