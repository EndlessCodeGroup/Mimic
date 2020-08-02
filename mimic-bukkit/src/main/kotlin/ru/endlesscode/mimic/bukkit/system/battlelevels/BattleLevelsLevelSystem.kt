/*
 * This file is part of BukkitMimic.
 * Copyright (C) 2018 Osip Fatkullin
 * Copyright (C) 2018 EndlessCode Group and contributors
 *
 * BukkitMimic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BukkitMimic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BukkitMimic.  If not, see <http://www.gnu.org/licenses/>.
 */
package ru.endlesscode.mimic.bukkit.system.battlelevels

import org.bukkit.entity.Player
import ru.endlesscode.mimic.bukkit.system.BukkitLevelSystem
import java.util.*
import kotlin.math.abs

/** Implementation of LevelSystem that uses BattleLevels. */
@registry.Subsystem(priority = registry.SubsystemPriority.NORMAL, classes = ["me.robin.battlelevels.api.BattleLevelsAPI"])
class BattleLevelsLevelSystem internal constructor(
    player: Player,
    private val battleLevelsApi: BattleLevelsApiWrapper
) : BukkitLevelSystem(BattleLevelsConverter.getInstance(battleLevelsApi), player) {

    companion object {
        const val TAG = "BattleLevels"

        @JvmField
        val FACTORY = Factory(TAG, ::BattleLevelsLevelSystem)
    }

    private constructor(player: Player) : this(player, BattleLevelsApiWrapper())

    override val name: String = TAG
    override val isEnabled: Boolean = true

    override var level: Int
        get() = battleLevelsApi.getLevel(playerUniqueId)
        set(value) {
            val delta = value - level
            if (delta < 0) {
                takeLevel(abs(delta))
            } else if (delta > 0) {
                giveLevel(delta)
            }
        }

    override var totalExp: Double
        get() = battleLevelsApi.getScore(playerUniqueId)
        set(value) {
            val delta = value - totalExp
            if (delta < 0) {
                takeExp(abs(delta))
            } else if (delta > 0) {
                giveExp(delta)
            }
        }

    @Suppress("OverridingDeprecatedMember") // Ok for implementation
    override var exp: Double
        get() = (totalExp - converter.levelToExp(level)).coerceAtLeast(0.0)
        set(value) {
            val delta = value.coerceIn(0.0, totalExpToNextLevel) - exp
            if (delta < 0) {
                takeExp(abs(delta))
            } else if (delta > 0) {
                giveExp(delta)
            }
        }

    private val playerUniqueId: UUID
        get() = player.uniqueId

    override fun giveLevel(lvlAmount: Int) {
        battleLevelsApi.addLevel(playerUniqueId, lvlAmount)
    }

    override fun takeLevel(lvlAmount: Int) {
        battleLevelsApi.removeLevel(playerUniqueId, lvlAmount)
    }

    override fun giveExp(expAmount: Double) {
        val remainingExp = expToNextLevel
        val currentLevel = level

        if (expAmount >= remainingExp) {
            var levelsToGive = 0
            var extraExp = expAmount
            var expToNextLevel = remainingExp
            while (extraExp >= expToNextLevel) {
                extraExp -= expToNextLevel
                levelsToGive++
                expToNextLevel = converter.getExpToReachLevel(currentLevel + levelsToGive)
            }
            giveLevel(levelsToGive)
            if (extraExp > 0) battleLevelsApi.addScore(playerUniqueId, extraExp)
        } else {
            battleLevelsApi.addScore(playerUniqueId, expAmount)
        }
    }

    override fun takeExp(expAmount: Double) {
        val currentExp = exp
        val currentLevel = level

        if (expAmount > currentExp) {
            var levelsToTake = 0
            var extraExp = expAmount - currentExp
            while (extraExp > 0) {
                extraExp -= converter.getExpToReachLevel(currentLevel - levelsToTake)
                levelsToTake++
            }
            takeLevel(levelsToTake)
            if (extraExp < 0) battleLevelsApi.addScore(playerUniqueId, abs(extraExp))
        } else {
            battleLevelsApi.removeScore(playerUniqueId, expAmount)
        }
    }
}
