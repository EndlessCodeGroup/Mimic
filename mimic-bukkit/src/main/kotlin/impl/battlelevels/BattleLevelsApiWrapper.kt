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

package ru.endlesscode.mimic.impl.battlelevels

import me.robin.battlelevels.api.BattleLevelsAPI
import java.util.*

internal class BattleLevelsApiWrapper {
    fun getNeededFor(level: Int): Double = BattleLevelsAPI.getNeededFor(level)
    fun getLevel(uuid: UUID): Int = BattleLevelsAPI.getLevel(uuid)
    fun getScore(uuid: UUID): Double = BattleLevelsAPI.getScore(uuid)
    fun addLevel(uuid: UUID, amount: Int): Unit = BattleLevelsAPI.addLevel(uuid, amount)
    fun removeLevel(uuid: UUID, amount: Int): Unit = BattleLevelsAPI.removeLevel(uuid, amount)
    fun addScore(uuid: UUID, amount: Double): Unit = BattleLevelsAPI.addScore(uuid, amount, false)
    fun removeScore(uuid: UUID, amount: Double): Unit = BattleLevelsAPI.removeScore(uuid, amount)
}
