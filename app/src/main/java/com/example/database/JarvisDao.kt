package com.example.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JarvisDao {
    @Query("SELECT * FROM command_logs ORDER BY timestamp DESC")
    fun getAllCommandLogs(): Flow<List<CommandLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommandLog(log: CommandLog)

    @Query("DELETE FROM command_logs")
    suspend fun clearAllCommandLogs()

    @Query("SELECT * FROM jarvis_settings WHERE configKey = :key")
    suspend fun getSettingByKey(key: String): JarvisSetting?

    @Query("SELECT * FROM jarvis_settings")
    fun getAllSettings(): Flow<List<JarvisSetting>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: JarvisSetting)
}
