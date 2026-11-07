package com.icanmath.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class QuestionGeneratorTest {

    @Test
    fun allGradesProduceValidQuestions() {
        for (g in 1..12) {
            val qs = QuestionGenerator.generate(g, 30, Random(g))
            assertTrue("kelas $g menghasilkan terlalu sedikit soal", qs.size >= 20)
            for (q in qs) {
                assertEquals(4, q.options.size)
                assertEquals("opsi harus unik: ${q.text}", 4, q.options.toSet().size)
                assertTrue(q.answerIndex in 0..3)
            }
        }
    }

    @Test
    fun everyGeneratedQuestionHasExplanation() {
        for (g in 1..12) {
            for (q in QuestionGenerator.generate(g, 30, Random(100 + g))) {
                assertTrue("soal tanpa penjelasan: ${q.text}", q.explanation.isNotBlank())
            }
        }
    }

    @Test
    fun numericAnswerIsAmongOptions() {
        val q = QuestionGenerator.numeric("2 + 3 = ?", 5, Random(1))
        assertEquals("5", q.options[q.answerIndex])
    }
}
