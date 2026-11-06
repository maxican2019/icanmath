package com.example.matematikaceria.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.matematikaceria.AppViewModel
import com.example.matematikaceria.Stats
import java.time.LocalDate

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
    val colors = listOf(Color(0xFF3949AB), Color(0xFF00796B), Color(0xFFB7791F), Color(0xFFC62828))

    Scaffold { pad ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pad).padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                listOf("+", "−", "×", "÷").forEachIndexed { i, symbol ->
                    val dy = if (i % 2 == 0) bob else -bob
                    Box(
                        modifier = Modifier
                            .offset(y = dy.dp)
                            .size(64.dp)
                            .background(colors[i], CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(symbol, fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(36.dp))
            Text("Matematika Ceria", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Belajar berhitung jadi seru", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(40.dp))
            Button(
                onClick = {
                    if (data.profiles.isEmpty()) nav.navigate("profileform") else nav.navigate("dashboard")
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) { Text("Masuk") }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { nav.navigate("settings") },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) { Text("Setting") }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = restore) { Text("Pulihkan dari file cadangan") }
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

@Composable
fun DashboardScreen(vm: AppViewModel, nav: NavHostController) {
    val data by vm.data.collectAsState()
    val p = data.profiles.firstOrNull { it.id == data.activeProfileId }

    AppScaffold("Beranda") {
        if (p == null) {
            Text("Belum ada profil.")
            Button(onClick = { nav.navigate("profileform") }) { Text("Buat profil") }
            return@AppScaffold
        }
        val today = LocalDate.now()
        val streak = Stats.currentStreak(Stats.dailyDates(data.sessions, p.id), today)
        val doneToday = data.sessions.any {
            it.profileId == p.id && it.kind == "DAILY" && it.date == today.toString()
        }
        val ranking = data.profiles
            .map { it to Stats.weeklyPoints(data.sessions, it.id, today) }
            .sortedByDescending { it.second }

        Text("Halo, ${p.name}!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("${Stats.gradeLabel(p.grade)} · ${p.age} tahun · Streak $streak hari")

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Latihan harian", style = MaterialTheme.typography.titleLarge)
                Text("5 soal campuran untuk ${Stats.gradeLabel(p.grade)}")
                Button(
                    enabled = !doneToday,
                    onClick = {
                        vm.startQuiz("DAILY", p.grade)
                        nav.navigate("quiz")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (doneToday) "Sudah selesai hari ini" else "Mulai latihan") }
            }
        }

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Ranking minggu ini", style = MaterialTheme.typography.titleMedium)
                ranking.forEachIndexed { i, (profile, pts) ->
                    val mark = if (profile.id == p.id) " (aktif)" else ""
                    Text("${i + 1}. ${profile.name}$mark: $pts poin")
                }
                if (ranking.size == 1) Text("Tambah profil untuk membandingkan.", style = MaterialTheme.typography.bodySmall)
            }
        }

        FilledTonalButton(onClick = { nav.navigate("classselect") }, modifier = Modifier.fillMaxWidth()) { Text("Latihan soal") }
        FilledTonalButton(onClick = { nav.navigate("logicmenu") }, modifier = Modifier.fillMaxWidth()) { Text("Latihan logika") }
        FilledTonalButton(onClick = { nav.navigate("print") }, modifier = Modifier.fillMaxWidth()) { Text("Cetak soal (PDF)") }
        FilledTonalButton(onClick = { nav.navigate("profile") }, modifier = Modifier.fillMaxWidth()) { Text("Profil dan medali") }
        OutlinedButton(onClick = { nav.navigate("settings") }, modifier = Modifier.fillMaxWidth()) { Text("Pengaturan") }
    }
}
