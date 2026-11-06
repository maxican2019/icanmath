package com.example.matematikaceria

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

/**
 * Penyimpanan lokal berupa satu file JSON (filesDir/data.json).
 * Format yang sama dipakai sebagai file cadangan, jadi backup dan restore sederhana.
 */
class Repository(context: Context) {
    private val file = File(context.filesDir, "data.json")
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()

    private val _data = MutableStateFlow(load())
    val data: StateFlow<AppData> = _data.asStateFlow()

    init {
        persist()
    }

    private fun load(): AppData {
        val loaded = try {
            if (file.exists()) json.decodeFromString<AppData>(file.readText()) else AppData()
        } catch (e: Exception) {
            AppData()
        }
        return if (loaded.backupId.isEmpty()) loaded.copy(backupId = UUID.randomUUID().toString()) else loaded
    }

    private fun persist() {
        scope.launch {
            mutex.withLock {
                file.writeText(json.encodeToString(_data.value))
            }
        }
    }

    fun update(block: (AppData) -> AppData) {
        _data.update { block(it) }
        persist()
    }

    fun exportJson(): String = json.encodeToString(_data.value)

    fun importJson(text: String): Boolean {
        return try {
            val imported = json.decodeFromString<AppData>(text)
            if (imported.backupId.isEmpty()) {
                false
            } else {
                _data.value = imported
                persist()
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    fun reset() {
        _data.value = AppData(backupId = _data.value.backupId)
        persist()
    }
}
