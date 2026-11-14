package com.icanmath.app.inventory

/** Gaya gambar bawaan (digambar kode) jika berkas PNG slot belum ada. */
enum class ItemArt { LOG, IRON, ORB }

/** Definisi satu item. [slot] adalah nama berkas gambar di res/drawable-nodpi (lihat docs/IMAGE_SLOTS.md). */
class ItemDef(
    val id: String,
    val displayName: String,
    val slot: String,
    val art: ItemArt
)

/**
 * SATU-SATUNYA tempat konfigurasi item. Untuk menambah item baru, tambahkan satu baris di sini.
 * Id dipakai di penyimpanan, jadi jangan diubah setelah dirilis.
 */
object ItemCatalog {
    val items: List<ItemDef> = listOf(
        ItemDef("kayu", "Kayu", "img_item_kayu", ItemArt.LOG),
        ItemDef("besi", "Besi", "img_item_besi", ItemArt.IRON),
        ItemDef("orb_tarantula", "Orb Tarantula", "img_item_orb_tarantula", ItemArt.ORB)
    )

    private val byId = items.associateBy { it.id }

    fun find(id: String): ItemDef? = byId[id]
}
