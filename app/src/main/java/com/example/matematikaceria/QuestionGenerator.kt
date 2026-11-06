package com.example.matematikaceria

import kotlin.random.Random

/**
 * Generator soal rule-based per kelas (1-12). Jawaban selalu dihitung oleh kode,
 * jadi kunci pasti benar. Topik di sini garis besar; sesuaikan dengan kurikulum yang dipakai.
 */
object QuestionGenerator {

    fun generate(grade: Int, count: Int, r: Random): List<Question> {
        if (count <= 0) return emptyList()
        val seen = HashSet<String>()
        val out = ArrayList<Question>()
        var guard = 0
        while (out.size < count && guard++ < count * 40) {
            val q = pick(grade, r)
            if (seen.add(q.text)) out.add(q.copy(grade = grade))
        }
        return out
    }

    /** Membuat soal pilihan ganda dengan 4 opsi unik yang salah satunya adalah jawaban. */
    fun numeric(
        text: String,
        answer: Int,
        r: Random,
        allowNegative: Boolean = false,
        explanation: String = ""
    ): Question {
        val opts = LinkedHashSet<Int>()
        opts.add(answer)
        val deltas = listOf(-10, -3, -2, -1, 1, 2, 3, 10).shuffled(r)
        for (d in deltas) {
            val v = answer + d
            if (allowNegative || v >= 0) opts.add(v)
            if (opts.size == 4) break
        }
        var extra = 4
        while (opts.size < 4) {
            opts.add(answer + extra)
            extra++
        }
        val list = opts.toList().shuffled(r)
        return Question(0, text, list.map { it.toString() }, list.indexOf(answer), explanation)
    }

    private fun pick(g: Int, r: Random): Question = when (g) {
        1 -> if (r.nextBoolean()) add(r, 1, 10) else sub(r, 20)
        2 -> if (r.nextBoolean()) add(r, 10, 60) else sub(r, 100)
        3 -> when (r.nextInt(3)) {
            0 -> add(r, 100, 600)
            1 -> sub(r, 1000)
            else -> mul(r, 2, 10, 2, 10)
        }
        4 -> when (r.nextInt(3)) {
            0 -> mul(r, 11, 30, 2, 9)
            1 -> div(r, 9, 12)
            else -> add(r, 1000, 5000)
        }
        5 -> when (r.nextInt(3)) {
            0 -> percent(r)
            1 -> fpb(r)
            else -> kpk(r)
        }
        6 -> when (r.nextInt(3)) {
            0 -> rectArea(r)
            1 -> average(r)
            else -> cubeVolume(r)
        }
        7 -> if (r.nextBoolean()) intOps(r) else linear(r, 1)
        8 -> if (r.nextBoolean()) linear(r, 2) else pythagoras(r)
        9 -> if (r.nextBoolean()) power(r) else squareRoot(r)
        10 -> if (r.nextBoolean()) quadraticRoot(r) else logarithm(r)
        11 -> if (r.nextBoolean()) arithTerm(r) else arithSum(r)
        else -> derivative(r)
    }

    private fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

    private fun pow(b: Int, e: Int): Int {
        var res = 1
        repeat(e) { res *= b }
        return res
    }

    private fun add(r: Random, lo: Int, hi: Int): Question {
        val a = r.nextInt(lo, hi + 1)
        val b = r.nextInt(lo, hi + 1)
        return numeric("$a + $b = ?", a + b, r)
    }

    private fun sub(r: Random, hi: Int): Question {
        val a = r.nextInt(5, hi + 1)
        val b = r.nextInt(1, a)
        return numeric("$a − $b = ?", a - b, r)
    }

    private fun mul(r: Random, a1: Int, a2: Int, b1: Int, b2: Int): Question {
        val a = r.nextInt(a1, a2 + 1)
        val b = r.nextInt(b1, b2 + 1)
        return numeric("$a × $b = ?", a * b, r)
    }

    private fun div(r: Random, maxB: Int, maxQ: Int): Question {
        val b = r.nextInt(2, maxB + 1)
        val q = r.nextInt(2, maxQ + 1)
        return numeric("${b * q} ÷ $b = ?", q, r)
    }

    private fun percent(r: Random): Question {
        val p = listOf(10, 20, 25, 50).random(r)
        val n = listOf(40, 80, 120, 200, 400).random(r)
        return numeric("$p% dari $n = ?", n * p / 100, r)
    }

    private fun fpb(r: Random): Question {
        val g = r.nextInt(2, 10)
        val x = r.nextInt(2, 7)
        var y = r.nextInt(2, 7)
        while (gcd(x, y) != 1) y = r.nextInt(2, 7)
        return numeric("FPB dari ${g * x} dan ${g * y} = ?", g, r)
    }

    private fun kpk(r: Random): Question {
        val x = r.nextInt(2, 10)
        var y = r.nextInt(2, 10)
        while (y == x) y = r.nextInt(2, 10)
        return numeric("KPK dari $x dan $y = ?", x * y / gcd(x, y), r)
    }

    private fun rectArea(r: Random): Question {
        val p = r.nextInt(3, 16)
        val l = r.nextInt(2, 13)
        return numeric("Luas persegi panjang dengan panjang $p cm dan lebar $l cm adalah ... cm²", p * l, r)
    }

    private fun average(r: Random): Question {
        val a = r.nextInt(10, 31)
        val b = r.nextInt(10, 31)
        val c = r.nextInt(10, 31)
        var d = r.nextInt(10, 31)
        d += (4 - (a + b + c + d) % 4) % 4
        return numeric("Rata-rata dari $a, $b, $c, dan $d adalah ...", (a + b + c + d) / 4, r)
    }

    private fun cubeVolume(r: Random): Question {
        val s = r.nextInt(2, 10)
        return numeric("Volume kubus dengan rusuk $s cm adalah ... cm³", s * s * s, r)
    }

    private fun intOps(r: Random): Question {
        val a = r.nextInt(-15, 16)
        val b = r.nextInt(-15, 16)
        val bs = if (b < 0) "($b)" else "$b"
        return if (r.nextBoolean()) {
            numeric("$a + $bs = ?", a + b, r, allowNegative = true)
        } else {
            numeric("$a − $bs = ?", a - b, r, allowNegative = true)
        }
    }

    private fun linear(r: Random, level: Int): Question {
        return if (level == 1) {
            val x = r.nextInt(1, 16)
            if (r.nextBoolean()) {
                val b = r.nextInt(1, 21)
                numeric("x + $b = ${x + b}, maka x = ?", x, r)
            } else {
                val a = r.nextInt(2, 10)
                numeric("${a}x = ${a * x}, maka x = ?", x, r)
            }
        } else {
            val x = r.nextInt(1, 13)
            val a = r.nextInt(2, 10)
            val b = r.nextInt(1, 21)
            numeric("${a}x + $b = ${a * x + b}, maka x = ?", x, r)
        }
    }

    private fun pythagoras(r: Random): Question {
        val t = listOf(
            Triple(3, 4, 5), Triple(5, 12, 13), Triple(8, 15, 17),
            Triple(7, 24, 25), Triple(6, 8, 10), Triple(9, 12, 15)
        ).random(r)
        return numeric(
            "Segitiga siku-siku dengan sisi tegak ${t.first} cm dan ${t.second} cm. Panjang sisi miring = ... cm",
            t.third, r
        )
    }

    private fun power(r: Random): Question {
        val b = r.nextInt(2, 6)
        val e = r.nextInt(2, 5)
        return numeric("${b}^${e} = ?", pow(b, e), r)
    }

    private fun squareRoot(r: Random): Question {
        val n = r.nextInt(5, 21)
        return numeric("√${n * n} = ?", n, r)
    }

    private fun quadraticRoot(r: Random): Question {
        val p = r.nextInt(1, 10)
        var q = r.nextInt(1, 10)
        while (q == p) q = r.nextInt(1, 10)
        return numeric("Akar terbesar dari x² − ${p + q}x + ${p * q} = 0 adalah ...", maxOf(p, q), r)
    }

    private fun logarithm(r: Random): Question {
        val b = listOf(2, 3, 5).random(r)
        val k = r.nextInt(1, 5)
        return numeric("${b}log ${pow(b, k)} = ?", k, r)
    }

    private fun arithTerm(r: Random): Question {
        val a = r.nextInt(1, 11)
        val d = r.nextInt(2, 7)
        val n = r.nextInt(8, 21)
        return numeric(
            "Barisan aritmetika: $a, ${a + d}, ${a + 2 * d}, ... Suku ke-$n adalah ...",
            a + (n - 1) * d, r
        )
    }

    private fun arithSum(r: Random): Question {
        val a = r.nextInt(1, 11)
        val d = r.nextInt(2, 7)
        val n = r.nextInt(5, 13)
        return numeric(
            "Jumlah $n suku pertama barisan $a, ${a + d}, ${a + 2 * d}, ... adalah ...",
            n * (2 * a + (n - 1) * d) / 2, r
        )
    }

    private fun derivative(r: Random): Question {
        val a = r.nextInt(1, 6)
        val b = r.nextInt(1, 10)
        val c = r.nextInt(1, 6)
        return numeric("Jika f(x) = ${a}x² + ${b}x, maka f'($c) = ...", 2 * a * c + b, r)
    }
}
