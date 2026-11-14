package com.icanmath.app.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

/** Efek "membal" saat ditekan, supaya tombol terasa hidup seperti di game. */
@Composable
fun Modifier.bouncyClick(onClick: () -> Unit): Modifier {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "press"
    )
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(interactionSource = source, indication = null, onClick = onClick)
}

/** Muncul satu per satu dengan efek membal. [index] menentukan urutan. */
@Composable
fun Modifier.popIn(index: Int): Modifier {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(70L * index)
        shown = true
    }
    val scale by animateFloatAsState(
        targetValue = if (shown) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "popScale"
    )
    val a by animateFloatAsState(targetValue = if (shown) 1f else 0f, animationSpec = tween(220), label = "popAlpha")
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
        alpha = a
    }
}

/** Kerangka layar ramah anak: header berwarna melengkung dengan gambar, isi di bawahnya. */
@Composable
fun FunScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    subtitle: String? = null,
    accent: Color = Color(0xFF3949AB),
    accent2: Color = Color(0xFF5E35B1),
    headerSlot: String? = null,
    headerEmoji: String = "",
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold { pad ->
        Column(Modifier.fillMaxSize().padding(bottom = pad.calculateBottomPadding())) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Brush.linearGradient(listOf(accent, accent2)))
            ) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 40.dp, y = (-30).dp)
                        .size(150.dp)
                        .background(Color.White.copy(alpha = 0.10f), CircleShape)
                )
                Box(
                    Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = (-30).dp, y = 30.dp)
                        .size(110.dp)
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = pad.calculateTopPadding())
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (onBack != null) {
                        Box(
                            Modifier.size(44.dp).bouncyClick(onBack).background(Color.White.copy(alpha = 0.22f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) { Text("←", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
                    }
                    Column(Modifier.weight(1f)) {
                        Text(title, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                        if (subtitle != null) Text(subtitle, color = Color.White.copy(alpha = 0.92f), style = MaterialTheme.typography.bodyMedium)
                    }
                    if (headerSlot != null) {
                        SlotImage(headerSlot, Modifier.size(68.dp)) { Text(headerEmoji, fontSize = 44.sp) }
                    }
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                content = content
            )
        }
    }
}

/** Kartu pilihan besar berwarna dengan gambar di kanan; dipakai untuk submenu. */
@Composable
fun FunCard(
    title: String,
    subtitle: String,
    emoji: String,
    slot: String,
    c1: Color,
    c2: Color,
    index: Int = 0,
    badge: String? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(26.dp)
    val colors = if (enabled) listOf(c1, c2) else listOf(Color(0xFF78909C), Color(0xFF90A4AE))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .popIn(index)
            .then(if (enabled) Modifier.bouncyClick(onClick) else Modifier)
            .shadow(if (enabled) 8.dp else 0.dp, shape)
            .clip(shape)
            .background(Brush.linearGradient(colors))
    ) {
        Box(
            Modifier.align(Alignment.TopEnd).offset(x = 30.dp, y = (-30).dp).size(120.dp)
                .background(Color.White.copy(alpha = 0.12f), CircleShape)
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Text(subtitle, color = Color.White.copy(alpha = 0.95f), style = MaterialTheme.typography.bodyMedium)
                if (badge != null) {
                    Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.25f)) {
                        Text(
                            badge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            SlotImage(slot, Modifier.size(88.dp)) { Text(emoji, fontSize = 60.sp) }
        }
    }
}

/** Kotak kelas bergaya level game. */
@Composable
fun GradeTile(
    grade: Int,
    isMine: Boolean,
    c1: Color,
    c2: Color,
    index: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .popIn(index)
            .bouncyClick(onClick)
            .shadow(6.dp, shape)
            .clip(shape)
            .background(Brush.linearGradient(listOf(c1, c2))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Kelas", color = Color.White.copy(alpha = 0.95f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text("$grade", color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Black)
        }
        if (isMine) {
            Surface(
                modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFFD54F)
            ) {
                Text(
                    "⭐ kamu",
                    fontSize = 11.sp,
                    color = Color(0xFF5D4037),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun LevelTab(
    label: String,
    emoji: String,
    slot: String,
    selected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    val bg = if (selected) color else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = modifier.bouncyClick(onClick).clip(shape).background(bg).padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(38.dp).background(Color.White.copy(alpha = 0.92f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            SlotImage(slot, Modifier.size(32.dp)) { Text(emoji, fontSize = 22.sp) }
        }
        Spacer(Modifier.width(6.dp))
        Text(label, color = fg, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun FunButton(
    label: String,
    c1: Color,
    c2: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    val colors = if (enabled) listOf(c1, c2) else listOf(Color(0xFF9E9E9E), Color(0xFFBDBDBD))
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .then(if (enabled) Modifier.bouncyClick(onClick) else Modifier)
            .shadow(if (enabled) 6.dp else 0.dp, shape)
            .clip(shape)
            .background(Brush.horizontalGradient(colors)),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
    }
}

/** Baris pilihan avatar 1..6. */
@Composable
fun AvatarPicker(selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        for (n in 1..6) {
            val on = n == selected
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .bouncyClick { onSelect(n) }
                    .then(if (on) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
                    .padding(4.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) { Avatar("?", 58, n) }
        }
    }
}

private class StarDot(val x: Float, val y: Float, val r: Float, val phase: Float)

/** Latar luar angkasa: gradasi biru tua, pelangi, planet, dan bintang yang berkelip. */
@Composable
fun SpaceBackground(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "space")
    val twinkle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Restart),
        label = "twinkle"
    )
    val stars = remember {
        val r = Random(7)
        List(80) { StarDot(r.nextFloat(), r.nextFloat(), 0.8f + r.nextFloat() * 2.2f, r.nextFloat()) }
    }
    val bands = listOf(
        Color(0xFFEF5350), Color(0xFFFFA726), Color(0xFFFFEE58),
        Color(0xFF66BB6A), Color(0xFF42A5F5), Color(0xFFAB47BC)
    )
    Canvas(modifier.fillMaxSize()) {
        drawRect(Brush.verticalGradient(listOf(Color(0xFF0B1161), Color(0xFF1A237E), Color(0xFF311B92))))
        val bw = size.width * 0.028f
        val cx = size.width * 0.5f
        val cy = size.height * 0.62f
        bands.forEachIndexed { i, c ->
            val r = size.width * 0.78f - i * bw
            drawArc(
                color = c.copy(alpha = 0.28f),
                startAngle = 200f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(cx - r, cy - r),
                size = Size(2 * r, 2 * r),
                style = Stroke(width = bw)
            )
        }
        val pc = Offset(size.width * 0.84f, size.height * 0.09f)
        val pr = size.width * 0.11f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFB388FF), Color(0xFF651FFF), Color(0xFF311B92)),
                center = Offset(pc.x - pr * 0.3f, pc.y - pr * 0.3f),
                radius = pr * 1.5f
            ),
            radius = pr,
            center = pc
        )
        stars.forEach { s ->
            val t = (twinkle + s.phase) % 1f
            val a = 0.25f + 0.75f * (if (t < 0.5f) t * 2f else (1f - t) * 2f)
            drawCircle(Color.White.copy(alpha = a), radius = s.r * density, center = Offset(s.x * size.width, s.y * size.height))
        }
    }
}
