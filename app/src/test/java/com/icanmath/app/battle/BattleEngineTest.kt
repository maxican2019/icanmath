package com.icanmath.app.battle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class BattleEngineTest {
    private val swordWood = BattleRules.swordTiers[0]
    private val swordRainbow = BattleRules.swordTiers[3]
    private val armorLeather = BattleRules.armorTiers[0]

    @Test
    fun loopsBackToLevelOneAfterLevelTen() {
        assertEquals(2, BattleRules.nextLevelAfterWin(1))
        assertEquals(10, BattleRules.nextLevelAfterWin(9))
        assertEquals(1, BattleRules.nextLevelAfterWin(10))
    }

    @Test
    fun correctAttackHurtsMonsterAndMonsterCountersAttacks() {
        val s = BattleEngine.start(1)
        val (n, log) = BattleEngine.resolveTurn(s, ActionType.SWORD, swordWood, true)
        assertEquals(s.monsterHp - 1, n.monsterHp)
        assertEquals(s.heroHp - s.level.monsterAttack, n.heroHp)
        assertTrue(log.monsterAttacked)
        assertNull(n.outcome)
    }

    @Test
    fun killingBlowWinsWithoutCounterAttack() {
        val s = BattleEngine.start(1)
        val (n, log) = BattleEngine.resolveTurn(s, ActionType.SWORD, swordRainbow, true)
        assertEquals(Outcome.WIN, n.outcome)
        assertEquals(s.heroHp, n.heroHp)
        assertEquals(false, log.monsterAttacked)
    }

    @Test
    fun wrongAnswerDealsNoDamageButMonsterStillAttacks() {
        val s = BattleEngine.start(1)
        val (n, _) = BattleEngine.resolveTurn(s, ActionType.SWORD, swordRainbow, false)
        assertEquals(s.monsterHp, n.monsterHp)
        assertEquals(s.heroHp - s.level.monsterAttack, n.heroHp)
    }

    @Test
    fun armorShieldAbsorbsMonsterDamage() {
        val s = BattleEngine.start(1)
        val (n, log) = BattleEngine.resolveTurn(s, ActionType.ARMOR, armorLeather, true)
        assertEquals(s.level.monsterAttack, log.absorbed)
        assertEquals(armorLeather.value - s.level.monsterAttack, n.shield)
        assertEquals(s.heroHp, n.heroHp)
    }

    @Test
    fun shieldIsCapped() {
        var s = BattleEngine.start(1)
        val diamond = BattleRules.armorTiers.last()
        repeat(5) { s = BattleEngine.resolveTurn(s, ActionType.ARMOR, diamond, true).first }
        assertTrue(s.shield <= BattleRules.MAX_SHIELD)
    }

    @Test
    fun heroLosesWhenHpRunsOutAndStateThenFreezes() {
        var s = BattleEngine.start(10)
        var guard = 0
        while (s.outcome == null && guard++ < 100) {
            s = BattleEngine.resolveTurn(s, ActionType.SWORD, swordWood, false).first
        }
        assertEquals(Outcome.LOSE, s.outcome)
        assertEquals(0, s.heroHp)
        val after = BattleEngine.resolveTurn(s, ActionType.SWORD, swordRainbow, true).first
        assertEquals(s.monsterHp, after.monsterHp)
    }

    @Test
    fun tenLevelsWithGrowingDifficulty() {
        assertEquals(10, BattleLevels.all.size)
        assertEquals((1..10).toList(), BattleLevels.all.map { it.level })
        assertTrue(BattleLevels.all.zipWithNext().all { (a, b) -> b.monsterHp > a.monsterHp })
    }

    @Test
    fun everyTierProducesARandomQuestionWithExplanation() {
        val rng = Random(5)
        for (action in ActionType.values()) {
            for (tier in BattleRules.tiers(action)) {
                for (grade in listOf(1, 7, 12)) {
                    val q = BattleQuestions.generate(grade, tier, rng)
                    assertEquals(4, q.options.size)
                    assertTrue(q.explanation.isNotBlank())
                }
            }
        }
    }
}
