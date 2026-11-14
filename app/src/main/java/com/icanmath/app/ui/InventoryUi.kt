package com.icanmath.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.icanmath.app.inventory.Inventory
import com.icanmath.app.inventory.InventoryHolder
import com.icanmath.app.inventory.ItemArt
import com.icanmath.app.inventory.ItemCatalog
import com.icanmath.app.inventory.ItemDef

/** Mengambil Inventory bersama. Semua game memakai ini, tidak pernah menyentuh penyimpanan langsung. */
@Composable
fun rememberInventory(): Inventory {
    val ctx = LocalContext.current
    return remember { InventoryHolder.get(ctx) }
}

/** Ikon item: PNG dari slot jika ada, jika tidak digambar kode (tanpa wajah dan tanpa emoji). */
@Composable
fun ItemIcon(item: ItemDef, size: Dp) {
    SlotImage(item.slot, Modifier.size(size)) {
        Canvas(Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            when (item.art) {
                ItemArt.LOG -> {
                    drawRoundRect(Color(0xFF8D5A2B), Offset(w * 0.08f, h * 0.26f), Size(w * 0.84f, h * 0.48f), CornerRadius(h * 0.14f))
                    drawRoundRect(Color(0xFFA9763F), Offset(w * 0.12f, h * 0.34f), Size(w * 0.6f, h * 0.1f), CornerRadius(h * 0.05f))
                    drawCircle(Color(0xFFE0B97A), radius = h * 0.24f, center = Offset(w * 0.8f, h * 0.5f))
                    drawCircle(Color(0xFF8D5A2B), radius = h * 0.15f, center = Offset(w * 0.8f, h * 0.5f), style = Stroke(h * 0.04f))
                    drawCircle(Color(0xFF8D5A2B), radius = h * 0.06f, center = Offset(w * 0.8f, h * 0.5f), style = Stroke(h * 0.03f))
                }
                ItemArt.IRON -> {
                    drawRoundRect(Color(0xFF546E7A), Offset(w * 0.1f, h * 0.3f), Size(w * 0.8f, h * 0.44f), CornerRadius(h * 0.1f))
                    drawRoundRect(Color(0xFF90A4AE), Offset(w * 0.16f, h * 0.36f), Size(w * 0.68f, h * 0.14f), CornerRadius(h * 0.06f))
                    drawLine(Color(0xFFECEFF1), Offset(w * 0.22f, h * 0.43f), Offset(w * 0.5f, h * 0.43f), h * 0.04f, StrokeCap.Round)
                }
                ItemArt.ORB -> {
                    drawCircle(
                        Brush.radialGradient(
                            listOf(Color(0xFFE1BEE7), Color(0xFF9C27B0), Color(0xFF4A148C)),
                            center = Offset(w * 0.4f, h * 0.38f),
                            radius = w * 0.6f
                        ),
                        radius = w * 0.4f,
                        center = Offset(w * 0.5f, h * 0.5f)
                    )
                    drawCircle(Color.White.copy(alpha = 0.7f), radius = w * 0.07f, center = Offset(w * 0.36f, h * 0.34f))
                }
            }
        }
    }
}

/** Bar inventory ringkas (ikon + jumlah). Otomatis diperbarui saat stok berubah. */
@Composable
fun InventoryBar(modifier: Modifier = Modifier) {
    val inventory = rememberInventory()
    val stock by inventory.items.collectAsState()
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ItemCatalog.items.forEach { def ->
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ItemIcon(def, 26.dp)
                    Text("${stock[def.id] ?: 0}", fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

/** Layar Tas: seluruh item dan jumlahnya, dipakai bersama oleh semua game. */
@Composable
fun InventoryScreen(nav: NavHostController) {
    val inventory = rememberInventory()
    val stock by inventory.items.collectAsState()
    FunScaffold(
        "Tas Saya", { nav.popBackStack() },
        subtitle = "Bahan hasil farming dari semua game",
        accent = Color(0xFF6D4C41), accent2 = Color(0xFF8D6E63),
        headerSlot = Slot.MENU_TAS, headerEmoji = "🎒"
    ) {
        val total = stock.values.sum()
        ItemCatalog.items.chunked(2).forEach { pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                pair.forEach { def ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ItemIcon(def, 64.dp)
                            Text(def.displayName, fontWeight = FontWeight.Bold)
                            Text("${stock[def.id] ?: 0}", fontSize = 28.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        Text(
            if (total == 0) "Tasmu masih kosong. Menangkan Math Battle untuk mendapat bahan!"
            else "Bahan ini bisa dipakai di game lain di Rumah Game.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
