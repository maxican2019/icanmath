package com.icanmath.app.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.icanmath.app.AppViewModel
import com.icanmath.app.QuestionGenerator
import com.icanmath.app.Medals
import kotlin.random.Random

/** Satu soal untuk game pilihan (puzzle penjumlahan dan bandingkan). */
class GameItem(
    val prompt: String,
    val big: String,
    val dotsLeft: Int,
    val dotsRight: Int,
    val options: List<String>,
    val answerIndex: Int,
    val hint: String,
    val explanation: String,
    val tip: String
)

fun puzzleItems(grade: Int, r: Random): List<GameItem> = List(5) {
    val max = when {
        grade <= 1 -> 10
        grade == 2 -> 20
        grade == 3 -> 100
        else -> 200
    }
    val c = r.nextInt(max / 2, max + 1)
    val a = r.nextInt(1, c)
    val q = QuestionGenerator.numeric("$a + ? = $c", c - a, r)
    GameItem(
        "Isi kotak yang hilang", "$a + ? = $c", 0, 0, q.options, q.answerIndex,
        "Petunjuk: $c − $a = ?",
        "Cari angka yang hilang dengan pengurangan: $c − $a = ${c - a}.\nCek: $a + ${c - a} = $c.",
        "Pengurangan adalah kebalikan penjumlahan, jadi bisa dipakai untuk mencari angka yang hilang."
    )
}

fun compareItems(grade: Int, r: Random): List<GameItem> = List(5) {
    val (a, b) = when {
        grade <= 2 -> r.nextInt(1, 11) to r.nextInt(1, 11)
        grade <= 4 -> r.nextInt(10, 100) to r.nextInt(10, 100)
        grade <= 6 -> r.nextInt(100, 1000) to r.nextInt(100, 1000)
        else -> r.nextInt(-20, 21) to r.nextInt(-20, 21)
    }
    val ans = if (a > b) 0 else if (a == b) 1 else 2
    val dots = grade <= 2
    val explanation = when (ans) {
        0 -> "$a lebih besar dari $b, jadi tandanya >.\n$a > $b."
        1 -> "$a sama dengan $b, jadi tandanya =.\n$a = $b."
        else -> "$a lebih kecil dari $b, jadi tandanya <.\n$a < $b."
    }
    GameItem(
        "Pilih tanda yang tepat", "$a ? $b",
        if (dots) a else 0, if (dots) b else 0,
        listOf(">", "=", "<"), ans, "Petunjuk: bandingkan $a dan $b",
        explanation,
        "Tanda > dan < seperti mulut buaya: mulutnya selalu terbuka ke bilangan yang lebih besar."
    )
}

@Composable
fun LogicMenuScreen(vm: AppViewModel, nav: NavHostController) {
    val data by vm.data.collectAsState()
    val p = data.profiles.firstOrNull { it.id == data.activeProfileId }
    val medals = if (p != null) Medals.logic(data.logicPlays, p.id) else emptyList()
    fun status(i: Int): String? = medals.getOrNull(i)?.let { "🏅 ${it.tier.label} · ${it.detail}" }

    FunScaffold(
        "Latihan Logika", { nav.popBackStack() },
        subtitle = "Asah otak sambil bermain!",
        accent = Color(0xFF00695C), accent2 = Color(0xFF2E7D32),
        headerSlot = Slot.MENU_LOGIKA, headerEmoji = "🧠"
    ) {
        FunCard(
            "Puzzle penjumlahan", "Temukan angka yang hilang", "🧩", Slot.LOGIC_PUZZLE,
            Color(0xFFC62828), Color(0xFFE64A19), index = 0, badge = status(0)
        ) { nav.navigate("puzzle") }
        FunCard(
            "Lebih besar atau kecil", "Mana yang lebih banyak?", "⚖️", Slot.LOGIC_COMPARE,
            Color(0xFF6A1B9A), Color(0xFF8E24AA), index = 1, badge = status(1)
        ) { nav.navigate("compare") }
        FunCard(
            "Kelompokkan", "Pisahkan buah ke keranjangnya", "🍎", Slot.LOGIC_GROUP,
            Color(0xFF2E7D32), Color(0xFF00897B), index = 2, badge = status(2)
        ) { nav.navigate("group") }
        FunCard(
            "Permainan lain", "Rumah game: Math Battle dan lainnya", "🎮", Slot.LOGIC_OTHER,
            Color(0xFF37474F), Color(0xFF00695C), index = 3, badge = "Baru!"
        ) { nav.navigate("games") }
        Text(
            "Medali logika terpisah dari medali latihan harian dan tidak memengaruhi ranking.",
            style = MaterialTheme.typography.bodySmall
        )
        FunButton("🏅 Lihat medali", Color(0xFFB26A00), Color(0xFFD48806)) { nav.navigate("profile") }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Dots(n: Int, color: Color) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(n) { Box(Modifier.size(22.dp).background(color, CircleShape)) }
    }
}

@Composable
fun GameSummary(vm: AppViewModel, solved: Int, total: Int, onDone: () -> Unit) {
    Text("Selesai!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    Text("$solved dari $total benar", style = MaterialTheme.typography.titleLarge)
    vm.logicMedals.forEach { Text("Medali baru: ${it.tier.label} · ${it.title}", fontWeight = FontWeight.Bold) }
    Button(onClick = onDone, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Selesai") }
}

@Composable
fun ChoiceGameScreen(
    vm: AppViewModel,
    nav: NavHostController,
    game: String,
    title: String,
    make: (Int, Random) -> List<GameItem>
) {
    val grade = vm.activeProfile()?.grade ?: 1
    val items = remember { make(grade, Random(System.currentTimeMillis())) }
    var idx by remember { mutableIntStateOf(0) }
    var selected by remember { mutableIntStateOf(-1) }
    var checked by remember { mutableStateOf(false) }
    var solved by remember { mutableIntStateOf(0) }
    var hint by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    var showExit by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<FeedbackData?>(null) }

    if (finished) {
        FunScaffold(
            title, { nav.popBackStack() },
            subtitle = "Permainan selesai!",
            accent = Color(0xFF00695C), accent2 = Color(0xFF2E7D32),
            headerSlot = Slot.MENU_LOGIKA, headerEmoji = "🧠"
        ) {
            GameSummary(vm, solved, items.size) { nav.popBackStack() }
        }
        return
    }

    val item = items[idx]
    val last = idx == items.lastIndex
    val isCorrect = selected == item.answerIndex

    PlayScaffold(
        onClose = { showExit = true },
        progress = (idx + if (checked) 1 else 0).toFloat() / items.size,
        accent = MaterialTheme.colorScheme.secondary,
        topRight = "⭐ ${solved * 10}",
        bottom = {
            Column {
                if (!checked) {
                    BottomActionBar("Periksa", selected >= 0) {
                        if (isCorrect) solved++
                        feedback = FeedbackData(
                            correct = isCorrect,
                            answer = item.options[item.answerIndex],
                            explanation = item.explanation,
                            tip = item.tip,
                            points = if (isCorrect) 10 else 0,
                            nextLabel = if (last) "Selesai" else "Lanjut"
                        )
                        checked = true
                    }
                }
                feedback?.let { f ->
                    AnimatedVisibility(
                        visible = checked,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = fadeOut(tween(90))
                    ) {
                        FeedbackPanel(f.correct, f.answer, f.explanation, f.tip, f.points, f.nextLabel) {
                            if (last) {
                                vm.saveLogic(game, solved, items.size)
                                finished = true
                            } else {
                                idx++
                                selected = -1
                                checked = false
                                hint = false
                            }
                        }
                    }
                }
            }
        }
    ) {
        Text("$title · Puzzle ${idx + 1} dari ${items.size}", style = MaterialTheme.typography.labelLarge)
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(
                Modifier.padding(20.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(item.prompt)
                Text(item.big, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                if (item.dotsLeft > 0 || item.dotsRight > 0) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Dots(item.dotsLeft, MaterialTheme.colorScheme.primary)
                            Text("${item.dotsLeft}", fontWeight = FontWeight.Bold)
                        }
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Dots(item.dotsRight, MaterialTheme.colorScheme.primary)
                            Text("${item.dotsRight}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        val perRow = if (item.options.size == 3) 3 else 2
        item.options.indices.toList().chunked(perRow).forEach { rowIdx ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowIdx.forEach { i ->
                    val state = when {
                        checked && i == item.answerIndex -> OptState.CORRECT
                        checked && i == selected -> OptState.WRONG
                        checked -> OptState.DIM
                        i == selected -> OptState.SELECTED
                        else -> OptState.IDLE
                    }
                    AnswerOption(
                        letter = ('A' + i).toString(),
                        text = item.options[i],
                        state = state,
                        enabled = !checked,
                        modifier = Modifier.weight(1f),
                        big = true
                    ) { selected = i }
                }
            }
        }
        if (!checked) {
            TextButton(onClick = { hint = true }) { Text("Petunjuk") }
            if (hint) Text(item.hint)
        }
    }

    if (showExit) {
        AlertDialog(
            onDismissRequest = { showExit = false },
            title = { Text("Keluar dari permainan?") },
            text = { Text("Progres permainan ini tidak akan disimpan.") },
            confirmButton = { TextButton(onClick = { showExit = false; nav.popBackStack() }) { Text("Keluar") } },
            dismissButton = { TextButton(onClick = { showExit = false }) { Text("Lanjut main") } }
        )
    }
}

private val fruitNames = listOf("Apel", "Pisang", "Anggur")
private val fruitLetters = listOf("A", "P", "G")
private val fruitColors = listOf(Color(0xFFC62828), Color(0xFFB7791F), Color(0xFF6A1B9A))

private val fruitSlots = listOf(Slot.FRUIT_APPLE, Slot.FRUIT_BANANA, Slot.FRUIT_GRAPE)

@Composable
fun FruitView(type: Int, selected: Boolean) {
    val ring = if (selected) {
        Modifier.border(3.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(14.dp))
    } else {
        Modifier
    }
    SlotImage(fruitSlots[type], Modifier.size(52.dp).then(ring)) {
        val shape = if (type == 1) RoundedCornerShape(50) else CircleShape
        val size = when (type) {
            1 -> Modifier.size(width = 56.dp, height = 36.dp)
            2 -> Modifier.size(30.dp)
            else -> Modifier.size(44.dp)
        }
        val outline = if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.secondary, shape) else Modifier
        Box(
            modifier = size.background(fruitColors[type], shape).then(outline),
            contentAlignment = Alignment.Center
        ) {
            Text(fruitLetters[type], color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GroupGameScreen(vm: AppViewModel, nav: NavHostController) {
    val grade = vm.activeProfile()?.grade ?: 1
    val types = if (grade <= 2) 2 else 3
    val rnd = remember { Random(System.currentTimeMillis()) }
    var round by remember { mutableIntStateOf(0) }
    var selected by remember { mutableIntStateOf(-1) }
    var mistakes by remember { mutableIntStateOf(0) }
    var roundsOk by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf("Ketuk buah, lalu ketuk keranjangnya") }
    var finished by remember { mutableStateOf(false) }

    val tray = remember(round) {
        val n = (if (grade <= 2) 4 else 6) + round * 2
        val base = (0 until types).toList() + List(n - types) { rnd.nextInt(types) }
        mutableStateListOf<Int>().apply { addAll(base.shuffled(rnd)) }
    }
    val placed = remember(round) {
        mutableStateListOf<Int>().apply { repeat(types) { add(0) } }
    }

    FunScaffold(
        "Kelompokkan Buah", { nav.popBackStack() },
        subtitle = "Ketuk buah, lalu ketuk keranjangnya",
        accent = Color(0xFF2E7D32), accent2 = Color(0xFF00897B),
        headerSlot = Slot.LOGIC_GROUP, headerEmoji = "🍎"
    ) {
        if (finished) {
            GameSummary(vm, roundsOk, 3) { nav.popBackStack() }
            return@FunScaffold
        }
        Text("Ronde ${round + 1} dari 3", fontWeight = FontWeight.SemiBold)
        Text(message)
        ElevatedCard(Modifier.fillMaxWidth()) {
            FlowRow(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                tray.forEachIndexed { i, t ->
                    Box(Modifier.clickable { selected = i }.padding(6.dp)) {
                        FruitView(t, selected == i)
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            for (t in 0 until types) {
                Card(
                    onClick = {
                        if (selected < 0 || selected >= tray.size) {
                            message = "Pilih buah dulu"
                        } else if (tray[selected] == t) {
                            tray.removeAt(selected)
                            placed[t] = placed[t] + 1
                            selected = -1
                            message = "Benar!"
                            if (tray.isEmpty()) {
                                val ok = roundsOk + if (mistakes <= 1) 1 else 0
                                roundsOk = ok
                                if (round == 2) {
                                    vm.saveLogic("GROUP", ok, 3)
                                    finished = true
                                } else {
                                    round++
                                    mistakes = 0
                                    selected = -1
                                    message = "Ronde berikutnya"
                                }
                            }
                        } else {
                            mistakes++
                            selected = -1
                            message = "Belum tepat, coba lagi"
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Column(Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Keranjang ${fruitNames[t]}", fontWeight = FontWeight.SemiBold)
                        Text("${placed[t]}", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
        }
    }
}
