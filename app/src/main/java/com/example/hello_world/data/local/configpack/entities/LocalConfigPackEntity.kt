package com.example.hello_world.data.local.configpack.entities


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "config_packs")
data class LocalConfigPackEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val systemMessage: String,
    val maxLength: Int,
    val temperature: Double,
    val frequencyPenalty: Double,
    val presencePenalty: Double,
    val model: String
)