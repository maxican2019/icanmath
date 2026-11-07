package com.icanmath.app.ui

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
fun HomeScreen(vm: AppViewModel, nav: NavHostController) {
    val data by vm.data.collectAsState()
    val restore = rememberRestoreAction(vm)
    val transition = rememberInfiniteTransition(label = "bob")
    val bob by transition.animateFloat(
        initialValue = -14f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bobValue"
    )
    val symbolColors = listOf(Color(0xFF7986CB), Color(0xFF4DB6AC), Color(0xFFFFB74D), Color(0xFFE57373))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Indigo, IndigoDark)))
            .systemBarsPadding()
    ) {
        // Hiasan simbol besar berwarna samar di latar belakang.
        Text("÷", fontSize = 120.sp, color = Color.White.copy(alpha = 0.10f),
            modifier = Modifier.align(Alignment.TopEnd).offset(x = 16.dp, y = (40 + bob).dp))
        Text("×", fontSize = 100.sp, color = Color.White.copy(alpha = 0.10f),
            modifier = Modifier.align(Alignment.BottomStart).offset(x = (-8).dp, y = (-40 - bob).dp))
        Text("+", fontSize = 90.sp, color = Color.White.copy(alpha = 0.08f),
            modifier = Modifier.align(Alignment.CenterStart).offset(x = (-20).dp, y = (-120 + bob).dp))

        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                listOf("+", "−", "×", "÷").forEachIndexed { i, symbol ->
                    val dy = if (i % 2 == 0) bob else -bob
                    Box(
                        modifier = Modifier
                            .offset(y = dy.dp)
                            .size(68.dp)
                            .background(symbolColors[i], CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(symbol, fontSize = 34.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
            Text("icanmath", fontSize = 44.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(6.dp))
            Text(
                "Belajar berhitung jadi permainan seru",
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(44.dp))
            Button(
                onClick = {
                    if (data.profiles.isEmpty()) nav.navigate("profileform") else nav.navigate("dashboard")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Indigo),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth().height(58.dp)
            ) { Text("Masuk", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { nav.navigate("settings") },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) { Text("Setting", fontSize = 16.sp) }
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = restore,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
            ) { Text("Pulihkan dari file cadangan") }
        }
    }
}

@Composable
fun ProfileFormScreen(vm: AppViewModel, nav: NavHostController) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableIntStateOf(8) }
    var grade by remember { mutableIntStateOf(2) }
    var error by remember { mutableStateOf(false) }

    AppScaffold("Profil Baru", { nav.popBackStack() }) {
        Text("Data hanya tersimpan di perangkat ini. Tanpa akun.")
        OutlinedTextField(
            value = name,
            onValueChange = { name = it.take(20); error = false },
            label = { Text("Nama panggilan") },
            singleLine = true,
            isError = error,
            supportingText = { if (error) Text("Nama wajib diisi") },
            modifier = Modifier.fillMaxWidth()
        )
        Text("Umur", fontWeight = FontWeight.SemiBold)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = { if (age > 5) age-- }) { Text("−") }
            Text("$age tahun", style = MaterialTheme.typography.titleLarge)
            OutlinedButton(onClick = { if (age < 20) age++ }) { Text("+") }
        }
        Text("Kelas", fontWeight = FontWeight.SemiBold)
        GradeChips(grade) { grade = it }
        Button(
            onClick = {
                if (name.isBlank()) {
                    error = true
                } else {
                    vm.addProfile(name.trim(), age, grade)
                    nav.navigate("dashboard") { popUpTo("home") }
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) { Text("Simpan") }
    }
}

private class MenuItem(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val tint: Color,
    val route: String
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
                            Avatar(p.name)
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
                        Text(
                            "🔥 $streak hari",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            color = Color(0xFF7A3E00),
                            fontWeight = FontWeight.Bold
                        )
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
                    Text("🎯", fontSize = 64.sp, modifier = Modifier.align(Alignment.TopEnd))
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
                    MenuItem("📝", "Latihan Soal", "Pilih kelas dan jenis soal", Color(0xFF3949AB), "classselect"),
                    MenuItem("🧩", "Latihan Logika", "Puzzle dan permainan", Color(0xFF00796B), "logicmenu"),
                    MenuItem("🖨️", "Cetak PDF", "Lembar soal untuk dicetak", Color(0xFFB7791F), "print"),
                    MenuItem("🏅", "Medali Saya", "Koleksi dan profil", Color(0xFFC62828), "profile"),
                    MenuItem("👨‍👩‍👧", "Tambah Profil", "Untuk adik atau kakak", Color(0xFF6A1B9A), "profileform"),
                    MenuItem("⚙️", "Pengaturan", "Cadangan dan data", Color(0xFF546E7A), "settings")
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
                                Text(medal, fontSize = 20.sp)
                                Avatar(profile.name, 32)
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
                            Avatar(pr.name, 44)
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
            ) { Text(m.emoji, fontSize = 28.sp) }
            Column {
                Text(m.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(m.subtitle, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
