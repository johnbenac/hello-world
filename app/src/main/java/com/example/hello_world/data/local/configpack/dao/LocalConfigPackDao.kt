package com.example.hello_world.data.local.configpack.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.hello_world.data.local.configpack.entities.LocalConfigPackEntity

@Dao
interface LocalConfigPackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfigPack(configPack: LocalConfigPackEntity)

    @Query("SELECT * FROM config_packs")
    suspend fun getAllConfigPacks(): List<LocalConfigPackEntity>

    @Query("DELETE FROM config_packs WHERE id = :configPackId")
    suspend fun deleteConfigPack(configPackId: String)
}