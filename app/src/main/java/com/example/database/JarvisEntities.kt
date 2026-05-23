package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "command_logs")
data class CommandLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val commandText: String,
    val response: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: String, // "OPEN_APP", "TOGGLE_FLASHLIGHT", "TYPE_TEXT", "CHAT", "WEB_SEARCH", "NONE"
    val success: Boolean
)

@Entity(tableName = "jarvis_settings")
data class JarvisSetting(
    @PrimaryKey val configKey: String,
    val configValue: String
)
