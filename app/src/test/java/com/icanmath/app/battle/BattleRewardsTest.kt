package com.icanmath.app.battle

import com.icanmath.app.inventory.Inventory
import com.icanmath.app.inventory.MemoryInventoryStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.concurrent.Executor

class BattleRewardsTest {
    private fun inv() = Inventory(MemoryInventoryStore(), persistExecutor = Executor { it.run() })

    @Test
    fun winnerGetsItemAndMessage() {
        val inventory = inv()
        val r = BattleRewards.claimVictory(3, inventory, roll = { 0.0 })
        assertEquals("Hadiah: 1 kayu!", r.message)
        assertEquals(1, inventory.get("kayu"))
    }

    @Test
    fun twoWoodMessage() {
        val inventory = inv()
        val r = BattleRewards.claimVictory(3, inventory, roll = { 55.0 })
        assertEquals("Hadiah: 2 kayu!", r.message)
        assertEquals(2, inventory.get("kayu"))
    }

    @Test
    fun unluckyRollGivesNothing() {
        val inventory = inv()
        val r = BattleRewards.claimVictory(3, inventory, roll = { 99.0 })
        assertEquals("Belum beruntung", r.message)
        assertNull(r.entry)
        assertEquals(0, inventory.getAll().values.sum())
    }

    @Test
    fun orbDropAtLevel10() {
        val inventory = inv()
        val r = BattleRewards.claimVictory(10, inventory, roll = { 0.5 })
        assertEquals("Hadiah: 1 orb tarantula!", r.message)
        assertEquals(1, inventory.get("orb_tarantula"))
    }

    @Test
    fun rollsExactlyOncePerVictory() {
        var calls = 0
        BattleRewards.claimVictory(6, inv(), roll = { calls++; 10.0 })
        assertEquals(1, calls)
    }

    @Test
    fun levelWithoutTableDoesNotRollOrGiveAnything() {
        var calls = 0
        val inventory = inv()
        BattleRewards.claimVictory(5, inventory, roll = { calls++; 0.0 })
        assertEquals(0, calls)
        assertEquals(0, inventory.getAll().values.sum())
    }
}
