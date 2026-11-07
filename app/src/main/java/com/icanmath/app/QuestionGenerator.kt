package com.icanmath.app

import kotlin.random.Random

/**
 * Generator soal rule-based per kelas (1-12). Jawaban dan penjelasan dihitung oleh kode,
 * jadi kunci pasti benar dan setiap soal punya "cara menghitung" serta istilah singkat (tip).
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
        explanation: String = "",
        tip: String = ""
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
        return Question(0, text, list.map { it.toString() }, list.indexOf(answer), explanation, tip)
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

    private fun p(n: Int): String = if (n < 0) "($n)" else "$n"

    private fun factors(n: Int): List<Int> = (1..n).filter { n % it == 0 }

    private fun add(r: Random, lo: Int, hi: Int): Question {
        val a = r.nextInt(lo, hi + 1)
        val b = r.nextInt(lo, hi + 1)
        val s = a + b
        val expl = if (hi <= 10) {
            "Mulai dari $a, lalu hitung maju $b kali sampai ketemu $s.\n$a + $b = $s."
        } else {
            "Jumlahkan mulai dari satuan, puluhan, lalu tempat berikutnya. Jika hasil sebuah tempat 10 atau lebih, simpan 1 ke tempat di sebelah kirinya.\n$a + $b = $s."
        }
        return numeric("$a + $b = ?", s, r, explanation = expl, tip = "Menjumlah artinya menggabungkan dua kelompok.")
    }

    private fun sub(r: Random, hi: Int): Question {
        val a = r.nextInt(5, hi + 1)
        val b = r.nextInt(1, a)
        val d = a - b
        return numeric(
            "$a − $b = ?", d, r,
            explanation = "Kurangkan $b dari $a.\n$a − $b = $d.\nCek dengan menjumlah balik: $d + $b = $a.",
            tip = "Mengurangi artinya mengambil sebagian dari sekelompok benda. Yang tersisa adalah hasilnya."
        )
    }

    private fun mul(r: Random, a1: Int, a2: Int, b1: Int, b2: Int): Question {
        val a = r.nextInt(a1, a2 + 1)
        val b = r.nextInt(b1, b2 + 1)
        return numeric(
            "$a × $b = ?", a * b, r,
            explanation = "$a × $b artinya $a dijumlahkan sebanyak $b kali.\n${List(b) { a }.joinToString(" + ")} = ${a * b}.",
            tip = "Perkalian adalah penjumlahan berulang."
        )
    }

    private fun div(r: Random, maxB: Int, maxQ: Int): Question {
        val b = r.nextInt(2, maxB + 1)
        val q = r.nextInt(2, maxQ + 1)
        return numeric(
            "${b * q} ÷ $b = ?", q, r,
            explanation = "Pembagian adalah kebalikan perkalian. Cari bilangan yang jika dikali $b hasilnya ${b * q}.\n$q × $b = ${b * q}, jadi ${b * q} ÷ $b = $q.",
            tip = "Membagi artinya membagi rata sebuah jumlah ke beberapa kelompok."
        )
    }

    private fun percent(r: Random): Question {
        val pc = listOf(10, 20, 25, 50).random(r)
        val n = listOf(40, 80, 120, 200, 400).random(r)
        val res = n * pc / 100
        return numeric(
            "$pc% dari $n = ?", res, r,
            explanation = "$pc% berarti $pc per 100.\n$pc% dari $n = $pc/100 × $n = $res.",
            tip = "Persen (%) berarti per seratus. 50% = setengah, 25% = seperempat, 10% = sepersepuluh."
        )
    }

    private fun fpb(r: Random): Question {
        val g = r.nextInt(2, 10)
        val x = r.nextInt(2, 7)
        var y = r.nextInt(2, 7)
        while (gcd(x, y) != 1) y = r.nextInt(2, 7)
        val a = g * x
        val b = g * y
        return numeric(
            "FPB dari $a dan $b = ?", g, r,
            explanation = "Faktor dari $a: ${factors(a).joinToString(", ")}.\nFaktor dari $b: ${factors(b).joinToString(", ")}.\nFaktor yang sama dan paling besar adalah $g, jadi FPB = $g.",
            tip = "FPB = Faktor Persekutuan Terbesar, yaitu bilangan terbesar yang bisa membagi habis kedua bilangan."
        )
    }

    private fun kpk(r: Random): Question {
        val x = r.nextInt(2, 10)
        var y = r.nextInt(2, 10)
        while (y == x) y = r.nextInt(2, 10)
        val l = x * y / gcd(x, y)
        val mx = (1..l / x).map { it * x }
        val my = (1..l / y).map { it * y }
        return numeric(
            "KPK dari $x dan $y = ?", l, r,
            explanation = "Kelipatan $x: ${mx.joinToString(", ")}.\nKelipatan $y: ${my.joinToString(", ")}.\nKelipatan yang sama untuk pertama kali adalah $l, jadi KPK = $l.",
            tip = "KPK = Kelipatan Persekutuan Terkecil, yaitu bilangan terkecil yang merupakan kelipatan kedua bilangan."
        )
    }

    private fun rectArea(r: Random): Question {
        val pj = r.nextInt(3, 16)
        val l = r.nextInt(2, 13)
        return numeric(
            "Luas persegi panjang dengan panjang $pj cm dan lebar $l cm adalah ... cm²", pj * l, r,
            explanation = "Luas persegi panjang = panjang × lebar.\n$pj × $l = ${pj * l} cm².",
            tip = "Luas adalah banyaknya persegi satuan yang menutupi permukaan sebuah bangun datar."
        )
    }

    private fun average(r: Random): Question {
        val a = r.nextInt(10, 31)
        val b = r.nextInt(10, 31)
        val c = r.nextInt(10, 31)
        var d = r.nextInt(10, 31)
        d += (4 - (a + b + c + d) % 4) % 4
        val sum = a + b + c + d
        return numeric(
            "Rata-rata dari $a, $b, $c, dan $d adalah ...", sum / 4, r,
            explanation = "Rata-rata = jumlah semua data ÷ banyak data.\nJumlah = $a + $b + $c + $d = $sum.\n$sum ÷ 4 = ${sum / 4}.",
            tip = "Rata-rata disebut juga mean: nilai yang dibagi rata ke semua data."
        )
    }

    private fun cubeVolume(r: Random): Question {
        val s = r.nextInt(2, 10)
        return numeric(
            "Volume kubus dengan rusuk $s cm adalah ... cm³", s * s * s, r,
            explanation = "Volume kubus = rusuk × rusuk × rusuk.\n$s × $s × $s = ${s * s * s} cm³.",
            tip = "Volume adalah banyaknya ruang yang ditempati sebuah bangun ruang."
        )
    }

    private fun intOps(r: Random): Question {
        val a = r.nextInt(-15, 16)
        val b = r.nextInt(-15, 16)
        val tip = "Pada garis bilangan, makin ke kiri nilainya makin kecil. Contoh: −5 lebih kecil dari −2."
        return if (r.nextBoolean()) {
            val expl = if ((a >= 0) == (b >= 0)) {
                "Tanda sama: jumlahkan nilainya, tanda tetap sama.\n$a + ${p(b)} = ${a + b}."
            } else {
                "Tanda berbeda: kurangkan nilai yang besar dengan yang kecil, lalu pakai tanda bilangan yang nilainya lebih besar.\n$a + ${p(b)} = ${a + b}."
            }
            numeric("$a + ${p(b)} = ?", a + b, r, allowNegative = true, explanation = expl, tip = tip)
        } else {
            numeric(
                "$a − ${p(b)} = ?", a - b, r, allowNegative = true,
                explanation = "Mengurangi sama dengan menambah lawannya. Lawan dari $b adalah ${-b}.\n$a − ${p(b)} = $a + ${p(-b)} = ${a - b}.",
                tip = tip
            )
        }
    }

    private fun linear(r: Random, level: Int): Question {
        val tip = "Persamaan seperti timbangan: apa pun yang dilakukan di satu ruas harus dilakukan juga di ruas yang lain."
        return if (level == 1) {
            val x = r.nextInt(1, 16)
            if (r.nextBoolean()) {
                val b = r.nextInt(1, 21)
                numeric(
                    "x + $b = ${x + b}, maka x = ?", x, r,
                    explanation = "Kurangi kedua ruas dengan $b.\nx = ${x + b} − $b = $x.",
                    tip = tip
                )
            } else {
                val a = r.nextInt(2, 10)
                numeric(
                    "${a}x = ${a * x}, maka x = ?", x, r,
                    explanation = "${a}x artinya $a × x. Bagi kedua ruas dengan $a.\nx = ${a * x} ÷ $a = $x.",
                    tip = tip
                )
            }
        } else {
            val x = r.nextInt(1, 13)
            val a = r.nextInt(2, 10)
            val b = r.nextInt(1, 21)
            val c = a * x + b
            numeric(
                "${a}x + $b = $c, maka x = ?", x, r,
                explanation = "Langkah 1: kurangi kedua ruas dengan $b.\n${a}x = ${c - b}.\nLangkah 2: bagi kedua ruas dengan $a.\nx = ${c - b} ÷ $a = $x.",
                tip = tip
            )
        }
    }

    private fun pythagoras(r: Random): Question {
        val t = listOf(
            Triple(3, 4, 5), Triple(5, 12, 13), Triple(8, 15, 17),
            Triple(7, 24, 25), Triple(6, 8, 10), Triple(9, 12, 15)
        ).random(r)
        val a = t.first
        val b = t.second
        val c = t.third
        return numeric(
            "Segitiga siku-siku dengan sisi tegak $a cm dan $b cm. Panjang sisi miring = ... cm", c, r,
            explanation = "Teorema Pythagoras: sisi miring² = sisi tegak² + sisi alas².\n$a² + $b² = ${a * a} + ${b * b} = ${a * a + b * b}.\nSisi miring = √${a * a + b * b} = $c cm.",
            tip = "Sisi miring (hipotenusa) adalah sisi terpanjang, letaknya di seberang sudut siku-siku."
        )
    }

    private fun power(r: Random): Question {
        val b = r.nextInt(2, 6)
        val e = r.nextInt(2, 5)
        val res = pow(b, e)
        return numeric(
            "${b}^${e} = ?", res, r,
            explanation = "${b}^${e} artinya $b dikalikan sendiri sebanyak $e kali.\n${List(e) { b }.joinToString(" × ")} = $res.",
            tip = "Pada ${b}^${e}, $b disebut bilangan pokok dan $e disebut pangkat (eksponen)."
        )
    }

    private fun squareRoot(r: Random): Question {
        val n = r.nextInt(5, 21)
        return numeric(
            "√${n * n} = ?", n, r,
            explanation = "Cari bilangan yang jika dikuadratkan hasilnya ${n * n}.\n$n × $n = ${n * n}, jadi √${n * n} = $n.",
            tip = "Akar kuadrat (√) adalah kebalikan dari pangkat dua."
        )
    }

    private fun quadraticRoot(r: Random): Question {
        val a = r.nextInt(1, 10)
        var b = r.nextInt(1, 10)
        while (b == a) b = r.nextInt(1, 10)
        return numeric(
            "Akar terbesar dari x² − ${a + b}x + ${a * b} = 0 adalah ...", maxOf(a, b), r,
            explanation = "Cari dua bilangan yang jumlahnya ${a + b} dan hasil kalinya ${a * b}: yaitu $a dan $b.\nFaktorkan: (x − $a)(x − $b) = 0, sehingga x = $a atau x = $b.\nAkar terbesar = ${maxOf(a, b)}.",
            tip = "Akar persamaan kuadrat adalah nilai x yang membuat persamaannya bernilai benar."
        )
    }

    private fun logarithm(r: Random): Question {
        val b = listOf(2, 3, 5).random(r)
        val k = r.nextInt(1, 5)
        return numeric(
            "${b}log ${pow(b, k)} = ?", k, r,
            explanation = "${b}log ${pow(b, k)} bertanya: $b dipangkatkan berapa agar hasilnya ${pow(b, k)}?\nKarena ${b}^${k} = ${pow(b, k)}, maka jawabannya $k.",
            tip = "Logaritma adalah kebalikan dari pangkat."
        )
    }

    private fun arithTerm(r: Random): Question {
        val a = r.nextInt(1, 11)
        val d = r.nextInt(2, 7)
        val n = r.nextInt(8, 21)
        return numeric(
            "Barisan aritmetika: $a, ${a + d}, ${a + 2 * d}, ... Suku ke-$n adalah ...", a + (n - 1) * d, r,
            explanation = "Beda (selisih antar suku) = ${a + d} − $a = $d.\nRumus: Un = a + (n − 1) × b.\nU$n = $a + ${n - 1} × $d = ${a + (n - 1) * d}.",
            tip = "Barisan aritmetika adalah barisan yang selisih antar suku berurutan selalu sama (disebut beda)."
        )
    }

    private fun arithSum(r: Random): Question {
        val a = r.nextInt(1, 11)
        val d = r.nextInt(2, 7)
        val n = r.nextInt(5, 13)
        val inner = 2 * a + (n - 1) * d
        return numeric(
            "Jumlah $n suku pertama barisan $a, ${a + d}, ${a + 2 * d}, ... adalah ...", n * inner / 2, r,
            explanation = "Rumus: Sn = n/2 × (2a + (n − 1)b).\nS$n = $n/2 × (2×$a + ${n - 1}×$d) = $n/2 × $inner = ${n * inner / 2}.",
            tip = "Sn dibaca jumlah n suku pertama. Huruf a adalah suku pertama dan b adalah beda."
        )
    }

    private fun derivative(r: Random): Question {
        val a = r.nextInt(1, 6)
        val b = r.nextInt(1, 10)
        val c = r.nextInt(1, 6)
        return numeric(
            "Jika f(x) = ${a}x² + ${b}x, maka f'($c) = ...", 2 * a * c + b, r,
            explanation = "Turunan ax² adalah 2ax, dan turunan bx adalah b.\nf'(x) = ${2 * a}x + $b.\nf'($c) = ${2 * a} × $c + $b = ${2 * a * c + b}.",
            tip = "Turunan menyatakan laju perubahan fungsi, atau kemiringan garis singgung di suatu titik."
        )
    }
}
