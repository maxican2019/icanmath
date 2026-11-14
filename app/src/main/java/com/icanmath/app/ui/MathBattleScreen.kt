package com.icanmath.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.icanmath.app.AppViewModel
import com.icanmath.app.Question
import com.icanmath.app.battle.ActionType
import com.icanmath.app.battle.BattleEngine
import com.icanmath.app.battle.BattleLevels
import com.icanmath.app.battle.BattleProgress
import com.icanmath.app.battle.BattleQuestions
import com.icanmath.app.battle.BattleRewards
import com.icanmath.app.battle.BattleRules
import com.icanmath.app.battle.BattleState
import com.icanmath.app.battle.LevelConfig
import com.icanmath.app.battle.Outcome
import com.icanmath.app.battle.RewardResult
import com.icanmath.app.battle.Tier
import com.icanmath.app.battle.TurnLog
import kotlin.random.Random

private enum class Phase { CHOOSE, ASK, RESULT, END }

/** Pemicu animasi satu giliran. [id] berubah setiap giliran. */
class BattleFx(val id: Int, val hero: ActionType?, val correct: Boolean, val monsterAttacks: Boolean)

private fun describeTurn(a: ActionType, t: Tier, correct: Boolean, log: TurnLog, state: BattleState): String {
    val name = when (a) {
        ActionType.ARROW -> "Panah ${t.label.lowercase()}"
        ActionType.SWORD -> "Pedang ${t.label.lowercase()}"
        ActionType.ARMOR -> "Armor ${t.label.lowercase()}"
    }
    val parts = ArrayList<String>()
    if (!correct) {
        parts.add("$name meleset karena jawabannya belum tepat.")
    } else if (a == ActionType.ARMOR) {
        parts.add("$name terpasang! DEF +${log.shieldGained}.")
    } else {
        parts.add("$name mengenai monster! (-${log.damageDealt})")
    }
    if (state.outcome == Outcome.WIN) {
        parts.add("Monster kalah!")
    } else if (log.monsterAttacked) {
        parts.add(
            if (log.absorbed > 0) "Monster membalas: armor menyerap ${log.absorbed}, kamu kena -${log.damageTaken}."
            else "Monster membalas: kamu kena -${log.damageTaken}."
        )
    }
    return parts.joinToString(" ")
}

@Composable
fun MathBattleScreen(vm: AppViewModel, nav: NavHostController) {
    val ctx = LocalContext.current
    val inventory = rememberInventory()
    val progress = remember { BattleProgress(ctx) }
    val grade = vm.activeProfile()?.grade ?: 3
    val rng = remember { Random(System.nanoTime()) }
    val haptic = LocalHapticFeedback.current

    var state by remember { mutableStateOf(BattleEngine.start(progress.level)) }
    var phase by remember { mutableStateOf(Phase.CHOOSE) }
    var action by remember { mutableStateOf<ActionType?>(null) }
    var tier by remember { mutableStateOf<Tier?>(null) }
    var question by remember { mutableStateOf<Question?>(null) }
    var selected by remember { mutableIntStateOf(-1) }
    var feedback by remember { mutableStateOf<FeedbackData?>(null) }
    var log by remember { mutableStateOf("Pilih senjata atau armor, lalu jawab soalnya!") }
    var reward by remember { mutableStateOf<RewardResult?>(null) }
    var fx by remember { mutableStateOf(BattleFx(0, null, false, false)) }
    var fxCounter by remember { mutableIntStateOf(0) }

    fun resetTurn() {
        phase = Phase.CHOOSE
        action = null
        tier = null
        question = null
        selected = -1
    }

    fun restartAt(level: Int) {
        state = BattleEngine.start(level)
        reward = null
        feedback = null
        fx = BattleFx(0, null, false, false)
        log = "Pilih senjata atau armor, lalu jawab soalnya!"
        resetTurn()
    }

    fun resolve() {
        val a = action ?: return
        val t = tier ?: return
        val q = question ?: return
        val correct = selected == q.answerIndex
        val (next, turn) = BattleEngine.resolveTurn(state, a, t, correct)
        state = next
        fxCounter++
        fx = BattleFx(fxCounter, a, correct, turn.monsterAttacked)
        log = describeTurn(a, t, correct, turn, next)
        if (next.outcome == Outcome.WIN) {
            // Undian hadiah dilakukan tepat SATU KALI, di sini, saat kemenangan terjadi.
            reward = BattleRewards.claimVictory(next.level.level, inventory)
            progress.level = BattleRules.nextLevelAfterWin(next.level.level)
        }
        if (!correct) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        // Isi panel dibekukan agar tidak berubah saat animasi keluar.
        feedback = FeedbackData(
            correct = correct,
            answer = q.options[q.answerIndex],
            explanation = q.explanation,
            tip = q.tip,
            points = 0,
            nextLabel = if (next.outcome != null) "Lanjut" else "Giliran berikutnya"
        )
        phase = Phase.RESULT
    }

    PlayScaffold(
        onClose = { nav.popBackStack() },
        progress = state.level.level / BattleRules.LEVELS.toFloat(),
        accent = MaterialTheme.colorScheme.secondary,
        topRight = "Level ${state.level.level}",
        bottom = {
            Column {
                if (phase == Phase.ASK) {
                    BottomActionBar(
                        if (action == ActionType.ARMOR) "Pasang armor!" else "Serang!",
                        selected >= 0
                    ) { resolve() }
                }
                feedback?.let { f ->
                    AnimatedVisibility(
                        visible = phase == Phase.RESULT,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = fadeOut(tween(90))
                    ) {
                        FeedbackPanel(f.correct, f.answer, f.explanation, f.tip, f.points, f.nextLabel) {
                            if (state.outcome != null) {
                                phase = Phase.END
                            } else {
                                resetTurn()
                                log = "Giliran berikutnya! Pilih lagi."
                            }
                        }
                    }
                }
            }
        }
    ) {
        InventoryBar()
        BattleStage(fx, state.level, state.shield, Modifier.fillMaxWidth().height(230.dp))
        // HP monster sengaja tidak ditampilkan.
        Text(state.level.monsterName, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("❤️ ${state.heroHp}/${BattleRules.HERO_HP}", fontWeight = FontWeight.Bold)
            LinearProgressIndicator(
                progress = { state.heroHp / BattleRules.HERO_HP.toFloat() },
                modifier = Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(5.dp))
            )
            if (state.shield > 0) Text("🛡️ ${state.shield}", fontWeight = FontWeight.Bold)
        }
        Text(log, style = MaterialTheme.typography.bodyMedium)

        when (phase) {
            Phase.CHOOSE -> ActionPicker { a, t ->
                action = a
                tier = t
                question = BattleQuestions.generate(grade, t, rng)
                selected = -1
                phase = Phase.ASK
            }
            Phase.ASK, Phase.RESULT -> {
                val q = question
                if (q != null) {
                    val checked = phase == Phase.RESULT
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Text(
                            q.text,
                            modifier = Modifier.padding(20.dp).fillMaxWidth(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                    val shortOptions = q.options.all { it.length <= 8 }
                    q.options.indices.toList().chunked(if (shortOptions) 2 else 1).forEach { rowIdx ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            rowIdx.forEach { i ->
                                val optState = when {
                                    checked && i == q.answerIndex -> OptState.CORRECT
                                    checked && i == selected -> OptState.WRONG
                                    checked -> OptState.DIM
                                    i == selected -> OptState.SELECTED
                                    else -> OptState.IDLE
                                }
                                AnswerOption(
                                    letter = ('A' + i).toString(),
                                    text = q.options[i],
                                    state = optState,
                                    enabled = !checked,
                                    modifier = Modifier.weight(1f),
                                    big = shortOptions
                                ) { selected = i }
                            }
                        }
                    }
                }
            }
            Phase.END -> {
                val won = state.outcome == Outcome.WIN
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            if (won) "🎉 Menang!" else "Kalah, ayo coba lagi!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (won) {
                            Text(reward?.message ?: "", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            InventoryBar()
                            Text("Berikutnya: Level ${progress.level}")
                        } else {
                            Text("Kamu bisa mengulang level ${state.level.level}.")
                        }
                    }
                }
                FunButton(
                    if (won) "Level berikutnya" else "Coba lagi",
                    Color(0xFFC62828), Color(0xFF8E24AA)
                ) { restartAt(if (won) progress.level else state.level.level) }
                OutlinedButton(onClick = { nav.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Kembali ke Rumah Game")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActionPicker(onPick: (ActionType, Tier) -> Unit) {
    val actions = listOf(
        Triple(ActionType.ARROW, "🏹 Panah", "Serang dari jauh"),
        Triple(ActionType.SWORD, "⚔️ Pedang", "Serang dari dekat, damage besar"),
        Triple(ActionType.ARMOR, "🛡️ Armor", "Pasang DEF untuk menahan serangan")
    )
    actions.forEach { (type, title, desc) ->
        Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(title, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                Text(desc, style = MaterialTheme.typography.bodySmall)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BattleRules.tiers(type).forEach { t -> TierChip(type, t) { onPick(type, t) } }
                }
            }
        }
    }
}

@Composable
private fun TierChip(type: ActionType, t: Tier, onClick: () -> Unit) {
    val colors = when (t.id) {
        "kayu" -> listOf(Color(0xFF6D4C41), Color(0xFF8D6E63))
        "besi" -> listOf(Color(0xFF455A64), Color(0xFF607D8B))
        "emas" -> listOf(Color(0xFF9A6700), Color(0xFFB7791F))
        "pelangi" -> listOf(Color(0xFFC62828), Color(0xFF9A6700), Color(0xFF2E7D32), Color(0xFF1565C0), Color(0xFF6A1B9A))
        "kulit" -> listOf(Color(0xFF795548), Color(0xFF8D6E63))
        else -> listOf(Color(0xFF01579B), Color(0xFF0277BD))
    }
    val label = if (type == ActionType.ARMOR) "${t.label} · DEF ${t.value}" else "${t.label} · ${t.value}"
    Box(
        modifier = Modifier
            .bouncyClick(onClick)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(colors))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) { Text(label, color = Color.White, fontWeight = FontWeight.Bold) }
}

/** Panggung pertarungan. Pahlawan dan monster digambar kode, dan sengaja TANPA mata, hidung, atau mulut. */
@Composable
private fun BattleStage(fx: BattleFx, level: LevelConfig, shield: Int, modifier: Modifier = Modifier) {
    val heroRun = remember { Animatable(0f) }
    val arrow = remember { Animatable(0f) }
    val glow = remember { Animatable(0f) }
    val lunge = remember { Animatable(0f) }
    val hit = remember { Animatable(0f) }
    var holding by remember { mutableStateOf<ActionType?>(null) }

    LaunchedEffect(fx.id) {
        if (fx.id == 0) return@LaunchedEffect
        holding = fx.hero
        if (fx.correct) {
            when (fx.hero) {
                ActionType.SWORD -> {
                    heroRun.animateTo(1f, tween(260))
                    hit.snapTo(1f)
                    hit.animateTo(0f, tween(260))
                    heroRun.animateTo(0f, tween(240))
                }
                ActionType.ARROW -> {
                    arrow.snapTo(0.001f)
                    arrow.animateTo(1f, tween(380, easing = LinearEasing))
                    hit.snapTo(1f)
                    hit.animateTo(0f, tween(260))
                    arrow.snapTo(0f)
                }
                ActionType.ARMOR -> {
                    glow.snapTo(1f)
                    glow.animateTo(0f, tween(700))
                }
                null -> {}
            }
        }
        if (fx.monsterAttacks) {
            lunge.animateTo(1f, tween(220))
            lunge.animateTo(0f, tween(220))
        }
    }

    Canvas(modifier.clip(RoundedCornerShape(24.dp))) {
        val w = size.width
        val h = size.height
        drawRect(Brush.verticalGradient(listOf(Color(0xFF81D4FA), Color(0xFFE1F5FE))), size = Size(w, h * 0.78f))
        drawRect(Color(0xFF66BB6A), topLeft = Offset(0f, h * 0.78f), size = Size(w, h * 0.22f))
        drawCircle(Color(0xFFFFEE58), radius = h * 0.09f, center = Offset(w * 0.12f, h * 0.16f))
        val u = h / 12f
        val ground = h * 0.86f
        val heroX = w * 0.2f + heroRun.value * w * 0.42f
        drawHero(heroX, ground, u, holding, glow.value, shield > 0)
        if (arrow.value > 0f) {
            val startX = w * 0.2f + 3f * u
            val ax = startX + arrow.value * (w * 0.62f - startX)
            val ay = ground - 5.2f * u
            drawLine(Color(0xFF5D4037), Offset(ax, ay), Offset(ax + 2f * u, ay), 0.3f * u, StrokeCap.Round)
            drawLine(Color(0xFF37474F), Offset(ax + 2f * u, ay), Offset(ax + 2.6f * u, ay), 0.5f * u, StrokeCap.Round)
        }
        val monsterX = w * 0.78f - lunge.value * w * 0.2f
        drawMonster(monsterX, ground, u, Color(level.monsterColor), hit.value)
    }
}

private fun DrawScope.drawHero(cx: Float, ground: Float, u: Float, holding: ActionType?, glow: Float, hasShield: Boolean) {
    val skin = Color(0xFFFFCC80)
    val legW = 1.1f * u
    val legH = 2.6f * u
    val bodyW = 2.6f * u
    val bodyH = 3f * u
    val head = 2.3f * u
    drawRoundRect(Color(0xFF3949AB), Offset(cx - bodyW * 0.45f, ground - legH), Size(legW, legH), CornerRadius(u * 0.3f))
    drawRoundRect(Color(0xFF3949AB), Offset(cx + bodyW * 0.45f - legW, ground - legH), Size(legW, legH), CornerRadius(u * 0.3f))
    val bodyTop = ground - legH - bodyH
    drawRoundRect(Color(0xFF29B6F6), Offset(cx - bodyW / 2, bodyTop), Size(bodyW, bodyH), CornerRadius(u * 0.5f))
    drawRoundRect(skin, Offset(cx - bodyW / 2 - 0.9f * u, bodyTop + 0.3f * u), Size(0.9f * u, 2.2f * u), CornerRadius(u * 0.3f))
    drawRoundRect(skin, Offset(cx + bodyW / 2, bodyTop + 0.3f * u), Size(0.9f * u, 2.2f * u), CornerRadius(u * 0.3f))
    val headTop = bodyTop - head - 0.1f * u
    // Kepala polos: tanpa mata, hidung, atau mulut.
    drawRoundRect(skin, Offset(cx - head / 2, headTop), Size(head, head), CornerRadius(u * 0.7f))
    drawRoundRect(Color(0xFF6D4C41), Offset(cx - head / 2, headTop), Size(head, head * 0.38f), CornerRadius(u * 0.6f))
    when (holding) {
        ActionType.ARROW -> {
            drawArc(
                Color(0xFF8D6E63), -70f, 140f, false,
                Offset(cx + bodyW / 2 + 0.2f * u, bodyTop - 0.4f * u), Size(2f * u, 4.4f * u),
                style = Stroke(0.35f * u, cap = StrokeCap.Round)
            )
        }
        ActionType.SWORD -> {
            drawLine(
                Color(0xFFCFD8DC), Offset(cx + bodyW / 2 + 0.6f * u, bodyTop + 1.2f * u),
                Offset(cx + bodyW / 2 + 3.6f * u, bodyTop - 1.6f * u), 0.55f * u, StrokeCap.Round
            )
            drawLine(
                Color(0xFF8D6E63), Offset(cx + bodyW / 2 + 0.3f * u, bodyTop + 1.6f * u),
                Offset(cx + bodyW / 2 + 1.1f * u, bodyTop + 0.8f * u), 0.5f * u, StrokeCap.Round
            )
        }
        else -> {}
    }
    if (glow > 0f || hasShield) {
        drawCircle(
            Color(0xFF80D8FF).copy(alpha = 0.25f + 0.5f * glow),
            radius = 4.4f * u,
            center = Offset(cx, bodyTop + bodyH / 2),
            style = Stroke(0.35f * u)
        )
    }
}

private fun DrawScope.drawMonster(cx: Float, ground: Float, u: Float, body: Color, hit: Float) {
    val c = if (hit > 0f) lerp(body, Color.White, 0.65f * hit) else body
    val dark = lerp(c, Color.Black, 0.3f)
    val cy = ground - 3.4f * u
    for (side in listOf(-1f, 1f)) {
        for (i in 0 until 4) {
            val y1 = cy - 0.9f * u + i * 0.6f * u
            val kneeX = cx + side * (3.4f * u + i * 0.1f * u)
            val kneeY = y1 - 1.7f * u
            val footX = cx + side * (4.8f * u + i * 0.3f * u)
            val footY = ground - 0.1f * u
            drawLine(dark, Offset(cx + side * 1.4f * u, y1), Offset(kneeX, kneeY), 0.45f * u, StrokeCap.Round)
            drawLine(dark, Offset(kneeX, kneeY), Offset(footX, footY), 0.4f * u, StrokeCap.Round)
        }
    }
    // Badan dan kepala-dada polos: tanpa mata, hidung, atau mulut.
    drawOval(c, Offset(cx - 2.2f * u, cy - 1.8f * u), Size(4.4f * u, 3.6f * u))
    drawOval(dark, Offset(cx - 3.1f * u, cy - 0.9f * u), Size(2.2f * u, 1.9f * u))
    drawArc(dark, 200f, 140f, false, Offset(cx - 1.6f * u, cy - 1.4f * u), Size(3.2f * u, 2.8f * u), style = Stroke(0.25f * u))
}
