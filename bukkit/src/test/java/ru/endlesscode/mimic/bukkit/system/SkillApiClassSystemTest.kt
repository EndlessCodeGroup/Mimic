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

import com.nhaarman.mockitokotlin2.verify
import ru.endlesscode.mimic.api.system.ClassSystem
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SkillApiClassSystemTest : SkillApiTestBase() {

    private lateinit var classSystem: ClassSystem

    @BeforeTest
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        classSystem = SkillApiClassSystem(player, skillApi)
    }

    @Test
    fun testGetClassesMustReturnRightClasses() {
        val expectedClasses = arrayOf("Mage", "Cleric")
        prepareClasses(*expectedClasses)
        val actualClasses: List<String?> = classSystem.classes
        assertEquals(expectedClasses.toList(), actualClasses)
    }

    @Test
    fun testGetClassesMustReturnEmptyList() {
        prepareClasses()
        val actualClasses = classSystem.classes
        assertTrue(actualClasses.isEmpty())
    }

    @Test
    fun testGetPrimaryClassReturnsRightClass() {
        prepareClasses("Primary", "Secondary", "Third")
        val actualPrimaryClass = classSystem.primaryClass
        assertEquals("Primary", actualPrimaryClass)
    }

    @Test
    fun testGetPrimaryClassReturnsEmptyString() {
        prepareClasses()
        val actualPrimaryClass = classSystem.primaryClass
        assertEquals("", actualPrimaryClass)
    }

    @Test
    fun testIsEnabledReturnsStatusOfSkillApi() {
        classSystem.isEnabled

        verify(skillApi).isLoaded
    }

    @Test
    fun testGetNameAlwaysReturnSkillAPI() {
        assertEquals("SkillAPI", classSystem.name)
    }
}
