package com.icanmath.app.inventory

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull

/** Sumber data lama (key-value). Pada aplikasi native, implementasinya membaca SharedPreferences. */
interface LegacySource {
    fun get(key: String): String?
    fun remove(key: String)
}

/** Migrasi dari prototipe Math Battle: localStorage "mb_inv" berformat {"kayu":n,"besi":n,"orb":n}. */
object LegacyMigration {
    const val LEGACY_KEY = "mb_inv"

    /** Nama lama ke id item baru. */
    private val keyMap = mapOf("kayu" to "kayu", "besi" to "besi", "orb" to "orb_tarantula")

    /**
     * Mengurai isi "mb_inv". Mengembalikan null jika bukan objek JSON.
     * Nilai yang bukan bilangan bulat >= 0 dilewati; kunci tak dikenal diabaikan.
     */
    fun parse(raw: String): Map<String, Int>? {
        val element = try {
            Json.parseToJsonElement(raw)
        } catch (e: Exception) {
            return null
        }
        val obj = element as? JsonObject ?: return null
        val out = LinkedHashMap<String, Int>()
        for ((key, value) in obj) {
            val id = keyMap[key] ?: continue
            val prim = value as? JsonPrimitive ?: continue
            if (prim.isString) continue
            val d = prim.doubleOrNull ?: continue
            if (d < 0.0 || d != Math.floor(d) || d > MAX_STACK) continue
            out[id] = d.toInt()
        }
        return out
    }
}
