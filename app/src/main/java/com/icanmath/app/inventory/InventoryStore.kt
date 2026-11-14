package com.icanmath.app.inventory

import java.io.File
import java.io.FileOutputStream
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/** Abstraksi penyimpanan teks. Game tidak pernah menyentuh ini; hanya [Inventory]. */
interface InventoryStore {
    /** Mengembalikan isi tersimpan, atau null jika belum ada. */
    fun read(): String?

    /** Menulis isi baru secara atomik. */
    fun write(text: String)
}

/** Penyimpanan berupa satu berkas, ditulis ke berkas sementara lalu dipindah secara atomik. */
class FileInventoryStore(private val file: File) : InventoryStore {
    override fun read(): String? = if (file.exists()) file.readText() else null

    override fun write(text: String) {
        file.parentFile?.mkdirs()
        val tmp = File(file.path + ".tmp")
        FileOutputStream(tmp).use {
            it.write(text.toByteArray())
            it.fd.sync()
        }
        try {
            Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } catch (e: AtomicMoveNotSupportedException) {
            Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }
}

/** Penyimpanan di memori, untuk tes. */
class MemoryInventoryStore(var text: String? = null) : InventoryStore {
    override fun read(): String? = text
    override fun write(text: String) {
        this.text = text
    }
}
