package com.icanmath.app.battle

import com.icanmath.app.Question
import com.icanmath.app.QuestionGenerator
import kotlin.random.Random

enum class ActionType { ARROW, SWORD, ARMOR }

/** Tingkat bahan. [value] adalah damage (panah, pedang) atau DEF (armor). [difficulty] menaikkan tingkat soal. */
class Tier(val id: String, val label: String, val value: Int, val difficulty: Int)

/** Aturan nilai dari catatan proyek: panah, pedang, dan armor beserta tingkat bahannya. */
object BattleRules {
    const val LEVELS = 10
    const val HERO_HP = 30
    const val MAX_SHIELD = 40

    val arrowTiers = listOf(
        Tier("kayu", "Kayu", 1, 0), Tier("besi", "Besi", 3, 1),
        Tier("emas", "Emas", 5, 2), Tier("pelangi", "Pelangi", 10, 3)
    )
    val swordTiers = listOf(
        Tier("kayu", "Kayu", 1, 0), Tier("besi", "Besi", 6, 1),
        Tier("emas", "Emas", 10, 2), Tier("pelangi", "Pelangi", 20, 3)
    )
    val armorTiers = listOf(
        Tier("kulit", "Kulit", 5, 0), Tier("kayu", "Kayu", 7, 1), Tier("besi", "Besi", 14, 2),
        Tier("emas", "Emas", 16, 3), Tier("berlian", "Berlian", 20, 4)
    )

    fun tiers(action: ActionType): List<Tier> = when (action) {
        ActionType.ARROW -> arrowTiers
        ActionType.SWORD -> swordTiers
        ActionType.ARMOR -> armorTiers
    }

    /** Setelah menang di level 10 game kembali ke level 1 (loop farming). */
    fun nextLevelAfterWin(level: Int): Int = if (level >= LEVELS) 1 else level + 1
}

/** Konfigurasi satu level. HP monster hanya dipakai di logika, tidak pernah ditampilkan. */
class LevelConfig(
    val level: Int,
    val monsterName: String,
    val monsterHp: Int,
    val monsterAttack: Int,
    val monsterColor: Long
)

object BattleLevels {
    // Angka diseimbangkan lewat simulasi: level 1-6 mudah, level 10 sulit tetapi bisa dimenangkan.
    val all: List<LevelConfig> = listOf(
        LevelConfig(1, "Laba-laba kecil", 6, 2, 0xFF7E57C2),
        LevelConfig(2, "Laba-laba kebun", 10, 2, 0xFF26A69A),
        LevelConfig(3, "Laba-laba hutan", 14, 2, 0xFF66BB6A),
        LevelConfig(4, "Laba-laba gua", 18, 2, 0xFF5C6BC0),
        LevelConfig(5, "Laba-laba rawa", 24, 3, 0xFF8D6E63),
        LevelConfig(6, "Laba-laba api", 30, 3, 0xFFEF5350),
        LevelConfig(7, "Laba-laba es", 38, 3, 0xFF42A5F5),
        LevelConfig(8, "Laba-laba petir", 46, 4, 0xFFFFA726),
        LevelConfig(9, "Laba-laba bayangan", 56, 4, 0xFF8E24AA),
        LevelConfig(10, "Tarantula raja", 70, 5, 0xFFD81B60)
    )

    fun of(level: Int): LevelConfig = all[(level - 1).coerceIn(0, all.size - 1)]
}

enum class Outcome { WIN, LOSE }

class BattleState(
    val level: LevelConfig,
    val heroHp: Int,
    val shield: Int,
    val monsterHp: Int,
    val turn: Int,
    val outcome: Outcome?
)

/** Ringkasan satu giliran, untuk teks dan animasi. */
class TurnLog(
    val damageDealt: Int,
    val shieldGained: Int,
    val monsterAttacked: Boolean,
    val damageTaken: Int,
    val absorbed: Int
)

/**
 * Mesin giliran murni (tanpa UI), jadi mudah diuji.
 * Pahlawan beraksi (jawaban benar: serangan atau armor), lalu monster membalas jika masih hidup.
 * DEF menyerap damage monster sampai habis.
 */
object BattleEngine {
    fun start(level: Int): BattleState {
        val cfg = BattleLevels.of(level)
        return BattleState(cfg, BattleRules.HERO_HP, 0, cfg.monsterHp, 1, null)
    }

    fun resolveTurn(state: BattleState, action: ActionType, tier: Tier, correct: Boolean): Pair<BattleState, TurnLog> {
        if (state.outcome != null) return state to TurnLog(0, 0, false, 0, 0)
        var monsterHp = state.monsterHp
        var shield = state.shield
        var dealt = 0
        var gained = 0
        if (correct) {
            when (action) {
                ActionType.ARROW, ActionType.SWORD -> {
                    dealt = tier.value
                    monsterHp -= dealt
                }
                ActionType.ARMOR -> {
                    val next = minOf(BattleRules.MAX_SHIELD, shield + tier.value)
                    gained = next - shield
                    shield = next
                }
            }
        }
        if (monsterHp <= 0) {
            val won = BattleState(state.level, state.heroHp, shield, 0, state.turn, Outcome.WIN)
            return won to TurnLog(dealt, gained, false, 0, 0)
        }
        val attack = state.level.monsterAttack
        val absorbed = minOf(shield, attack)
        shield -= absorbed
        val taken = attack - absorbed
        val heroHp = state.heroHp - taken
        val outcome = if (heroHp <= 0) Outcome.LOSE else null
        val next = BattleState(state.level, maxOf(0, heroHp), shield, monsterHp, state.turn + 1, outcome)
        return next to TurnLog(dealt, gained, true, taken, absorbed)
    }
}

/** Soal selalu acak. Tingkat bahan yang lebih tinggi memakai soal satu tingkat kelas lebih sulit per tingkatnya. */
object BattleQuestions {
    fun generate(grade: Int, tier: Tier, rng: Random): Question =
        QuestionGenerator.generate((grade + tier.difficulty).coerceIn(1, 12), 1, rng).first()
}
