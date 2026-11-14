package com.icanmath.app.battle

import android.content.Context

/** Menyimpan level Math Battle terakhir. Inventory TIDAK disimpan di sini; itu hanya lewat modul Inventory. */
class BattleProgress(context: Context) {
    private val prefs = context.getSharedPreferences("math_battle", Context.MODE_PRIVATE)

    var level: Int
        get() = prefs.getInt("level", 1).coerceIn(1, BattleRules.LEVELS)
        set(value) {
            prefs.edit().putInt("level", value.coerceIn(1, BattleRules.LEVELS)).apply()
        }
}
