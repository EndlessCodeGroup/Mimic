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

package ru.endlesscode.mimic.bukkit.system.skillapi

import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class SkillApiConverterTest(
    private val exp: Double,
    private val level: Double
) : SkillApiTestBase() {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): List<Array<Double>> = listOf(
            arrayOf(0.0, 1.0),
            arrayOf(-1.0, 0.0),
            arrayOf(100.0, 5.0),
            arrayOf(140.0, 5.8)
        )
    }

    // SUT
    private lateinit var converter: SkillApiConverter

    @BeforeTest
    override fun setUp() {
        super.setUp()

        converter = SkillApiConverter.getInstance(skillApi)
    }

    @Test
    fun testExpToLevel() {
        // When
        val actual = converter.expToLevel(this.exp)

        // Then
        assertEquals(this.level, actual)
    }
}
