package com.icanmath.app.inventory

import android.content.Context
import java.io.File

/** Sumber data lama di Android: SharedPreferences "mb_legacy". */
class PrefsLegacySource(context: Context) : LegacySource {
    private val prefs = context.getSharedPreferences("mb_legacy", Context.MODE_PRIVATE)
    override fun get(key: String): String? = prefs.getString(key, null)
    override fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }
}

/** Satu instance Inventory untuk seluruh aplikasi. Semua game mengambilnya dari sini. */
object InventoryHolder {
    @Volatile
    private var instance: Inventory? = null

    fun get(context: Context): Inventory {
        instance?.let { return it }
        return synchronized(this) {
            instance ?: create(context.applicationContext).also { instance = it }
        }
    }

    private fun create(context: Context): Inventory {
        val inventory = Inventory(FileInventoryStore(File(context.filesDir, "inventory_v1.json")))
        inventory.migrateLegacy(PrefsLegacySource(context))
        return inventory
    }
}
