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
package ru.endlesscode.mimic.bukkit.impl.vanilla

import org.bukkit.entity.Player
import ru.endlesscode.mimic.bukkit.BukkitLevelSystem
import ru.endlesscode.mimic.bukkit.impl.vanilla.VanillaConverter.Companion.instance

/** Vanilla experience bar system. */
class MinecraftLevelSystem private constructor(player: Player) : BukkitLevelSystem(instance, player) {

    companion object {
        const val ID: String = "minecraft"
    }

    override var level: Int
        get() = player.level
        set(value) {
            player.level = value.coerceAtLeast(0)
        }

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

    class Provider : BukkitLevelSystem.Provider(ID) {
        override fun getSystem(player: Player): BukkitLevelSystem = MinecraftLevelSystem(player)
    }
}
