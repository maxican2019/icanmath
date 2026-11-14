package com.icanmath.app.inventory

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.Executor
import java.util.concurrent.Executors

const val INVENTORY_SCHEMA = "inventory_v1"

/** Batas atas satu item, mencegah luapan Int. Penambahan melewati batas dipotong ke batas. */
const val MAX_STACK = 999_999_999

@Serializable
internal data class InventoryFile(
    val schema: String = INVENTORY_SCHEMA,
    val items: Map<String, Int> = emptyMap(),
    val legacyMigrated: Boolean = false
)

/**
 * Inventory bersama untuk semua game. Satu-satunya pintu baca/tulis stok.
 *
 * - Semua jumlah adalah Int >= 0 dan tidak pernah negatif.
 * - Seluruh operasi baca-ubah-tulis diserialisasi dengan satu kunci, jadi aman dipanggil dari banyak thread.
 * - Perubahan disimpan berurutan lewat [persistExecutor]; berkas ditulis atomik oleh [InventoryStore].
 * - Data rusak atau schema tak dikenal membuat stok kembali ke nol tanpa crash.
 */
class Inventory(
    private val store: InventoryStore,
    private val catalog: List<ItemDef> = ItemCatalog.items,
    private val persistExecutor: Executor = defaultExecutor()
) {
    private val lock = Any()
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val ids: Set<String> = catalog.map { it.id }.toSet()
    private var legacyMigrated = false
    private var needsRewrite = false

    private val _items = MutableStateFlow(load())

    /** Stok terkini; UI cukup mengumpulkan flow ini. */
    val items: StateFlow<Map<String, Int>> = _items.asStateFlow()

    init {
        if (needsRewrite) commit(_items.value)
    }

    fun getAll(): Map<String, Int> = _items.value

    fun get(item: String): Int = _items.value[item] ?: 0

    fun isKnown(item: String): Boolean = item in ids

    /** Menambah [n] (> 0). Mengembalikan false untuk n tidak valid atau item tak dikenal. */
    fun add(item: String, n: Int): Boolean = synchronized(lock) {
        if (n <= 0 || item !in ids) return@synchronized false
        val current = _items.value[item] ?: 0
        val next = minOf(MAX_STACK.toLong(), current.toLong() + n.toLong()).toInt()
        commit(_items.value + (item to next))
        true
    }

    /** Mengurangi [n] (> 0). Gagal (tanpa perubahan) jika stok kurang. */
    fun spend(item: String, n: Int): Boolean = spendMany(mapOf(item to n))

    /**
     * Membayar beberapa item sekaligus, atomik: berhasil semua atau tidak sama sekali.
     * Gagal jika ada item tak dikenal, jumlah <= 0, atau stok salah satu item kurang.
     * Map kosong dianggap berhasil tanpa perubahan.
     */
    fun spendMany(cost: Map<String, Int>): Boolean = synchronized(lock) {
        if (cost.isEmpty()) return@synchronized true
        val current = _items.value
        for ((item, n) in cost) {
            if (n <= 0 || item !in ids) return@synchronized false
            if ((current[item] ?: 0) < n) return@synchronized false
        }
        val next = current.toMutableMap()
        for ((item, n) in cost) next[item] = next.getValue(item) - n
        commit(next)
        true
    }

    /** Memeriksa kecukupan stok tanpa mengubahnya. */
    fun canAfford(cost: Map<String, Int>): Boolean {
        val current = _items.value
        return cost.all { (item, n) -> n > 0 && item in ids && (current[item] ?: 0) >= n }
    }

    /**
     * Migrasi sekali dari data prototipe ("mb_inv"). Mengembalikan true jika ada data yang dipindahkan.
     * Key tidak ada: dilewati. Key rusak: dihapus tanpa memindahkan apa pun. Setelah itu tidak diulang lagi.
     */
    fun migrateLegacy(source: LegacySource): Boolean = synchronized(lock) {
        val raw = source.get(LegacyMigration.LEGACY_KEY) ?: return@synchronized false
        if (legacyMigrated) {
            source.remove(LegacyMigration.LEGACY_KEY)
            return@synchronized false
        }
        val parsed = LegacyMigration.parse(raw)
        val next = _items.value.toMutableMap()
        var moved = false
        if (parsed != null) {
            for ((item, n) in parsed) {
                if (n > 0 && item in ids) {
                    next[item] = minOf(MAX_STACK.toLong(), (next[item] ?: 0).toLong() + n).toInt()
                    moved = true
                }
            }
        }
        legacyMigrated = true
        commit(next)
        source.remove(LegacyMigration.LEGACY_KEY)
        moved
    }

    private fun zeros(): Map<String, Int> = catalog.associate { it.id to 0 }

    private fun load(): Map<String, Int> {
        val raw = (try {
            store.read()
        } catch (e: Exception) {
            null
        }) ?: return zeros()
        val file = try {
            json.decodeFromString<InventoryFile>(raw)
        } catch (e: Exception) {
            null
        }
        if (file == null || file.schema != INVENTORY_SCHEMA) {
            // Rusak atau schema tak dikenal: mulai dari nol dan timpa dengan berkas yang valid.
            needsRewrite = true
            return zeros()
        }
        legacyMigrated = file.legacyMigrated
        return catalog.associate { it.id to (file.items[it.id] ?: 0).coerceIn(0, MAX_STACK) }
    }

    /** Dipanggil hanya saat memegang [lock]. */
    private fun commit(map: Map<String, Int>) {
        _items.value = map
        val text = json.encodeToString(InventoryFile(INVENTORY_SCHEMA, map, legacyMigrated))
        persistExecutor.execute {
            try {
                store.write(text)
            } catch (e: Exception) {
                // Gagal menulis tidak boleh membuat game crash; stok di memori tetap benar.
            }
        }
    }

    companion object {
        private fun defaultExecutor(): Executor = Executors.newSingleThreadExecutor { r ->
            Thread(r, "inventory-io").apply { isDaemon = true }
        }
    }
}
