package com.example.matematikaceria

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream

/** Membuat lembar soal A4 (dan halaman kunci jawaban) dengan PdfDocument bawaan Android. */
object PdfExporter {
    private const val W = 595
    private const val H = 842
    private const val M = 48f

    fun createWorksheet(
        context: Context,
        school: String,
        grade: Int,
        questions: List<Question>,
        withKey: Boolean
    ): File {
        val doc = PdfDocument()
        val title = Paint().apply { textSize = 16f; isFakeBoldText = true; isAntiAlias = true }
        val body = Paint().apply { textSize = 12f; isAntiAlias = true }

        var pageNo = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(W, H, pageNo).create())
        var canvas = page.canvas
        var y = M + 10f

        fun newPage() {
            doc.finishPage(page)
            pageNo++
            page = doc.startPage(PdfDocument.PageInfo.Builder(W, H, pageNo).create())
            canvas = page.canvas
            y = M
        }

        fun line(text: String, paint: Paint, gap: Float) {
            if (y > H - M) newPage()
            canvas.drawText(text, M, y, paint)
            y += gap
        }

        fun center(text: String, paint: Paint, gap: Float) {
            canvas.drawText(text, W / 2f - paint.measureText(text) / 2f, y, paint)
            y += gap
        }

        if (school.isNotBlank()) center(school, title, 22f)
        center("LATIHAN MATEMATIKA ${Stats.gradeLabel(grade).uppercase()}", title, 28f)
        line("Nama: ................................      Tanggal: ....................", body, 30f)

        questions.forEachIndexed { i, q ->
            val lines = wrap("${i + 1}. ${q.text}", body, W - 2 * M)
            val needed = lines.size * 16f + 40f
            if (y + needed > H - M) newPage()
            lines.forEach { line(it, body, 16f) }
            val opts = q.options.mapIndexed { k, o -> "${'A' + k}. $o" }.joinToString("      ")
            line("      $opts", body, 30f)
        }

        if (withKey) {
            newPage()
            line("KUNCI JAWABAN", title, 26f)
            questions.forEachIndexed { i, q ->
                line("${i + 1}. ${'A' + q.answerIndex}. ${q.options[q.answerIndex]}", body, 18f)
            }
        }

        doc.finishPage(page)
        val dir = File(context.cacheDir, "pdf").apply { mkdirs() }
        val file = File(dir, "latihan-matematika.pdf")
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    private fun wrap(text: String, paint: Paint, maxWidth: Float): List<String> {
        val out = ArrayList<String>()
        var cur = ""
        for (w in text.split(" ")) {
            val t = if (cur.isEmpty()) w else "$cur $w"
            if (paint.measureText(t) > maxWidth && cur.isNotEmpty()) {
                out.add(cur)
                cur = w
            } else {
                cur = t
            }
        }
        if (cur.isNotEmpty()) out.add(cur)
        return out
    }
}
