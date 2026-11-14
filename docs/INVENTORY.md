# Inventory bersama

Modul stok item yang dipakai semua game di APK ini. Game **tidak pernah** membaca atau menulis
penyimpanan langsung; semuanya lewat `Inventory`.

Kode: `app/src/main/java/com/icanmath/app/inventory/`

## Item

Satu-satunya tempat konfigurasi item adalah `ItemCatalog` (`ItemCatalog.kt`). Item awal: `kayu`, `besi`, `orb_tarantula`.
Semua jumlah berupa Int >= 0 (maksimal 999.999.999 per item).

Menambah item baru: tambahkan satu baris `ItemDef(id, nama tampil, nama slot gambar, gaya gambar bawaan)`.
Id tidak boleh diubah setelah dirilis karena dipakai di penyimpanan.

## API

```kotlin
val inventory = InventoryHolder.get(context)   // di Compose: rememberInventory()

inventory.getAll()                 // Map<String, Int>
inventory.get("kayu")              // Int
inventory.add("kayu", 2)           // Boolean; n harus > 0
inventory.spend("besi", 1)         // Boolean; gagal jika stok kurang
inventory.spendMany(mapOf("kayu" to 5, "besi" to 2))  // Boolean; atomik, tidak pernah mengurangi sebagian
inventory.canAfford(mapOf("kayu" to 5))                // Boolean; tanpa mengubah stok
inventory.items                    // StateFlow<Map<String, Int>>; UI otomatis update
```

Aturan: stok tidak pernah negatif. Nilai tidak valid (n <= 0, item tak dikenal) mengembalikan `false` tanpa mengubah apa pun.
`spendMany` dengan map kosong mengembalikan `true` tanpa perubahan.

## Contoh: game lain membayar 5 kayu + 2 besi

```kotlin
@Composable
fun BuyButton() {
    val inventory = rememberInventory()
    Button(onClick = {
        if (inventory.spendMany(mapOf("kayu" to 5, "besi" to 2))) {
            // berhasil: berikan hasil pembelian
        } else {
            // stok kurang: tidak ada yang berkurang
        }
    }) { Text("Bangun rumah") }
}
```

Menampilkan stok: `InventoryBar()` (bar ringkas) atau `InventoryScreen(nav)` (rute `"inventory"`, menu Tas Saya).

## Penyimpanan dan keamanan data

- File `filesDir/inventory_v1.json`, berisi `{"schema":"inventory_v1","items":{...},"legacyMigrated":bool}`.
- Ditulis ke berkas sementara lalu dipindah secara atomik, dan seluruh operasi diserialisasi dengan satu kunci (aman dari banyak thread).
- Data rusak atau schema tak dikenal: stok kembali ke nol tanpa crash, dan berkas ditimpa dengan yang valid.
- Menaikkan schema nantinya: ubah `INVENTORY_SCHEMA` dan tambahkan langkah migrasi di `Inventory.load()`.

## Migrasi dari prototipe

Prototipe menyimpan `localStorage["mb_inv"]` = `{"kayu":n,"besi":n,"orb":n}`. `Inventory.migrateLegacy(source)` memindahkannya
sekali (`orb` menjadi `orb_tarantula`), menggabungkan dengan stok yang ada, lalu menghapus key lama. Key tidak ada: dilewati.
Key rusak: dihapus tanpa memindahkan apa pun. Setelah berhasil, bendera `legacyMigrated` mencegah pengulangan.

Aplikasi native tidak punya `localStorage`. Sumber default (`PrefsLegacySource`) membaca SharedPreferences `mb_legacy`
dengan key `mb_inv`. Jika prototipe berjalan di WebView, salin nilainya ke sana lewat jembatan JavaScript sebelum Inventory dibuka.

## Drop Math Battle

`battle/DropTable.kt` (data, bukan logika game). Undian **satu kali** saat pemain menang: satu angka acak 0 sampai 100,
dicek berurutan dengan peluang kumulatif sesuai urutan tabel; tidak masuk entri mana pun berarti tidak dapat apa-apa.

| Level | Entri (berurutan) |
|---|---|
| 3 | 1 kayu 50% / 2 kayu 10% / 1 besi 2% |
| 6 | 2 kayu 50% / 4 kayu 10% / 2 besi 2% |
| 9 | 4 kayu 50% / 8 kayu 10% / 4 besi 5% |
| 10 | 1 orb_tarantula 1% / 8 kayu 25% / 5 besi 50% |

Level lain tidak ada drop. Setelah menang di level 10, game kembali ke level 1. RNG bisa di-inject:
`DropTable.roll(level, Random(seed))`, atau `rollWith(level, nilai)` untuk hasil pasti di tes.
`BattleRewards.claimVictory(level, inventory)` mengundi, menambah ke inventory, dan mengembalikan pesan untuk layar kemenangan.

## Menjalankan tes

```
gradle testDebugUnitTest
```

Tes ada di `app/src/test/java/com/icanmath/app/inventory/` dan `.../battle/`: add/spend/spendMany (termasuk stok kurang dan nilai tak valid),
data rusak, concurrency, migrasi `mb_inv`, serta simulasi 100.000 undian per level (3, 6, 9, 10) dengan toleransi 0,5 poin persen.
