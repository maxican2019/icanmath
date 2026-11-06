package com.example.matematikaceria

import android.content.Context
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.random.Random

/** Bank soal tetap, dibaca dari file JSON di folder assets. Tambahkan soal baru langsung di file JSON. */
object QuestionBank {
    private val json = Json { ignoreUnknownKeys = true }
    private val cache = HashMap<String, List<Question>>()

    fun load(context: Context, file: String): List<Question> = cache.getOrPut(file) {
        try {
            context.assets.open(file).bufferedReader().use { json.decodeFromString<List<Question>>(it.readText()) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun pick(context: Context, file: String, grade: Int, count: Int, r: Random): List<Question> =
        load(context, file).filter { it.grade == grade }.shuffled(r).take(count)
}
