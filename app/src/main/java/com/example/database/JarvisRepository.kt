package com.example.database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class JarvisRepository(private val jarvisDao: JarvisDao) {

    val commandLogs: Flow<List<CommandLog>> = jarvisDao.getAllCommandLogs()
    
    val allSettings: Flow<Map<String, String>> = jarvisDao.getAllSettings().map { list ->
        list.associate { it.configKey to it.configValue }
    }

    suspend fun logCommand(commandText: String, response: String, actionType: String, success: Boolean) {
        val log = CommandLog(
            commandText = commandText,
            response = response,
            actionType = actionType,
            success = success
        )
        jarvisDao.insertCommandLog(log)
    }

    suspend fun clearLogs() {
        jarvisDao.clearAllCommandLogs()
    }

    suspend fun getSetting(key: String, defaultValue: String): String {
        return jarvisDao.getSettingByKey(key)?.configValue ?: defaultValue
    }

    suspend fun saveSetting(key: String, value: String) {
        jarvisDao.insertSetting(JarvisSetting(key, value))
    }
}
