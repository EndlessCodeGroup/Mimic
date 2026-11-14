// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.vanilla

import org.bukkit.entity.Player
import ru.endlesscode.mimic.level.LevelProgress
import ru.endlesscode.mimic.level.SingleTrackLevelProvider
import kotlin.math.roundToInt

/**
 * Vanilla experience bar as the single track `minecraft:level`.
 * Vanilla experience points are whole numbers, so experience amounts are rounded.
 */
internal class MinecraftLevelProvider : SingleTrackLevelProvider() {

    override val id: String = ID

    override fun getProgress(player: Player): LevelProgress {
        val expToNextLevel = player.expToLevel.toDouble()
        return LevelProgress.level(player.level)
            .minLevel(0)
            .exp(player.exp * expToNextLevel)
            .expToNextLevel(expToNextLevel)
            .build()
    }

    override fun setLevel(player: Player, level: Int): Boolean {
        player.level = level
        player.exp = 0f
        return true
    }

    override fun giveExp(player: Player, amount: Double): Boolean {
        player.giveExp(amount.roundToInt())
        return true
    }

    override fun takeExp(player: Player, amount: Double): Boolean {
        // Vanilla lowers levels when negative experience is given
        player.giveExp(-amount.roundToInt())
        return true
    }

    companion object {
        const val ID = "minecraft"
    }
}
