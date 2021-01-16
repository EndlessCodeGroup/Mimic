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

package ru.endlesscode.mimic.impl.skillapi

import com.sucy.skill.api.enums.ExpSource
import com.sucy.skill.api.player.PlayerClass
import org.bukkit.entity.Player
import ru.endlesscode.mimic.level.BukkitLevelSystem
import ru.endlesscode.mimic.level.ExpLevelConverter

/** Implementation of LevelSystem that uses SkillAPI. */
public class SkillApiLevelSystem internal constructor(
    player: Player,
    private val skillApi: SkillApiWrapper,
) : BukkitLevelSystem(player) {

    public companion object {
        public const val ID: String = "skillapi"
    }

    override val converter: ExpLevelConverter = SkillApiConverter.getInstance(skillApi)

    override var level: Int
        get() = playerClass?.level ?: 1
        set(value) {
            playerClass?.level = value
        }

    override var exp: Double
        get() = playerClass?.exp ?: 0.0
        set(value) {
            playerClass?.exp = value
        }

    private val playerClass: PlayerClass?
        get() = skillApi.getPlayerData(player).mainClass

    override fun takeLevels(lvlAmount: Int) {
        if (playerClass != null) {
            val newLevel = (level - lvlAmount).coerceAtLeast(1)
            val fractional = fractionalExp
            level = newLevel
            fractionalExp = fractional
        }
    }

    override fun giveLevels(lvlAmount: Int) {
        playerClass?.giveLevels(lvlAmount)
    }

    override fun takeExp(expAmount: Double) {
        val playerClass = playerClass
        if (playerClass != null) {
            val percent = expAmount / playerClass.requiredExp
            playerClass.loseExp(percent)
        }
    }

    override fun giveExp(expAmount: Double) {
        playerClass?.giveExp(expAmount, ExpSource.SPECIAL)
    }

    internal class Provider : BukkitLevelSystem.Provider(ID) {
        private val skillApi = SkillApiWrapper()

        override val isEnabled: Boolean
            get() = skillApi.isLoaded

        override fun getSystem(player: Player): BukkitLevelSystem = SkillApiLevelSystem(player, skillApi)
    }
}
