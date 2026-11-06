package com.example.matematikaceria.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.matematikaceria.AppViewModel
import com.example.matematikaceria.QuizState
import com.example.matematikaceria.Stats

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClassSelectScreen(vm: AppViewModel, nav: NavHostController) {
    val p = vm.activeProfile()
    val pg = p?.grade ?: 1
    var tab by remember { mutableIntStateOf(if (pg <= 6) 0 else if (pg <= 9) 1 else 2) }
    val ranges = listOf(1..6, 7..9, 10..12)

    AppScaffold("Pilih Kelas", { nav.popBackStack() }) {
        TabRow(selectedTabIndex = tab) {
            listOf("SD", "SMP", "SMA").forEachIndexed { i, t ->
                Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t) })
            }
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            for (g in ranges[tab]) {
                ElevatedButton(
                    onClick = { nav.navigate("mode/$g") },
                    modifier = Modifier.width(104.dp).height(80.dp)
                ) { Text("Kelas $g") }
            }
        }
        if (p != null) {
            Text(
                "Kelas kamu: ${Stats.gradeLabel(p.grade)}. Latihan di kelas lain tidak masuk ranking dan medali, tetap tercatat di riwayat.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ModeSelectScreen(vm: AppViewModel, nav: NavHostController, grade: Int) {
    fun start(kind: String) {
        vm.startQuiz(kind, grade)
        nav.navigate("quiz")
    }
    AppScaffold("Kelas $grade (${Stats.gradeLabel(grade)})", { nav.popBackStack() }) {
        ModeCard("Random", "Soal baru dibuat tiap mulai") { start("RANDOM") }
        ModeCard("Pilihan ganda", "Bank soal tetap dengan pembahasan") { start("MCQ") }
        ModeCard("Soal cerita", "Bank soal cerita tetap") { start("STORY") }
    }
}

@Composable
private fun ModeCard(title: String, desc: String, onClick: () -> Unit) {
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp).fillMaxWidth()) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(desc, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun QuizScreen(vm: AppViewModel, nav: NavHostController) {
    val quiz = vm.quiz
    if (quiz == null) {
        LaunchedEffect(Unit) { nav.popBackStack() }
        Box(Modifier.fillMaxSize())
    } else {
        QuizContent(vm, nav, quiz)
    }
}

@Composable
private fun QuizContent(vm: AppViewModel, nav: NavHostController, quiz: QuizState) {
    var idx by remember { mutableIntStateOf(0) }
    var selected by remember { mutableIntStateOf(-1) }
    var checked by remember { mutableStateOf(false) }
    var correct by remember { mutableIntStateOf(0) }
    val q = quiz.questions[idx]
    val total = quiz.questions.size

    AppScaffold("Soal ${idx + 1} dari $total", { nav.popBackStack() }) {
        LinearProgressIndicator(
            progress = { (idx + if (checked) 1 else 0).toFloat() / total },
            modifier = Modifier.fillMaxWidth()
        )
        vm.notice?.let { Text(it, color = MaterialTheme.colorScheme.secondary) }
        ElevatedCard(Modifier.fillMaxWidth()) {
            Text(q.text, Modifier.padding(20.dp), style = MaterialTheme.typography.titleLarge)
        }
        q.options.forEachIndexed { i, opt ->
            val label = when {
                checked && i == q.answerIndex -> "✓ $opt"
                checked && i == selected -> "✗ $opt"
                else -> opt
            }
            if (i == selected && !checked) {
                Button(onClick = { selected = i }, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text(label) }
            } else {
                OutlinedButton(
                    onClick = { if (!checked) selected = i },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) { Text(label) }
            }
        }
        if (checked) {
            val ok = selected == q.answerIndex
            Text(
                if (ok) "Benar!" else "Kurang tepat. Jawaban: ${q.options[q.answerIndex]}",
                fontWeight = FontWeight.Bold
            )
            if (q.explanation.isNotBlank()) Text(q.explanation)
        }
        Button(
            enabled = selected >= 0,
            onClick = {
                if (!checked) {
                    checked = true
                    if (selected == q.answerIndex) correct++
                } else if (idx == total - 1) {
                    vm.finishQuiz(correct)
                    nav.navigate("result") { popUpTo("quiz") { inclusive = true } }
                } else {
                    idx++
                    selected = -1
                    checked = false
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text(if (!checked) "Periksa" else if (idx == total - 1) "Selesai" else "Lanjut")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResultScreen(vm: AppViewModel, nav: NavHostController) {
    val r = vm.result
    val data by vm.data.collectAsState()
    var chosen by remember { mutableStateOf(data.activeProfileId) }

    AppScaffold("Hasil Latihan") {
        if (r == null) {
            Text("Tidak ada hasil.")
            return@AppScaffold
        }
        Text("${r.correct} / ${r.total}", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
        Text("Waktu ${r.seconds} detik")
        if (r.newMedals.isNotEmpty()) {
            Text("Medali baru!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            r.newMedals.forEach { Text("${it.tier.label}: ${it.title}") }
        }
        // Pilih profil hanya muncul jika ada lebih dari 1 profil. Default: profil yang sama.
        if (data.profiles.size > 1) {
            Text("Lanjut sebagai profil:", fontWeight = FontWeight.SemiBold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                data.profiles.forEach { pr ->
                    val same = if (pr.id == data.activeProfileId) " (sama)" else ""
                    FilterChip(
                        selected = chosen == pr.id,
                        onClick = { chosen = pr.id },
                        label = { Text(pr.name + same) }
                    )
                }
            }
        }
        Button(
            onClick = {
                chosen?.let { vm.setActive(it) }
                nav.navigate("dashboard") { popUpTo("dashboard") { inclusive = true } }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) { Text("Selesai") }
    }
}
