package com.example.matematikaceria

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.util.UUID
import kotlin.random.Random

class QuizState(
    val kind: String,
    val grade: Int,
    val questions: List<Question>,
    val countsForRank: Boolean,
    val startedAt: Long
)

class ResultState(
    val kind: String,
    val correct: Int,
    val total: Int,
    val seconds: Int,
    val newMedals: List<Medal>
)

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repository(app)
    val data: StateFlow<AppData> = repo.data

    var quiz by mutableStateOf<QuizState?>(null)
        private set
    var result by mutableStateOf<ResultState?>(null)
        private set
    var notice by mutableStateOf<String?>(null)
        private set
    var logicMedals by mutableStateOf<List<Medal>>(emptyList())
        private set

    fun activeProfile(): Profile? {
        val d = data.value
        return d.profiles.firstOrNull { it.id == d.activeProfileId }
    }

    fun addProfile(name: String, age: Int, grade: Int) {
        val p = Profile(UUID.randomUUID().toString(), name, age, grade)
        repo.update { it.copy(profiles = it.profiles + p, activeProfileId = p.id) }
    }

    fun setActive(id: String) {
        repo.update { it.copy(activeProfileId = id) }
    }

    fun deleteProfile(id: String) {
        repo.update { d ->
            val rest = d.profiles.filter { it.id != id }
            d.copy(
                profiles = rest,
                sessions = d.sessions.filter { it.profileId != id },
                logicPlays = d.logicPlays.filter { it.profileId != id },
                activeProfileId = if (d.activeProfileId == id) rest.firstOrNull()?.id else d.activeProfileId
            )
        }
    }

    /**
     * Memulai latihan. kind: DAILY, RANDOM, MCQ, STORY.
     * Hanya latihan di kelas profil yang masuk ranking dan medali.
     */
    fun startQuiz(kind: String, grade: Int) {
        val p = activeProfile() ?: return
        val ctx = getApplication<Application>()
        notice = null
        val questions: List<Question>
        val g: Int
        when (kind) {
            "DAILY" -> {
                g = p.grade
                // Seed dari tanggal: soal hari ini sama jika dibuka ulang di hari yang sama.
                val r = Random(LocalDate.now().toEpochDay() * 31 + g)
                val mcq = QuestionBank.pick(ctx, "questions_mcq.json", g, 2, r)
                val story = QuestionBank.pick(ctx, "questions_story.json", g, 1, r)
                val fixed = mcq + story
                val random = QuestionGenerator.generate(g, 5 - fixed.size, r)
                questions = (fixed + random).shuffled(r)
            }
            "MCQ", "STORY" -> {
                g = grade
                val file = if (kind == "MCQ") "questions_mcq.json" else "questions_story.json"
                val picked = QuestionBank.pick(ctx, file, g, 10, Random(System.currentTimeMillis()))
                if (picked.isEmpty()) {
                    notice = "Bank soal ${Stats.gradeLabel(g)} belum tersedia, memakai soal acak."
                    questions = QuestionGenerator.generate(g, 10, Random(System.currentTimeMillis()))
                } else {
                    questions = picked
                }
            }
            else -> {
                g = grade
                questions = QuestionGenerator.generate(g, 10, Random(System.currentTimeMillis()))
            }
        }
        quiz = QuizState(kind, g, questions, g == p.grade, System.currentTimeMillis())
        result = null
    }

    fun finishQuiz(correct: Int) {
        val q = quiz ?: return
        val p = activeProfile() ?: return
        val seconds = ((System.currentTimeMillis() - q.startedAt) / 1000).toInt()
        val before = Medals.practice(data.value.sessions, p.id)
        val s = Session(p.id, LocalDate.now().toString(), q.kind, q.grade, correct, q.questions.size, seconds, q.countsForRank)
        repo.update { it.copy(sessions = it.sessions + s) }
        val after = Medals.practice(data.value.sessions, p.id)
        result = ResultState(q.kind, correct, q.questions.size, seconds, Medals.newlyEarned(before, after))
    }

    fun saveLogic(game: String, solved: Int, total: Int) {
        val p = activeProfile() ?: return
        val before = Medals.logic(data.value.logicPlays, p.id)
        val play = LogicPlay(p.id, LocalDate.now().toString(), game, solved, total)
        repo.update { it.copy(logicPlays = it.logicPlays + play) }
        logicMedals = Medals.newlyEarned(before, Medals.logic(data.value.logicPlays, p.id))
    }

    fun backupJson(): String = repo.exportJson()
    fun restore(text: String): Boolean = repo.importJson(text)
    fun resetAll() = repo.reset()
}
