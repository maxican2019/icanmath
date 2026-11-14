package com.icanmath.app.battle

import com.icanmath.app.inventory.Inventory
import com.icanmath.app.inventory.ItemCatalog
import kotlin.random.Random

/** Hasil undian hadiah untuk layar kemenangan. [message] siap ditampilkan. */
class RewardResult(val entry: DropEntry?, val message: String)

object BattleRewards {
    /**
     * Mengundi hadiah SATU KALI untuk kemenangan di [level] dan langsung menambahkannya ke [inventory].
     * [roll] menghasilkan angka di [0, 100); bisa diganti pada tes.
     */
    fun claimVictory(
        level: Int,
        inventory: Inventory,
        table: DropTable = DropTable.default,
        roll: () -> Double = { Random.nextDouble() * 100.0 }
    ): RewardResult {
        if (!table.hasDrops(level)) {
            return RewardResult(null, "Level ini belum punya hadiah. Coba level 3, 6, 9, atau 10!")
        }
        val entry = table.rollWith(level, roll())
            ?: return RewardResult(null, "Belum beruntung")
        inventory.add(entry.item, entry.amount)
        val name = ItemCatalog.find(entry.item)?.displayName?.lowercase() ?: entry.item
        return RewardResult(entry, "Hadiah: ${entry.amount} $name!")
    }
}
