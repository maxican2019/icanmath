package com.example.matematikaceria

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

enum class Tier(val label: String, val letter: String) {
    NONE("Terkunci", "-"),
    BRONZE("Bronze", "B"),
    SILVER("Silver", "S"),
    GOLD("Gold", "G")
}

data class Medal(val title: String, val tier: Tier, val detail: String)

object Stats {
    fun gradeLabel(g: Int): String = when {
        g <= 6 -> "SD $g"
        g <= 9 -> "SMP $g"
        else -> "SMA $g"
    }

    /** Daily sukses: latihan harian selesai dengan skor minimal 60% di kelas profil. */
    fun isDailySuccess(s: Session): Boolean =
        s.kind == "DAILY" && s.countsForRank && s.correct * 100 >= s.total * 60

    fun dailyDates(sessions: List<Session>, profileId: String): List<LocalDate> =
        sessions.filter { it.profileId == profileId && isDailySuccess(it) }
            .map { LocalDate.parse(it.date) }
            .distinct()
            .sorted()

    fun bestStreak(dates: List<LocalDate>): Int {
        var best = 0
        var run = 0
        var prev: LocalDate? = null
        for (d in dates) {
            run = if (prev != null && prev.plusDays(1) == d) run + 1 else 1
            if (run > best) best = run
            prev = d
        }
        return best
    }

    fun currentStreak(dates: List<LocalDate>, today: LocalDate): Int {
        val set = dates.toSet()
        var d = if (set.contains(today)) today else today.minusDays(1)
        var n = 0
        while (set.contains(d)) {
            n++
            d = d.minusDays(1)
        }
        return n
    }

    /** Poin ranking lokal: 10 poin per jawaban benar sejak Senin, hanya kelas profil. */
    fun weeklyPoints(sessions: List<Session>, profileId: String, today: LocalDate): Int {
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return sessions
            .filter { it.profileId == profileId && it.countsForRank && !LocalDate.parse(it.date).isBefore(monday) }
            .sumOf { it.correct * 10 }
    }
}

object Medals {
    private fun tierOf(v: Int, b: Int, s: Int, g: Int): Tier = when {
        v >= g -> Tier.GOLD
        v >= s -> Tier.SILVER
        v >= b -> Tier.BRONZE
        else -> Tier.NONE
    }

    private fun countMedal(title: String, v: Int, b: Int, s: Int, g: Int): Medal {
        val t = tierOf(v, b, s, g)
        val next = when (t) {
            Tier.NONE -> b
            Tier.BRONZE -> s
            Tier.SILVER -> g
            Tier.GOLD -> null
        }
        return Medal(title, t, if (next != null) "$v / $next" else "$v (tertinggi)")
    }

    private fun secondsPerQuestion(grade: Int): Int = when {
        grade <= 3 -> 40
        grade <= 6 -> 60
        else -> 90
    }

    /** Medali latihan: waktu, nilai, daily sukses, daily non-stop. */
    fun practice(sessions: List<Session>, profileId: String): List<Medal> {
        val mine = sessions.filter { it.profileId == profileId && it.countsForRank && it.total >= 5 }
        val dailyOk = mine.count { Stats.isDailySuccess(it) }
        val bestStreak = Stats.bestStreak(Stats.dailyDates(sessions, profileId))
        val good = mine.count { it.correct * 100 >= it.total * 80 }
        val perfect = mine.count { it.correct == it.total }
        val scoreTier = when {
            perfect >= 5 -> Tier.GOLD
            perfect >= 1 -> Tier.SILVER
            good >= 1 -> Tier.BRONZE
            else -> Tier.NONE
        }
        // Medali waktu hanya dihitung jika akurasi minimal 80%, supaya tidak asal cepat.
        var timeTier = Tier.NONE
        for (s in mine) {
            if (s.correct * 100 < s.total * 80) continue
            val limit = secondsPerQuestion(s.grade) * s.total
            val t = when {
                s.seconds * 2 <= limit -> Tier.GOLD
                s.seconds * 4 <= limit * 3 -> Tier.SILVER
                s.seconds <= limit -> Tier.BRONZE
                else -> Tier.NONE
            }
            if (t.ordinal > timeTier.ordinal) timeTier = t
        }
        return listOf(
            Medal("Waktu", timeTier, "Cepat dengan akurasi minimal 80%"),
            Medal("Nilai", scoreTier, "80% / 100% / 100% sebanyak 5 kali"),
            countMedal("Daily sukses", dailyOk, 25, 50, 100),
            countMedal("Daily non-stop", bestStreak, 7, 30, 100)
        )
    }

    private val logicGames = listOf(
        "PUZZLE" to "Puzzle penjumlahan",
        "COMPARE" to "Lebih besar atau kecil",
        "GROUP" to "Kelompokkan"
    )

    /** Medali logika terpisah: 10 / 30 / 60 permainan selesai (skor minimal 60%) per game. */
    fun logic(plays: List<LogicPlay>, profileId: String): List<Medal> =
        logicGames.map { (key, title) ->
            val n = plays.count { it.profileId == profileId && it.game == key && it.solved * 100 >= it.total * 60 }
            countMedal(title, n, 10, 30, 60)
        }

    fun newlyEarned(before: List<Medal>, after: List<Medal>): List<Medal> =
        after.filterIndexed { i, m -> m.tier.ordinal > before[i].tier.ordinal }
}
