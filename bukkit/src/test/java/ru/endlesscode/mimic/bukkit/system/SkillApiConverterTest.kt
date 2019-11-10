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

package ru.endlesscode.mimic.bukkit.system

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.BeforeTest

class SkillApiConverterTest : SkillApiTestBase() {

    // SUT
    private lateinit var converter: SkillApiConverter

    @BeforeTest
    override fun setUp() {
        super.setUp()

        converter = SkillApiConverter.getInstance(skillApi)
    }

    @Test
    fun testExpToLevelWithZeroMustReturnOne() {
        val actual = converter.expToLevel(0.0)
        val expected = 1.0
        assertEquals(expected, actual, 0.0001)
    }

    @Test
    fun testExpToLevelWithNegativeMustReturnZero() {
        val actual = converter.expToLevel(-1.0)
        val expected = 0.0
        assertEquals(expected, actual, 0.0001)
    }

    @Test
    fun testExpToLevelMustReturnFullLevel() {
        val actual = converter.expToLevel(100.0)
        val expected = 5.0
        assertEquals(expected, actual, 0.0001)
    }

    @Test
    fun testExpToLevelMustReturnFractionalLevel() {
        val actual = converter.expToLevel(140.0)
        val expected = 5.8
        assertEquals(expected, actual, 0.0001)
    }

    @Test
    fun testLevelToExpMustReturnZero() {
        val actual = converter.levelToExp(1)
        val expected = 0
        assertEquals(expected.toDouble(), actual, 0.0001)
    }

    @Test
    fun testLevelToExpMustReturnRightValue() {
        val actual = converter.levelToExp(5)
        val expected = 100
        assertEquals(expected.toDouble(), actual, 0.0001)
    }

    @Test
    fun testGetExpToReachLevelMustReturnMinusOne() {
        val actual = converter.getExpToReachLevel(0)
        val expected = -1
        assertEquals(expected.toDouble(), actual, 0.0001)
    }
}
