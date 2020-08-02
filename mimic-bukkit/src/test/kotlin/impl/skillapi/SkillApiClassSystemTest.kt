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

package ru.endlesscode.mimic.bukkit.impl.skillapi

import ru.endlesscode.mimic.ClassSystem
import kotlin.test.*

class SkillApiClassSystemTest : SkillApiTestBase() {

    private lateinit var classSystem: ClassSystem

    @BeforeTest
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        classSystem = SkillApiClassSystem(player, skillApi)
    }

    @Test
    fun `when get classes - should return right classes`() {
        // Given
        val expectedClasses = listOf("Mage", "Cleric")
        prepareClasses(expectedClasses)

        // When
        val actualClasses = classSystem.classes

        // Then
        assertEquals(expectedClasses, actualClasses)
    }

    @Test
    fun `when get classes - and there no classes - should return empty list`() {
        // Given
        prepareClasses()

        // When
        val actualClasses = classSystem.classes

        // Then
        assertTrue(actualClasses.isEmpty())
    }

    @Test
    fun `when get primary class - should return right class`() {
        // Given
        prepareClasses("Primary", "Secondary", "Third")

        // When
        val actualPrimaryClass = classSystem.primaryClass

        // Then
        assertEquals("Primary", actualPrimaryClass)
    }

    @Test
    fun `when get primary class - and there no classes - should return null`() {
        // Given
        prepareClasses()

        // When
        val actualPrimaryClass = classSystem.primaryClass

        // Then
        assertNull(actualPrimaryClass)
    }
}
