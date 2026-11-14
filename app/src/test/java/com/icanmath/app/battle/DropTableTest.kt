package com.icanmath.app.battle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.random.Random

class DropTableTest {
    private val table = DropTable.default

    @Test
    fun cumulativeBoundariesAtLevel3() {
        // 1 kayu 50% | 2 kayu 10% | 1 besi 2%  ->  [0,50) [50,60) [60,62) lalu kosong
        assertEquals(1, table.rollWith(3, 0.0)!!.amount)
        assertEquals("kayu", table.rollWith(3, 49.999)!!.item)
        assertEquals(2, table.rollWith(3, 50.0)!!.amount)
        assertEquals(2, table.rollWith(3, 59.999)!!.amount)
        assertEquals("besi", table.rollWith(3, 60.0)!!.item)
        assertEquals("besi", table.rollWith(3, 61.999)!!.item)
        assertNull(table.rollWith(3, 62.0))
        assertNull(table.rollWith(3, 99.999))
    }

    @Test
    fun cumulativeBoundariesAtLevel10() {
        // 1 orb 1% | 8 kayu 25% | 5 besi 50%  ->  [0,1) [1,26) [26,76) lalu kosong
        assertEquals("orb_tarantula", table.rollWith(10, 0.5)!!.item)
        assertEquals("kayu", table.rollWith(10, 1.0)!!.item)
        assertEquals(8, table.rollWith(10, 25.999)!!.amount)
        assertEquals("besi", table.rollWith(10, 26.0)!!.item)
        assertEquals(5, table.rollWith(10, 75.999)!!.amount)
        assertNull(table.rollWith(10, 76.0))
    }

    @Test
    fun levelsWithoutDropsNeverGiveAnything() {
        val rng = Random(1)
        for (level in listOf(1, 2, 4, 5, 7, 8, 11, 0, -1)) {
            assertTrue(!table.hasDrops(level))
            repeat(1000) { assertNull(table.roll(level, rng)) }
        }
    }

    @Test
    fun distributionMatchesTableOver100kRollsPerLevel() {
        val n = 100_000
        val tolerance = 0.5 // poin persen; kira-kira 3 simpangan baku terburuk
        for (level in listOf(3, 6, 9, 10)) {
            val rng = Random(12_345 + level)
            val entries = table.entries(level)
            val counts = HashMap<DropEntry, Int>()
            var none = 0
            repeat(n) {
                val r = table.roll(level, rng)
                if (r == null) none++ else counts[r] = (counts[r] ?: 0) + 1
            }
            for (e in entries) {
                val pct = (counts[e] ?: 0) * 100.0 / n
                assertTrue(
                    "level $level ${e.amount} ${e.item}: harapan ${e.chancePercent}%, hasil $pct%",
                    abs(pct - e.chancePercent) <= tolerance
                )
            }
            val expectedNone = 100.0 - entries.sumOf { it.chancePercent }
            assertTrue(
                "level $level tanpa hadiah: harapan $expectedNone%, hasil ${none * 100.0 / n}%",
                abs(none * 100.0 / n - expectedNone) <= tolerance
            )
        }
    }

    @Test
    fun oneRollGivesAtMostOneEntry() {
        // Satu angka acak hanya bisa jatuh di satu rentang kumulatif, jadi hasilnya saling eksklusif.
        for (level in listOf(3, 6, 9, 10)) {
            var v = 0.0
            while (v < 100.0) {
                val hits = table.entries(level).count { e -> table.rollWith(level, v) === e }
                assertTrue(hits <= 1)
                v += 0.25
            }
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsTablesOverOneHundredPercent() {
        DropTable(mapOf(1 to listOf(DropEntry("kayu", 1, 70.0), DropEntry("besi", 1, 40.0))))
    }
}
