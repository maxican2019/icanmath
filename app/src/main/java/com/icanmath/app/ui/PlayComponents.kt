package com.icanmath.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class OptState { IDLE, SELECTED, CORRECT, WRONG, DIM }

private val GreenBg = Color(0xFFD7F2DC)
private val GreenFg = Color(0xFF1B5E20)
private val GreenAccent = Color(0xFF2E7D32)
private val RedBg = Color(0xFFFADBD8)
private val RedFg = Color(0xFF8C1D18)
private val RedAccent = Color(0xFFB3261E)

/** Kerangka layar bermain: tombol tutup, progress tebal, skor di kanan, aksi di bawah. */
@Composable
fun PlayScaffold(
    onClose: () -> Unit,
    progress: Float,
    accent: Color,
    topRight: String,
    bottom: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(bottomBar = bottom) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = onClose) { Text("✕", fontSize = 20.sp) }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.weight(1f).height(12.dp).clip(RoundedCornerShape(6.dp)),
                    color = accent
                )
                Text(topRight, fontWeight = FontWeight.Bold)
            }
            content()
            Box(Modifier.height(8.dp))
        }
    }
}

@Composable
fun BottomActionBar(label: String, enabled: Boolean, onClick: () -> Unit) {
    Surface(tonalElevation = 3.dp, shadowElevation = 6.dp) {
        Button(
            onClick = onClick,
            enabled = enabled,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp).height(56.dp)
        ) { Text(label, fontSize = 17.sp, fontWeight = FontWeight.Bold) }
    }
}

/** Kartu jawaban: berubah warna dan ikon (✓ atau ✗) setelah diperiksa, tidak hanya mengandalkan warna. */
@Composable
fun AnswerOption(
    letter: String,
    text: String,
    state: OptState,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    big: Boolean = false,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val container by animateColorAsState(
        when (state) {
            OptState.SELECTED -> scheme.primaryContainer
            OptState.CORRECT -> GreenBg
            OptState.WRONG -> RedBg
            else -> scheme.surface
        },
        label = "optBg"
    )
    val borderColor = when (state) {
        OptState.SELECTED -> scheme.primary
        OptState.CORRECT -> GreenAccent
        OptState.WRONG -> RedAccent
        else -> scheme.outlineVariant
    }
    val textColor = when (state) {
        OptState.SELECTED -> scheme.onPrimaryContainer
        OptState.CORRECT -> GreenFg
        OptState.WRONG -> RedFg
        else -> scheme.onSurface
    }
    val badgeColor = when (state) {
        OptState.SELECTED -> scheme.primary
        OptState.CORRECT -> GreenAccent
        OptState.WRONG -> RedAccent
        else -> scheme.surfaceVariant
    }
    val badgeText = when (state) {
        OptState.SELECTED -> scheme.onPrimary
        OptState.CORRECT, OptState.WRONG -> Color.White
        else -> scheme.onSurfaceVariant
    }
    val scale by animateFloatAsState(if (state == OptState.SELECTED) 1.03f else 1f, label = "optScale")
    val mark = when (state) {
        OptState.CORRECT -> "✓"
        OptState.WRONG -> "✗"
        else -> letter
    }
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        color = container,
        border = BorderStroke(2.dp, borderColor),
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
            alpha = if (state == OptState.DIM) 0.45f else 1f
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(Modifier.size(32.dp).background(badgeColor, CircleShape), contentAlignment = Alignment.Center) {
                Text(mark, color = badgeText, fontWeight = FontWeight.Bold)
            }
            Text(
                text,
                color = textColor,
                style = if (big) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Panel hasil di bawah layar: benar atau salah, cara menghitung, dan istilah singkat.
 * Tujuannya agar pengguna belajar dari setiap soal, bukan hanya diberi tahu benar atau salah.
 */
@Composable
fun FeedbackPanel(
    correct: Boolean,
    correctAnswer: String,
    explanation: String,
    tip: String,
    points: Int,
    nextLabel: String,
    onNext: () -> Unit
) {
    val bg = if (correct) GreenBg else RedBg
    val fg = if (correct) GreenFg else RedFg
    Surface(
        color = bg,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                if (correct) "✓ Benar! +$points poin" else "✗ Belum tepat",
                color = fg,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (!correct) Text("Jawaban yang benar: $correctAnswer", color = fg, fontWeight = FontWeight.SemiBold)
            Column(
                modifier = Modifier.heightIn(max = 280.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Cara menghitung", color = fg, fontWeight = FontWeight.Bold)
                Text(
                    if (explanation.isNotBlank()) explanation else "Jawaban yang benar adalah $correctAnswer.",
                    color = fg
                )
                if (tip.isNotBlank()) {
                    Text("Tahukah kamu?", color = fg, fontWeight = FontWeight.Bold)
                    Text(tip, color = fg)
                }
            }
            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(containerColor = fg, contentColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text(nextLabel, fontSize = 17.sp, fontWeight = FontWeight.Bold) }
        }
    }
}
