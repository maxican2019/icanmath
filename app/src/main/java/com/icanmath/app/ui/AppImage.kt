package com.icanmath.app.ui

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource

/**
 * Daftar slot gambar. Setiap slot adalah nama berkas di app/src/main/res/drawable-nodpi/.
 * Timpa berkasnya dengan PNG Anda (nama sama), tanpa mengubah kode.
 * Jika berkas dihapus, aplikasi kembali memakai tampilan bawaan (emoji atau bentuk polos).
 * Katalog lengkap beserta ukuran ada di docs/IMAGE_SLOTS.md.
 */
object Slot {
    const val HOME_BG = "img_home_bg"
    const val ITEM_KAYU = "img_item_kayu"
    const val ITEM_BESI = "img_item_besi"
    const val ITEM_ORB = "img_item_orb_tarantula"
    const val GAME_MATH_BATTLE = "img_game_math_battle"
    const val MENU_TAS = "img_menu_tas"
    const val LEVEL_SD = "img_level_sd"
    const val LEVEL_SMP = "img_level_smp"
    const val LEVEL_SMA = "img_level_sma"
    const val MENU_SOAL = "img_menu_soal"
    const val MENU_LOGIKA = "img_menu_logika"
    const val MENU_CETAK = "img_menu_cetak"
    const val MENU_MEDALI = "img_menu_medali"
    const val MENU_PROFIL = "img_menu_profil"
    const val MENU_SETTING = "img_menu_setting"
    const val DAILY_HERO = "img_daily_hero"
    const val STREAK = "img_streak"
    const val RANK_1 = "img_rank_1"
    const val RANK_2 = "img_rank_2"
    const val RANK_3 = "img_rank_3"
    const val MODE_RANDOM = "img_mode_random"
    const val MODE_MCQ = "img_mode_mcq"
    const val MODE_STORY = "img_mode_story"
    const val LOGIC_PUZZLE = "img_logic_puzzle"
    const val LOGIC_COMPARE = "img_logic_compare"
    const val LOGIC_GROUP = "img_logic_group"
    const val LOGIC_OTHER = "img_logic_other"
    const val FRUIT_APPLE = "img_fruit_apple"
    const val FRUIT_BANANA = "img_fruit_banana"
    const val FRUIT_GRAPE = "img_fruit_grape"
    const val MEDAL_BRONZE = "img_medal_bronze"
    const val MEDAL_SILVER = "img_medal_silver"
    const val MEDAL_GOLD = "img_medal_gold"
    const val MEDAL_LOCKED = "img_medal_locked"
    const val STAR_ON = "img_star_on"
    const val STAR_OFF = "img_star_off"
    const val FEEDBACK_CORRECT = "img_feedback_correct"
    const val FEEDBACK_WRONG = "img_feedback_wrong"
    const val AVATAR_1 = "img_avatar_1"
    const val AVATAR_2 = "img_avatar_2"
    const val AVATAR_3 = "img_avatar_3"
    const val AVATAR_4 = "img_avatar_4"
    const val AVATAR_5 = "img_avatar_5"
    const val AVATAR_6 = "img_avatar_6"
}

/** Menampilkan gambar dari slot jika berkasnya ada; jika tidak, memanggil [fallback]. */
@Composable
fun SlotImage(
    name: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Fit,
    fallback: @Composable () -> Unit
) {
    val ctx = LocalContext.current
    val id = remember(name) { ctx.resources.getIdentifier(name, "drawable", ctx.packageName) }
    if (id != 0) {
        Image(
            painter = painterResource(id),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        fallback()
    }
}

/** Mengembalikan true jika berkas gambar untuk slot [name] ada. */
@Composable
fun rememberSlotExists(name: String): Boolean {
    val ctx = LocalContext.current
    return remember(name) { ctx.resources.getIdentifier(name, "drawable", ctx.packageName) != 0 }
}
