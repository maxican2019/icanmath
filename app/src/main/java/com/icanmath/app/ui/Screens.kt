package com.icanmath.app.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.icanmath.app.AppViewModel
import com.icanmath.app.Stats
import java.time.LocalDate

private val Indigo = Color(0xFF3949AB)
private val IndigoDark = Color(0xFF1A237E)

@Composable
private fun BoxScope.FloatingSymbol(
    text: String,
    color: Color,
    x: Dp,
    y: Dp,
    size: Int,
    bob: Float,
    dir: Float,
    spin: Float,
    baseRotation: Float
) {
    Text(
        text,
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(x = x, y = y + (bob * dir * 12f).dp)
            .graphicsLayer { rotationZ = baseRotation + spin * dir },
        color = color,
        fontSize = size.sp,
        fontWeight = FontWeight.Black,
        style = TextStyle(shadow = Shadow(color = color.copy(alpha = 0.85f), offset = Offset.Zero, blurRadius = 30f))
    )
}

@Composable
fun HomeScreen(vm: AppViewModel, nav: NavHostController) {
    val data by vm.data.collectAsState()
    val restore = rememberRestoreAction(vm)
    val hasBg = rememberSlotExists(Slot.HOME_BG)
    val transition = rememberInfiniteTransition(label = "home")
    val bob by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bob"
    )
    val spin by transition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(3200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "spin"
    )

    Box(Modifier.fillMaxSize()) {
        // Latar: gambar kustom jika ada, jika tidak memakai latar luar angkasa buatan kode.
        SlotImage(Slot.HOME_BG, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) { SpaceBackground() }

        BoxWithConstraints(Modifier.fillMaxSize().systemBarsPadding()) {
            val w = maxWidth
            val h = maxHeight
            if (!hasBg) {
                FloatingSymbol("1", Color(0xFFB388FF), w * 0.10f, h * 0.07f, 72, bob, 1f, spin, -12f)
                FloatingSymbol("2", Color(0xFF4FC3F7), w * 0.46f, h * 0.03f, 70, bob, -1f, spin, 8f)
                FloatingSymbol("+", Color(0xFFFF5252), w * 0.28f, h * 0.15f, 88, bob, -1f, spin, 0f)
                FloatingSymbol("×", Color(0xFF69F0AE), w * 0.56f, h * 0.15f, 88, bob, 1f, spin, 0f)
                FloatingSymbol("1", Color(0xFFFFB74D), w * 0.04f, h * 0.26f, 88, bob, -1f, spin, -6f)
                FloatingSymbol("×", Color(0xFF40C4FF), w * 0.28f, h * 0.27f, 88, bob, 1f, spin, 0f)
                FloatingSymbol("÷", Color(0xFFFFD740), w * 0.56f, h * 0.27f, 88, bob, -1f, spin, 0f)
                FloatingSymbol("3", Color(0xFFFFA726), w * 0.80f, h * 0.24f, 80, bob, 1f, spin, 10f)
                FloatingSymbol("📐", Color(0xFFE040FB), w * 0.08f, h * 0.41f, 52, bob, 1f, spin, -8f)
                FloatingSymbol("🎲", Color(0xFF40C4FF), w * 0.42f, h * 0.42f, 52, bob, -1f, spin, 6f)
                FloatingSymbol("🧮", Color(0xFFFFAB40), w * 0.74f, h * 0.41f, 52, bob, 1f, spin, 10f)
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "icanmath",
                    fontSize = 48.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    style = TextStyle(shadow = Shadow(color = Color(0xFF80D8FF), offset = Offset.Zero, blurRadius = 32f))
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Belajar berhitung jadi permainan seru",
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(28.dp))
                FunButton("Masuk", Color(0xFFFFB300), Color(0xFFFF6F00)) {
                    if (data.profiles.isEmpty()) nav.navigate("profileform") else nav.navigate("dashboard")
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { nav.navigate("settings") },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) { Text("Setting", fontSize = 16.sp) }
                Spacer(Modifier.height(4.dp))
                TextButton(
                    onClick = restore,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                ) { Text("Pulihkan dari file cadangan") }
            }
        }
    }
}

@Composable
fun ProfileFormScreen(vm: AppViewModel, nav: NavHostController) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableIntStateOf(8) }
    var grade by remember { mutableIntStateOf(2) }
    var avatar by remember { mutableIntStateOf(1) }
    var error by remember { mutableStateOf(false) }

    FunScaffold(
        "Profil Baru", { nav.popBackStack() },
        subtitle = "Kenalan dulu, yuk!",
        accent = Color(0xFF6A1B9A), accent2 = Color(0xFF8E24AA),
        headerSlot = Slot.MENU_PROFIL, headerEmoji = "👨‍👩‍👧"
    ) {
        Text("Data hanya tersimpan di perangkat ini. Tanpa akun.")
        Text("Pilih avatar", fontWeight = FontWeight.Bold)
        AvatarPicker(avatar) { avatar = it }
        OutlinedTextField(
            value = name,
            onValueChange = { name = it.take(20); error = false },
            label = { Text("Nama panggilan") },
            singleLine = true,
            isError = error,
            supportingText = { if (error) Text("Nama wajib diisi") },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Text("Umur", fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = { if (age > 5) age-- }) { Text("−") }
            Text("$age tahun", style = MaterialTheme.typography.titleLarge)
            OutlinedButton(onClick = { if (age < 20) age++ }) { Text("+") }
        }
        Text("Kelas", fontWeight = FontWeight.Bold)
        GradeChips(grade) { grade = it }
        FunButton("Simpan", Color(0xFF6A1B9A), Color(0xFF8E24AA)) {
            if (name.isBlank()) {
                error = true
            } else {
                vm.addProfile(name.trim(), age, grade, avatar)
                nav.navigate("dashboard") { popUpTo("home") }
            }
        }
    }
}

private class MenuItem(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val tint: Color,
    val route: String,
    val slot: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(vm: AppViewModel, nav: NavHostController) {
    val data by vm.data.collectAsState()
    val p = data.profiles.firstOrNull { it.id == data.activeProfileId }
    var showSheet by remember { mutableStateOf(false) }

    Scaffold { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            if (p == null) {
                Text("Belum ada profil.")
                Button(onClick = { nav.navigate("profileform") }) { Text("Buat profil") }
            } else {
                val today = LocalDate.now()
                val streak = Stats.currentStreak(Stats.dailyDates(data.sessions, p.id), today)
                val doneToday = data.sessions.any {
                    it.profileId == p.id && it.kind == "DAILY" && it.date == today.toString()
                }
                val ranking = data.profiles
                    .map { it to Stats.weeklyPoints(data.sessions, it.id, today) }
                    .sortedByDescending { it.second }

                // Header: klik nama untuk mengganti profil.
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        onClick = { showSheet = true },
                        shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 6.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Avatar(p.name, avatar = p.avatar)
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(p.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("  ▾", style = MaterialTheme.typography.titleMedium)
                                }
                                Text(
                                    "${Stats.gradeLabel(p.grade)} · ${p.age} tahun",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFFFE8CC)) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SlotImage(Slot.STREAK, Modifier.size(22.dp)) { Text("🔥") }
                            Text("$streak hari", color = Color(0xFF7A3E00), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Kartu latihan harian.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.linearGradient(listOf(Indigo, Color(0xFF5C6BC0))))
                        .padding(20.dp)
                ) {
                    SlotImage(Slot.DAILY_HERO, Modifier.align(Alignment.TopEnd).size(76.dp)) {
                        Text("🎯", fontSize = 64.sp, modifier = Modifier.align(Alignment.TopEnd))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            if (doneToday) "Hebat, latihan hari ini selesai!" else "Latihan harian",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (doneToday) "Kembali lagi besok untuk menjaga streak $streak harimu."
                            else "5 soal campuran · sekitar 5 menit · ${Stats.gradeLabel(p.grade)}",
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(Modifier.height(6.dp))
                        Button(
                            enabled = !doneToday,
                            onClick = {
                                vm.startQuiz("DAILY", p.grade)
                                nav.navigate("quiz")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Indigo),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) { Text(if (doneToday) "Sudah selesai" else "Mulai sekarang", fontWeight = FontWeight.Bold) }
                    }
                }

                Text("Menu", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                val menu = listOf(
                    MenuItem("📝", "Latihan Soal", "Pilih kelas dan jenis soal", Color(0xFF3949AB), "classselect", Slot.MENU_SOAL),
                    MenuItem("🧩", "Latihan Logika", "Puzzle dan permainan", Color(0xFF00796B), "logicmenu", Slot.MENU_LOGIKA),
                    MenuItem("🖨️", "Cetak PDF", "Lembar soal untuk dicetak", Color(0xFFB7791F), "print", Slot.MENU_CETAK),
                    MenuItem("🏅", "Medali Saya", "Koleksi dan profil", Color(0xFFC62828), "profile", Slot.MENU_MEDALI),
                    MenuItem("👨‍👩‍👧", "Tambah Profil", "Untuk adik atau kakak", Color(0xFF6A1B9A), "profileform", Slot.MENU_PROFIL),
                    MenuItem("⚙️", "Pengaturan", "Cadangan dan data", Color(0xFF546E7A), "settings", Slot.MENU_SETTING)
                )
                menu.chunked(2).forEach { rowItems ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        rowItems.forEach { m ->
                            MenuTile(m, Modifier.weight(1f)) { nav.navigate(m.route) }
                        }
                    }
                }

                // Ranking lokal.
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🏆 Ranking minggu ini", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        ranking.forEachIndexed { i, (profile, pts) ->
                            val medal = when (i) { 0 -> "🥇"; 1 -> "🥈"; 2 -> "🥉"; else -> "${i + 1}." }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                val rankSlot = when (i) { 0 -> Slot.RANK_1; 1 -> Slot.RANK_2; 2 -> Slot.RANK_3; else -> "" }
                                SlotImage(rankSlot, Modifier.size(28.dp)) { Text(medal, fontSize = 20.sp) }
                                Avatar(profile.name, 32, profile.avatar)
                                Text(
                                    profile.name + if (profile.id == p.id) " (aktif)" else "",
                                    modifier = Modifier.weight(1f)
                                )
                                Text("$pts poin", fontWeight = FontWeight.Bold)
                            }
                        }
                        if (ranking.size == 1) {
                            Text("Tambah profil untuk membandingkan.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }

    if (showSheet && p != null) {
        ModalBottomSheet(onDismissRequest = { showSheet = false }) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Ganti profil", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                data.profiles.forEach { pr ->
                    val active = pr.id == p.id
                    Surface(
                        onClick = {
                            vm.setActive(pr.id)
                            showSheet = false
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = if (active) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Avatar(pr.name, 44, pr.avatar)
                            Column(Modifier.weight(1f)) {
                                Text(pr.name, fontWeight = FontWeight.Bold)
                                Text("${Stats.gradeLabel(pr.grade)} · ${pr.age} tahun", style = MaterialTheme.typography.bodySmall)
                            }
                            if (active) Text("✓ Aktif", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                OutlinedButton(
                    onClick = {
                        showSheet = false
                        nav.navigate("profileform")
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) { Text("+ Tambah profil") }
            }
        }
    }
}

@Composable
private fun MenuTile(m: MenuItem, modifier: Modifier, onClick: () -> Unit) {
    ElevatedCard(onClick = onClick, modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(52.dp).background(m.tint.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) { SlotImage(m.slot, Modifier.size(40.dp)) { Text(m.emoji, fontSize = 28.sp) } }
            Column {
                Text(m.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(m.subtitle, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
