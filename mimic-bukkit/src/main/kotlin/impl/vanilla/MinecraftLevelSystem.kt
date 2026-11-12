// Copyright (c) 2018 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.vanilla

import org.bukkit.entity.Player
import ru.endlesscode.mimic.level.BukkitLevelSystem
import ru.endlesscode.mimic.level.ExpLevelConverter

/** Vanilla experience bar system. */
public class MinecraftLevelSystem private constructor(player: Player) : BukkitLevelSystem(player) {

    override val converter: ExpLevelConverter
        get() = MinecraftExpLevelConverter.instance

    override var level: Int
        get() = player.level
        set(value) {
            player.level = value.coerceAtLeast(0)
        }

    @Suppress("OVERRIDE_DEPRECATION") // Ok for implementation
    override var exp: Double
        get() {
            val expToNextLevel = converter.getExpToReachNextLevel(level)
            return expToNextLevel * player.exp
        }
        set(value) {
            val expToNextLevel = converter.getExpToReachNextLevel(level)
            val allowedExperience = value.coerceIn(0.0, expToNextLevel)
            player.exp = (allowedExperience / expToNextLevel).toFloat()
        }

    override var fractionalExp: Double
        get() = player.exp.toDouble()
        set(value) {
            player.exp = value.coerceIn(0.0, 1.0).toFloat()
        }

    override val totalExpToNextLevel: Double
        get() = player.expToLevel.toDouble()

    internal class Provider : BukkitLevelSystem.Provider {
        override val id: String = ID
        override fun getSystem(player: Player): BukkitLevelSystem = MinecraftLevelSystem(player)
    }

    public companion object {
        public const val ID: String = "minecraft"
    }
}
