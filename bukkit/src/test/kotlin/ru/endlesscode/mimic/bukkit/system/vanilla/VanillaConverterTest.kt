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

package ru.endlesscode.mimic.bukkit.system.vanilla

import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class VanillaConverterTest(
    private val exp: Int,
    private val level: Int,
    private val expToNext: Int
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): List<Array<Int>> {
            return listOf(
                arrayOf(0, 0, 7),
                arrayOf(7, 1, 9),
                arrayOf(315, 15, 37),
                arrayOf(352, 16, 42),
                arrayOf(1395, 30, 112),
                arrayOf(1507, 31, 121),
                arrayOf(2727, 39, 193)
            )
        }
    }

    // SUT
    private lateinit var converter: VanillaConverter

    @BeforeTest
    fun setUp() {
        converter = VanillaConverter.instance
    }

    @Test
    fun testExpToFullLevel() {
        // When
        val fullLevel = converter.expToFullLevel(exp.toDouble())

        // Then
        assertEquals(level, fullLevel)
    }

    @Test
    fun testLevelToExp() {
        // When
        val exp = converter.levelToExp(level)

        // Then
        assertEquals(this.exp.toDouble(), exp)
    }

    @Test
    fun testGetExpToReachNextLevel() {
        // When
        val expToNext = converter.getExpToReachNextLevel(level)

        // Then
        assertEquals(this.expToNext.toDouble(), expToNext)
    }
}
