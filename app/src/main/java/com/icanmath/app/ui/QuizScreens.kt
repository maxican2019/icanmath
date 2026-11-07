package com.icanmath.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.icanmath.app.AppViewModel
import com.icanmath.app.Question
import com.icanmath.app.QuestionBank
import com.icanmath.app.QuizState
import com.icanmath.app.Stats
import kotlinx.coroutines.delay

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
    val ctx = LocalContext.current
    val mcq = QuestionBank.count(ctx, "questions_mcq.json", grade)
    // Soal cerita hanya tersedia jika bank ceritanya ada (SD 1-6). Di SMP dan SMA soal sudah berbentuk soal biasa.
    val story = QuestionBank.count(ctx, "questions_story.json", grade)

    fun start(kind: String) {
        vm.startQuiz(kind, grade)
        nav.navigate("quiz")
    }
    AppScaffold("Kelas $grade (${Stats.gradeLabel(grade)})", { nav.popBackStack() }) {
        ModeCard("🎲", "Soal acak", "Soal baru dibuat tiap mulai, tidak akan habis") { start("RANDOM") }
        if (mcq > 0) ModeCard("🎯", "Pilihan ganda", "$mcq soal tetap dengan pembahasan") { start("MCQ") }
        if (story > 0) ModeCard("📖", "Soal cerita", "$story soal cerita dengan kata kunci") { start("STORY") }
    }
}

@Composable
private fun ModeCard(emoji: String, title: String, desc: String, onClick: () -> Unit) {
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(emoji, fontSize = 32.sp)
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(desc, style = MaterialTheme.typography.bodyMedium)
            }
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
    var combo by remember { mutableIntStateOf(0) }
    var points by remember { mutableIntStateOf(0) }
    var gained by remember { mutableIntStateOf(0) }
    var showExit by remember { mutableStateOf(false) }
    val wrong = remember { mutableStateListOf<Question>() }
    val haptic = LocalHapticFeedback.current

    val q = quiz.questions[idx]
    val total = quiz.questions.size
    val last = idx == total - 1
    val isCorrect = selected == q.answerIndex
    val kindLabel = when (quiz.kind) {
        "DAILY" -> "Latihan harian"
        "RANDOM" -> "Soal acak"
        "MCQ" -> "Pilihan ganda"
        else -> "Soal cerita"
    }

    PlayScaffold(
        onClose = { showExit = true },
        progress = (idx + if (checked) 1 else 0).toFloat() / total,
        accent = MaterialTheme.colorScheme.primary,
        topRight = "⭐ $points",
        bottom = {
            Column {
                if (!checked) {
                    BottomActionBar("Periksa", selected >= 0) {
                        checked = true
                        if (isCorrect) {
                            correct++
                            combo++
                            gained = 10 + if (combo >= 3) 5 else 0
                            points += gained
                        } else {
                            combo = 0
                            gained = 0
                            wrong.add(q)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    }
                }
                AnimatedVisibility(
                    visible = checked,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn()
                ) {
                    FeedbackPanel(
                        correct = isCorrect,
                        correctAnswer = q.options[q.answerIndex],
                        explanation = q.explanation,
                        tip = q.tip,
                        points = gained,
                        nextLabel = if (last) "Lihat hasil" else "Lanjut"
                    ) {
                        if (last) {
                            vm.finishQuiz(correct, wrong.toList())
                            nav.navigate("result") { popUpTo("quiz") { inclusive = true } }
                        } else {
                            idx++
                            selected = -1
                            checked = false
                        }
                    }
                }
            }
        }
    ) {
        Text(
            "$kindLabel · Soal ${idx + 1} dari $total",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (combo >= 2) {
            Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFFFE8CC)) {
                Text(
                    "🔥 $combo beruntun! Teruskan!",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    color = Color(0xFF7A3E00),
                    fontWeight = FontWeight.Bold
                )
            }
        }
        vm.notice?.let { Text(it, color = MaterialTheme.colorScheme.secondary) }
        ElevatedCard(Modifier.fillMaxWidth()) {
            Text(
                q.text,
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
        val shortOptions = q.options.all { it.length <= 8 }
        q.options.indices.toList().chunked(if (shortOptions) 2 else 1).forEach { rowIdx ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowIdx.forEach { i ->
                    val state = when {
                        checked && i == q.answerIndex -> OptState.CORRECT
                        checked && i == selected -> OptState.WRONG
                        checked -> OptState.DIM
                        i == selected -> OptState.SELECTED
                        else -> OptState.IDLE
                    }
                    AnswerOption(
                        letter = ('A' + i).toString(),
                        text = q.options[i],
                        state = state,
                        enabled = !checked,
                        modifier = Modifier.weight(1f),
                        big = shortOptions
                    ) { selected = i }
                }
            }
        }
    }

    if (showExit) {
        AlertDialog(
            onDismissRequest = { showExit = false },
            title = { Text("Keluar dari latihan?") },
            text = { Text("Progres sesi ini tidak akan disimpan.") },
            confirmButton = { TextButton(onClick = { showExit = false; nav.popBackStack() }) { Text("Keluar") } },
            dismissButton = { TextButton(onClick = { showExit = false }) { Text("Lanjut main") } }
        )
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
        val pct = if (r.total == 0) 0 else r.correct * 100 / r.total
        val stars = when {
            pct >= 90 -> 3
            pct >= 60 -> 2
            pct >= 30 -> 1
            else -> 0
        }
        val message = when (stars) {
            3 -> "Luar biasa! Kamu hebat!"
            2 -> "Bagus sekali! Sedikit lagi sempurna."
            1 -> "Terus berlatih, kamu pasti bisa!"
            else -> "Jangan menyerah, pelajari dulu lalu coba lagi!"
        }
        var shown by remember { mutableIntStateOf(0) }
        LaunchedEffect(Unit) {
            repeat(stars) {
                delay(350)
                shown = it + 1
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            repeat(3) { i ->
                val earned = i < stars
                val visible = !earned || i < shown
                val scale by animateFloatAsState(
                    targetValue = if (visible) 1f else 0.3f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "star$i"
                )
                Text(
                    if (earned) "⭐" else "☆",
                    fontSize = 52.sp,
                    modifier = Modifier.graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        alpha = if (visible) 1f else 0f
                    }
                )
            }
        }
        Text(
            "${r.correct} / ${r.total}",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold
        )
        Text(message, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Text("Waktu ${r.seconds} detik", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)

        if (r.newMedals.isNotEmpty()) {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("🏅 Medali baru!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    r.newMedals.forEach { Text("${it.tier.label}: ${it.title}") }
                }
            }
        }

        if (r.wrong.isNotEmpty()) {
            Text("Pelajari lagi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            r.wrong.forEach { w ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(w.text, fontWeight = FontWeight.SemiBold)
                        Text("Jawaban yang benar: ${w.options[w.answerIndex]}", color = Color(0xFF1B5E20), fontWeight = FontWeight.Bold)
                        if (w.explanation.isNotBlank()) {
                            Text("Cara menghitung", fontWeight = FontWeight.Bold)
                            Text(w.explanation)
                        }
                        if (w.tip.isNotBlank()) {
                            Text("Tahukah kamu?", fontWeight = FontWeight.Bold)
                            Text(w.tip)
                        }
                    }
                }
            }
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
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(54.dp)
        ) { Text("Selesai", fontSize = 17.sp, fontWeight = FontWeight.Bold) }
    }
}
