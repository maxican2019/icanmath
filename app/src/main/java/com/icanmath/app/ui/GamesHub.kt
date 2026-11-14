package com.icanmath.app.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.icanmath.app.battle.BattleProgress

/** Definisi satu game di Rumah Game. [route] null berarti belum tersedia. */
class GameDef(
    val id: String,
    val title: String,
    val subtitle: String,
    val emoji: String,
    val slot: String,
    val route: String?,
    val c1: Color,
    val c2: Color
)

/** Daftar game. Menambah game baru cukup menambah satu entri di sini dan satu rute di AppNav. */
object GameRegistry {
    val games: List<GameDef> = listOf(
        GameDef(
            "math_battle", "Math Battle", "Lawan monster dengan matematika!", "⚔️", Slot.GAME_MATH_BATTLE,
            "battle", Color(0xFFC62828), Color(0xFF8E24AA)
        ),
        GameDef(
            "soon_1", "Rumah Game 2", "Segera hadir, pakai bahan dari tasmu!", "🏠", Slot.LOGIC_OTHER,
            null, Color(0xFF78909C), Color(0xFF90A4AE)
        ),
        GameDef(
            "soon_2", "Rumah Game 3", "Segera hadir, pakai bahan dari tasmu!", "🏠", Slot.LOGIC_OTHER,
            null, Color(0xFF78909C), Color(0xFF90A4AE)
        )
    )
}

/** Rumah Game: pintu masuk ke Math Battle dan game lain. Dibuka dari Latihan Logika, Permainan lain. */
@Composable
fun GamesHubScreen(nav: NavHostController) {
    val ctx = LocalContext.current
    val level = remember { BattleProgress(ctx).level }
    FunScaffold(
        "Rumah Game", { nav.popBackStack() },
        subtitle = "Main game seru, kumpulkan bahan!",
        accent = Color(0xFF00695C), accent2 = Color(0xFF2E7D32),
        headerSlot = Slot.LOGIC_OTHER, headerEmoji = "🎮"
    ) {
        InventoryBar()
        GameRegistry.games.forEachIndexed { i, g ->
            FunCard(
                g.title, g.subtitle, g.emoji, g.slot, g.c1, g.c2,
                index = i,
                badge = if (g.id == "math_battle") "Level $level" else "Segera hadir",
                enabled = g.route != null
            ) { g.route?.let { nav.navigate(it) } }
        }
        Text("Bahan dari tasmu juga akan dipakai di game lain nanti.")
    }
}
