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

import ru.endlesscode.mimic.api.system.ExpLevelConverter

/** Converter for BattleLevels level system. */
class BattleLevelsConverter private constructor(
    private val battleLevelsApi: BattleLevelsApiWrapper
) : ExpLevelConverter {

    companion object {
        private var internalInstance: BattleLevelsConverter? = null

        @JvmStatic
        val instance: BattleLevelsConverter
            get() = getInstance()

        internal fun getInstance(battleLevelsApi: BattleLevelsApiWrapper? = null): BattleLevelsConverter {
            return internalInstance
                ?: BattleLevelsConverter(battleLevelsApi ?: BattleLevelsApiWrapper()).also { internalInstance = it }
        }
    }

    override fun getExpToReachLevel(level: Int): Double {
        return battleLevelsApi.getNeededFor(level) - battleLevelsApi.getNeededFor(level - 1)
    }
}
