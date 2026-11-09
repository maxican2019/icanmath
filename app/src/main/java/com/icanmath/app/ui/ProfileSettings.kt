package com.icanmath.app.ui

import com.icanmath.app.Tier
import com.icanmath.app.Medal
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.icanmath.app.AppViewModel
import com.icanmath.app.Medals
import com.icanmath.app.PdfExporter
import com.icanmath.app.QuestionGenerator
import com.icanmath.app.Stats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import kotlin.random.Random

@Composable
private fun StatChip(emoji: String, value: String, label: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(emoji, fontSize = 28.sp)
            Column {
                Text(value, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                Text(label, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun MedalCard(m: Medal, modifier: Modifier = Modifier) {
    val earned = m.tier != Tier.NONE
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = if (earned) Color(0xFFFFF3D6) else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TierBadge(m.tier, 76)
            Text(
                m.title,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = if (earned) Color(0xFF5D4037) else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                m.tier.label,
                fontWeight = FontWeight.Bold,
                color = if (earned) Color(0xFF8A5A00) else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                m.detail,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = if (earned) Color(0xFF5D4037) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProfileScreen(vm: AppViewModel, nav: NavHostController) {
    val data by vm.data.collectAsState()
    val p = data.profiles.firstOrNull { it.id == data.activeProfileId }
    var deleteId by remember { mutableStateOf<String?>(null) }

    FunScaffold(
        "Medali Saya", { nav.popBackStack() },
        subtitle = if (p != null) "${p.name} · ${Stats.gradeLabel(p.grade)} · ${p.age} tahun" else null,
        accent = Color(0xFFB26A00), accent2 = Color(0xFFD48806),
        headerSlot = Slot.MENU_MEDALI, headerEmoji = "🏅"
    ) {
        if (p == null) {
            Text("Belum ada profil.")
            return@FunScaffold
        }
        val today = LocalDate.now()
        val streak = Stats.currentStreak(Stats.dailyDates(data.sessions, p.id), today)
        val weekly = Stats.weeklyPoints(data.sessions, p.id, today)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatChip("⭐", "$weekly poin", "minggu ini", Modifier.weight(1f))
            StatChip("🔥", "$streak hari", "streak", Modifier.weight(1f))
        }

        Text("Avatar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        AvatarPicker(p.avatar) { vm.setAvatar(p.id, it) }

        Text("Medali latihan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Medals.practice(data.sessions, p.id).chunked(2).forEach { pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                pair.forEach { MedalCard(it, Modifier.weight(1f)) }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        Text("Medali logika", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Medals.logic(data.logicPlays, p.id).chunked(2).forEach { pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                pair.forEach { MedalCard(it, Modifier.weight(1f)) }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        if (data.profiles.size > 1) {
            HorizontalDivider()
            Text("Ganti profil", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            data.profiles.forEach { pr ->
                Row(
                    Modifier.fillMaxWidth().clickable { vm.setActive(pr.id) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RadioButton(selected = pr.id == p.id, onClick = { vm.setActive(pr.id) })
                    Avatar(pr.name, 36, pr.avatar)
                    Text("${pr.name} · ${Stats.gradeLabel(pr.grade)}")
                }
            }
        }
        FunButton("+ Tambah profil", Color(0xFF6A1B9A), Color(0xFF8E24AA)) { nav.navigate("profileform") }
        TextButton(onClick = { deleteId = p.id }) { Text("Hapus profil ini") }
    }

    deleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { deleteId = null },
            title = { Text("Hapus profil?") },
            text = { Text("Riwayat dan medali profil ini ikut terhapus.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteProfile(id)
                    deleteId = null
                    if (vm.data.value.profiles.isEmpty()) {
                        nav.navigate("home") { popUpTo("home") { inclusive = true } }
                    }
                }) { Text("Hapus") }
            },
            dismissButton = { TextButton(onClick = { deleteId = null }) { Text("Batal") } }
        )
    }
}

@Composable
fun SettingsScreen(vm: AppViewModel, nav: NavHostController) {
    val ctx = LocalContext.current
    val data by vm.data.collectAsState()
    val restore = rememberRestoreAction(vm)
    var confirmRestore by remember { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }

    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            val ok = try {
                ctx.contentResolver.openOutputStream(uri)?.use { it.write(vm.backupJson().toByteArray()) }
                true
            } catch (e: Exception) {
                false
            }
            Toast.makeText(ctx, if (ok) "Cadangan tersimpan" else "Gagal menyimpan", Toast.LENGTH_SHORT).show()
        }
    }

    FunScaffold(
        "Pengaturan", { nav.popBackStack() },
        subtitle = "Cadangan dan data",
        accent = Color(0xFF37474F), accent2 = Color(0xFF546E7A),
        headerSlot = Slot.MENU_SETTING, headerEmoji = "⚙️"
    ) {
        Text("Cadangan data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("ID backup: ${data.backupId.take(8)}")
        Text("File cadangan berisi semua profil, riwayat, dan medali. Bagikan file ini lalu pulihkan di perangkat baru. Tanpa enkripsi, simpan di tempat aman.")
        Button(
            onClick = { saveLauncher.launch("icanmath-backup.json") },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Simpan cadangan ke perangkat") }
        OutlinedButton(
            onClick = {
                val dir = File(ctx.cacheDir, "backup").apply { mkdirs() }
                val f = File(dir, "icanmath-backup.json")
                f.writeText(vm.backupJson())
                shareFile(ctx, f, "application/json", "Bagikan cadangan")
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Bagikan file cadangan") }
        OutlinedButton(onClick = { confirmRestore = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Pulihkan dari file")
        }
        HorizontalDivider()
        Text("Profil", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        data.profiles.forEach { Text("${it.name} · ${Stats.gradeLabel(it.grade)} · ${it.age} tahun") }
        OutlinedButton(onClick = { nav.navigate("profileform") }, modifier = Modifier.fillMaxWidth()) {
            Text("Tambah profil")
        }
        HorizontalDivider()
        OutlinedButton(onClick = { confirmReset = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Hapus semua data")
        }
    }

    if (confirmRestore) {
        AlertDialog(
            onDismissRequest = { confirmRestore = false },
            title = { Text("Pulihkan data?") },
            text = { Text("Data di perangkat ini akan ditimpa isi file cadangan.") },
            confirmButton = { TextButton(onClick = { confirmRestore = false; restore() }) { Text("Pilih file") } },
            dismissButton = { TextButton(onClick = { confirmRestore = false }) { Text("Batal") } }
        )
    }
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Hapus semua data?") },
            text = { Text("Semua profil, riwayat, dan medali dihapus dan tidak bisa dipulihkan tanpa file cadangan.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmReset = false
                    vm.resetAll()
                    nav.navigate("home") { popUpTo("home") { inclusive = true } }
                }) { Text("Hapus") }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Batal") } }
        )
    }
}

@Composable
fun PrintScreen(vm: AppViewModel, nav: NavHostController) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var grade by remember { mutableIntStateOf(vm.activeProfile()?.grade ?: 3) }
    var count by remember { mutableIntStateOf(10) }
    var school by remember { mutableStateOf("") }
    var withKey by remember { mutableStateOf(true) }
    var busy by remember { mutableStateOf(false) }

    FunScaffold(
        "Cetak Soal", { nav.popBackStack() },
        subtitle = "Buat lembar soal PDF",
        accent = Color(0xFF8D5A00), accent2 = Color(0xFFB7791F),
        headerSlot = Slot.MENU_CETAK, headerEmoji = "🖨️"
    ) {
        Text("Kelas", fontWeight = FontWeight.SemiBold)
        GradeChips(grade) { grade = it }
        Text("Jumlah soal", fontWeight = FontWeight.SemiBold)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = { if (count > 5) count -= 5 }) { Text("−") }
            Text("$count soal", style = MaterialTheme.typography.titleLarge)
            OutlinedButton(onClick = { if (count < 30) count += 5 }) { Text("+") }
        }
        OutlinedTextField(
            value = school,
            onValueChange = { school = it.take(40) },
            label = { Text("Nama sekolah (opsional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = withKey, onCheckedChange = { withKey = it })
            Spacer(Modifier.width(8.dp))
            Text("Sertakan kunci jawaban (halaman terpisah)")
        }
        Button(
            enabled = !busy,
            onClick = {
                busy = true
                scope.launch {
                    try {
                        val qs = QuestionGenerator.generate(grade, count, Random(System.currentTimeMillis()))
                        val file = withContext(Dispatchers.IO) {
                            PdfExporter.createWorksheet(ctx, school, grade, qs, withKey)
                        }
                        shareFile(ctx, file, "application/pdf", "Bagikan atau cetak PDF")
                    } catch (e: Exception) {
                        Toast.makeText(ctx, "Gagal membuat PDF", Toast.LENGTH_SHORT).show()
                    } finally {
                        busy = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) { Text(if (busy) "Membuat PDF..." else "Buat PDF") }
        Text(
            "Dari menu bagikan, pilih Cetak, atau simpan ke Drive atau Files.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
