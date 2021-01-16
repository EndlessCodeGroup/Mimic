/*
 * This file is part of BukkitMimic.
 * Copyright (C) 2020 Osip Fatkullin
 * Copyright (C) 2020 EndlessCode Group and contributors
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
package ru.endlesscode.mimic.impl.vanilla

import ru.endlesscode.mimic.level.ExpLevelConverter
import kotlin.math.sqrt

/**
 * Converter for vanilla experience system.
 *
 * @see [Minecraft Wiki: Experience - Leveling Up](https://minecraft.gamepedia.com/Experience#Leveling_up)
 */
public class MinecraftExpLevelConverter private constructor() : ExpLevelConverter {

    public companion object {
        @JvmStatic
        public val instance: ExpLevelConverter by lazy { MinecraftExpLevelConverter() }
    }

    override fun expToLevel(exp: Double): Double = when {
        exp >= 1628 -> (sqrt(72 * exp - 54215) + 325) / 18
        exp >= 394 -> sqrt(40 * exp - 7839) / 10 + 8.1
        exp > 0 -> sqrt(exp + 9) - 3
        else -> 0.0
    }

    // Total experience =
    //      4.5 × level^2 – 162.5 × level + 2220 (at levels 32+)
    //      2.5 × level^2 – 40.5 × level + 360 (at levels 17–31)
    //      level^2 + 6 × level (at levels 0–16)
    override fun levelToExp(level: Int): Double = when {
        level >= 32 -> 4.5 * level * level - 162.5 * level + 2220
        level >= 17 -> 2.5 * level * level - 40.5 * level + 360
        level > 0 -> level * level + 6.0 * level
        else -> 0.0
    }

    override fun getExpToReachLevel(level: Int): Double = getExpToReachNextLevel(level - 1)

    // Experience required =
    //      9 × current_level – 158 (for levels 31+)
    //      5 × current_level – 38 (for levels 16–30)
    //      2 × current_level + 7 (for levels 0–15)
    override fun getExpToReachNextLevel(level: Int): Double = when {
        level >= 31 -> 9.0 * level - 158
        level >= 16 -> 5.0 * level - 38
        level >= 0 -> 2.0 * level + 7
        else -> -1.0
    }
}
