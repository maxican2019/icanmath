package com.icanmath.app.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.Executor

private class FakeLegacy(var value: String?) : LegacySource {
    var removeCalls = 0
    override fun get(key: String): String? = if (key == "mb_inv") value else null
    override fun remove(key: String) {
        if (key == "mb_inv") {
            value = null
            removeCalls++
        }
    }
}

class MigrationTest {
    private val direct = Executor { it.run() }
    private fun newInv(store: InventoryStore = MemoryInventoryStore()) = Inventory(store, persistExecutor = direct)

    @Test
    fun migratesLegacyValuesAndMapsOrbToOrbTarantula() {
        val inv = newInv()
        val legacy = FakeLegacy("""{"kayu":5,"besi":3,"orb":2}""")
        assertTrue(inv.migrateLegacy(legacy))
        assertEquals(5, inv.get("kayu"))
        assertEquals(3, inv.get("besi"))
        assertEquals(2, inv.get("orb_tarantula"))
        assertNull(legacy.value)
    }

    @Test
    fun runsOnlyOnce() {
        val inv = newInv()
        val legacy = FakeLegacy("""{"kayu":5}""")
        assertTrue(inv.migrateLegacy(legacy))
        assertFalse(inv.migrateLegacy(legacy))
        assertEquals(5, inv.get("kayu"))
    }

    @Test
    fun doesNotRepeatAfterRestartEvenIfKeyReappears() {
        val store = MemoryInventoryStore()
        val first = newInv(store)
        first.migrateLegacy(FakeLegacy("""{"kayu":5}"""))
        val second = newInv(store)
        val legacyAgain = FakeLegacy("""{"kayu":99}""")
        assertFalse(second.migrateLegacy(legacyAgain))
        assertEquals(5, second.get("kayu"))
        assertNull(legacyAgain.value)
    }

    @Test
    fun skipsWhenKeyIsMissing() {
        val inv = newInv()
        val legacy = FakeLegacy(null)
        assertFalse(inv.migrateLegacy(legacy))
        assertEquals(0, inv.getAll().values.sum())
        assertEquals(0, legacy.removeCalls)
    }

    @Test
    fun corruptLegacyDataIsRemovedWithoutChangingStock() {
        val inv = newInv()
        val legacy = FakeLegacy("bukan json")
        assertFalse(inv.migrateLegacy(legacy))
        assertEquals(0, inv.getAll().values.sum())
        assertNull(legacy.value)
    }

    @Test
    fun invalidNumbersAreSkipped() {
        val inv = newInv()
        val legacy = FakeLegacy("""{"kayu":-3,"besi":2.5,"orb":"7"}""")
        assertFalse(inv.migrateLegacy(legacy))
        assertEquals(0, inv.getAll().values.sum())
        assertNull(legacy.value)
    }

    @Test
    fun unknownKeysAreIgnored() {
        val inv = newInv()
        assertTrue(inv.migrateLegacy(FakeLegacy("""{"kayu":2,"emas":9}""")))
        assertEquals(2, inv.get("kayu"))
        assertEquals(mapOf("kayu" to 2, "besi" to 0, "orb_tarantula" to 0), inv.getAll())
    }

    @Test
    fun mergesWithExistingStock() {
        val inv = newInv()
        inv.add("kayu", 4)
        assertTrue(inv.migrateLegacy(FakeLegacy("""{"kayu":5}""")))
        assertEquals(9, inv.get("kayu"))
    }

    @Test
    fun parseAcceptsWholeDecimalsAndRejectsNonObjects() {
        assertEquals(mapOf("kayu" to 5), LegacyMigration.parse("""{"kayu":5.0}"""))
        assertNull(LegacyMigration.parse("[]"))
        assertEquals(emptyMap<String, Int>(), LegacyMigration.parse("{}"))
    }
}
