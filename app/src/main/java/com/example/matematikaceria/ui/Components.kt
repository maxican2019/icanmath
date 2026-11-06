package com.example.matematikaceria.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.matematikaceria.AppViewModel
import com.example.matematikaceria.Medal
import com.example.matematikaceria.Stats
import com.example.matematikaceria.Tier
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (onBack != null) TextButton(onClick = onBack) { Text("Kembali") }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GradeChips(selected: Int, onSelect: (Int) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (g in 1..12) {
            FilterChip(
                selected = selected == g,
                onClick = { onSelect(g) },
                label = { Text(Stats.gradeLabel(g)) }
            )
        }
    }
}

@Composable
fun TierBadge(tier: Tier) {
    val color = when (tier) {
        Tier.BRONZE -> Color(0xFF8A5A2B)
        Tier.SILVER -> Color(0xFF5F6B7A)
        Tier.GOLD -> Color(0xFFA8620A)
        Tier.NONE -> Color(0xFF757575)
    }
    Box(Modifier.size(44.dp).background(color, CircleShape), contentAlignment = Alignment.Center) {
        Text(tier.letter, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MedalRow(m: Medal) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TierBadge(m.tier)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(m.title, fontWeight = FontWeight.SemiBold)
            Text("${m.tier.label} · ${m.detail}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

fun shareFile(context: Context, file: File, mime: String, title: String) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mime
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, title))
}

/** Mengembalikan aksi yang membuka pemilih file lalu memulihkan data dari file cadangan. */
@Composable
fun rememberRestoreAction(vm: AppViewModel): () -> Unit {
    val ctx = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            val text = try {
                ctx.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            } catch (e: Exception) {
                null
            }
            val ok = text != null && vm.restore(text)
            Toast.makeText(
                ctx,
                if (ok) "Data berhasil dipulihkan" else "File cadangan tidak valid",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    return { launcher.launch(arrayOf("*/*")) }
}
