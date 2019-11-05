/*
 * This file is part of MimicAPI.
 * Copyright (C) 2018 Osip Fatkullin
 * Copyright (C) 2018 EndlessCode Group and contributors
 *
 * MimicAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MimicAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with MimicAPI.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.endlesscode.mimic.api.system.registry

import ru.endlesscode.mimic.api.system.ExpLevelConverter
import ru.endlesscode.mimic.api.system.LevelSystem
import ru.endlesscode.mimic.api.system.LevelSystem.Factory

/**
 * Dummy implementation of LevelSystem for tests.
 */
@Subsystem(priority = SubsystemPriority.LOWEST)
class BasicLevelSystemImpl : LevelSystem {

    companion object {
        private const val TAG = "Basic Level System"
        private const val EXP_IN_LEVEL = 10.0

        @JvmField
        val FACTORY = Factory(TAG) { BasicLevelSystemImpl() }
    }

    override val isEnabled = true
    override val name = TAG

    override val converter: ExpLevelConverter =
        Converter()

    override var level = 0
        set(newLevel) {
            field = newLevel.coerceAtLeast(0)
        }

    override var exp = 0.0
        set(value) {
            field = value.coerceIn(0.0, EXP_IN_LEVEL - 1)
        }

    override val totalExpToNextLevel: Double
        get() = converter.getExpToReachNextLevel(0) - exp

    /** Dummy implementation of ExpLevelConverter. Each level needs 10XP. */
    private class Converter : ExpLevelConverter {

        override fun levelToExp(level: Int): Double = level * EXP_IN_LEVEL

        override fun expToLevel(exp: Double): Double = exp / EXP_IN_LEVEL

        override fun getExpToReachLevel(level: Int): Double =
            EXP_IN_LEVEL
    }
}
