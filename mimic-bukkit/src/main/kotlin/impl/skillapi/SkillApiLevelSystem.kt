// Copyright (c) 2018 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

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

    override val converter: ExpLevelConverter = SkillApiConverter.getInstance(skillApi)

    override var level: Int
        get() = playerClass?.level ?: 1
        set(value) {
            playerClass?.level = value
        }

    @Suppress("OVERRIDE_DEPRECATION") // Ok for implementation
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

    internal class Provider : BukkitLevelSystem.Provider {
        private val skillApi = SkillApiWrapper()

        override val isEnabled: Boolean get() = skillApi.isLoaded
        override val id: String = ID

        override fun getSystem(player: Player): BukkitLevelSystem = SkillApiLevelSystem(player, skillApi)
    }

    public companion object {
        public const val ID: String = "skillapi"
    }
}
