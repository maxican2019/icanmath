package com.icanmath.app.battle

import kotlin.random.Random

/** Satu kemungkinan hadiah. [chancePercent] dalam persen (0-100). */
class DropEntry(val item: String, val amount: Int, val chancePercent: Double)

/**
 * Tabel drop berbasis data. Undian dilakukan SATU KALI per kemenangan: satu angka acak 0 sampai 100,
 * lalu dicek berurutan dengan peluang kumulatif sesuai urutan entri. Hasilnya saling eksklusif.
 * Jika angka tidak masuk entri mana pun, tidak ada hadiah. Level tanpa entri tidak punya drop.
 */
class DropTable(private val byLevel: Map<Int, List<DropEntry>>) {
    init {
        for ((level, entries) in byLevel) {
            require(entries.sumOf { it.chancePercent } <= 100.0 + 1e-9) { "Total peluang level $level melebihi 100%" }
            require(entries.all { it.amount > 0 && it.chancePercent >= 0.0 }) { "Entri drop level $level tidak valid" }
        }
    }

    fun entries(level: Int): List<DropEntry> = byLevel[level].orEmpty()

    fun hasDrops(level: Int): Boolean = entries(level).isNotEmpty()

    /** Undian dengan angka [value] di rentang [0, 100). Dipakai langsung oleh tes untuk hasil deterministik. */
    fun rollWith(level: Int, value: Double): DropEntry? {
        var cumulative = 0.0
        for (entry in entries(level)) {
            cumulative += entry.chancePercent
            if (value < cumulative) return entry
        }
        return null
    }

    /** Undian dengan RNG yang bisa di-inject. */
    fun roll(level: Int, rng: Random): DropEntry? = rollWith(level, rng.nextDouble() * 100.0)

    companion object {
        /** Tabel Math Battle. Mengubah hadiah cukup dengan mengubah data di sini. */
        val default = DropTable(
            mapOf(
                3 to listOf(
                    DropEntry("kayu", 1, 50.0),
                    DropEntry("kayu", 2, 10.0),
                    DropEntry("besi", 1, 2.0)
                ),
                6 to listOf(
                    DropEntry("kayu", 2, 50.0),
                    DropEntry("kayu", 4, 10.0),
                    DropEntry("besi", 2, 2.0)
                ),
                9 to listOf(
                    DropEntry("kayu", 4, 50.0),
                    DropEntry("kayu", 8, 10.0),
                    DropEntry("besi", 4, 5.0)
                ),
                10 to listOf(
                    DropEntry("orb_tarantula", 1, 1.0),
                    DropEntry("kayu", 8, 25.0),
                    DropEntry("besi", 5, 50.0)
                )
            )
        )
    }
}
