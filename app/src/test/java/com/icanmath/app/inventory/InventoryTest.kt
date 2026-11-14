package com.icanmath.app.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

class InventoryTest {
    private val direct = Executor { it.run() }
    private fun newInv(store: InventoryStore = MemoryInventoryStore()) = Inventory(store, persistExecutor = direct)

    @Test
    fun startsAtZeroForEveryCatalogItem() {
        assertEquals(mapOf("kayu" to 0, "besi" to 0, "orb_tarantula" to 0), newInv().getAll())
    }

    @Test
    fun addIncreasesStock() {
        val inv = newInv()
        assertTrue(inv.add("kayu", 3))
        assertTrue(inv.add("kayu", 2))
        assertEquals(5, inv.get("kayu"))
        assertEquals(0, inv.get("besi"))
    }

    @Test
    fun addRejectsInvalidValuesWithoutChange() {
        val inv = newInv()
        assertFalse(inv.add("kayu", 0))
        assertFalse(inv.add("kayu", -5))
        assertFalse(inv.add("tidak_ada", 1))
        assertEquals(0, inv.getAll().values.sum())
    }

    @Test
    fun addSaturatesAtMaxStack() {
        val inv = newInv()
        assertTrue(inv.add("kayu", MAX_STACK))
        assertTrue(inv.add("kayu", 5))
        assertEquals(MAX_STACK, inv.get("kayu"))
    }

    @Test
    fun spendReducesAndFailsWhenStockIsShort() {
        val inv = newInv()
        inv.add("kayu", 3)
        assertTrue(inv.spend("kayu", 2))
        assertEquals(1, inv.get("kayu"))
        assertFalse(inv.spend("kayu", 2))
        assertEquals(1, inv.get("kayu"))
    }

    @Test
    fun spendRejectsInvalidValues() {
        val inv = newInv()
        inv.add("kayu", 3)
        assertFalse(inv.spend("kayu", 0))
        assertFalse(inv.spend("kayu", -1))
        assertFalse(inv.spend("tidak_ada", 1))
        assertEquals(3, inv.get("kayu"))
    }

    @Test
    fun spendManySucceedsAtomically() {
        val inv = newInv()
        inv.add("kayu", 7)
        inv.add("besi", 3)
        // Contoh game lain: membayar 5 kayu + 2 besi.
        assertTrue(inv.spendMany(mapOf("kayu" to 5, "besi" to 2)))
        assertEquals(2, inv.get("kayu"))
        assertEquals(1, inv.get("besi"))
    }

    @Test
    fun spendManyNeverSpendsPartially() {
        val inv = newInv()
        inv.add("kayu", 5)
        inv.add("besi", 1)
        assertFalse(inv.spendMany(mapOf("kayu" to 5, "besi" to 2)))
        assertEquals(5, inv.get("kayu"))
        assertEquals(1, inv.get("besi"))
    }

    @Test
    fun spendManyRejectsInvalidEntriesWithoutChange() {
        val inv = newInv()
        inv.add("kayu", 5)
        assertFalse(inv.spendMany(mapOf("kayu" to 1, "besi" to 0)))
        assertFalse(inv.spendMany(mapOf("kayu" to 1, "besi" to -2)))
        assertFalse(inv.spendMany(mapOf("kayu" to 1, "tidak_ada" to 1)))
        assertEquals(5, inv.get("kayu"))
    }

    @Test
    fun spendManyWithEmptyCostSucceedsWithoutChange() {
        val inv = newInv()
        inv.add("kayu", 2)
        assertTrue(inv.spendMany(emptyMap()))
        assertEquals(2, inv.get("kayu"))
    }

    @Test
    fun canAffordDoesNotChangeStock() {
        val inv = newInv()
        inv.add("kayu", 5)
        assertTrue(inv.canAfford(mapOf("kayu" to 5)))
        assertFalse(inv.canAfford(mapOf("kayu" to 6)))
        assertEquals(5, inv.get("kayu"))
    }

    @Test
    fun randomOperationsNeverGoNegative() {
        val inv = newInv()
        val rnd = Random(99)
        val ids = listOf("kayu", "besi", "orb_tarantula", "tidak_ada")
        repeat(5000) {
            val id = ids[rnd.nextInt(ids.size)]
            when (rnd.nextInt(3)) {
                0 -> inv.add(id, rnd.nextInt(-3, 10))
                1 -> inv.spend(id, rnd.nextInt(-3, 10))
                else -> inv.spendMany(mapOf(id to rnd.nextInt(-3, 6), "kayu" to rnd.nextInt(-1, 4)))
            }
            assertTrue(inv.getAll().values.all { it >= 0 })
        }
    }

    @Test
    fun persistsAndReloads() {
        val store = MemoryInventoryStore()
        val a = newInv(store)
        a.add("kayu", 4)
        a.add("orb_tarantula", 1)
        val b = newInv(store)
        assertEquals(4, b.get("kayu"))
        assertEquals(1, b.get("orb_tarantula"))
        assertTrue(store.text!!.contains(INVENTORY_SCHEMA))
    }

    @Test
    fun corruptDataResetsToZeroWithoutCrash() {
        val store = MemoryInventoryStore("{{{ bukan json")
        val inv = newInv(store)
        assertEquals(0, inv.getAll().values.sum())
        // Berkas rusak ditimpa dengan yang valid.
        assertTrue(store.text!!.contains(INVENTORY_SCHEMA))
        assertEquals(0, newInv(store).getAll().values.sum())
    }

    @Test
    fun unknownSchemaResetsToZero() {
        val store = MemoryInventoryStore("""{"schema":"inventory_v99","items":{"kayu":5}}""")
        assertEquals(0, newInv(store).get("kayu"))
    }

    @Test
    fun outOfRangeStoredValuesAreClamped() {
        val store = MemoryInventoryStore("""{"schema":"inventory_v1","items":{"kayu":-5,"besi":2000000000}}""")
        val inv = newInv(store)
        assertEquals(0, inv.get("kayu"))
        assertEquals(MAX_STACK, inv.get("besi"))
    }

    @Test
    fun storeReadFailureDoesNotCrash() {
        val broken = object : InventoryStore {
            override fun read(): String? = throw RuntimeException("disk error")
            override fun write(text: String) {}
        }
        assertEquals(0, newInv(broken).getAll().values.sum())
    }

    @Test
    fun storeWriteFailureDoesNotCrashAndKeepsMemoryCorrect() {
        val broken = object : InventoryStore {
            override fun read(): String? = null
            override fun write(text: String) = throw RuntimeException("disk full")
        }
        val inv = newInv(broken)
        assertTrue(inv.add("kayu", 2))
        assertEquals(2, inv.get("kayu"))
    }

    @Test
    fun observeFlowReflectsChanges() {
        val inv = newInv()
        assertEquals(0, inv.items.value["kayu"])
        inv.add("kayu", 3)
        assertEquals(3, inv.items.value["kayu"])
        inv.spend("kayu", 1)
        assertEquals(2, inv.items.value["kayu"])
    }

    @Test
    fun concurrentAddsAreNotLost() {
        val inv = newInv()
        val threads = List(8) { Thread { repeat(1000) { inv.add("kayu", 1) } } }
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        assertEquals(8000, inv.get("kayu"))
    }

    @Test
    fun concurrentSpendAndAddStayConsistent() {
        val inv = newInv()
        val added = AtomicInteger()
        val spent = AtomicInteger()
        val adders = List(4) { Thread { repeat(2000) { if (inv.add("besi", 1)) added.incrementAndGet() } } }
        val spenders = List(4) { Thread { repeat(2000) { if (inv.spend("besi", 1)) spent.incrementAndGet() } } }
        (adders + spenders).forEach { it.start() }
        (adders + spenders).forEach { it.join() }
        assertTrue(inv.get("besi") >= 0)
        assertEquals(added.get() - spent.get(), inv.get("besi"))
    }
}
