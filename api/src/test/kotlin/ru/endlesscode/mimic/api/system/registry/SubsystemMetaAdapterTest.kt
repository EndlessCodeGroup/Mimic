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

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import ru.endlesscode.mimic.api.system.MimicSystem
import kotlin.reflect.KClass

@RunWith(Parameterized::class)
class SubsystemMetaAdapterTest(
    private val subsystemClass: KClass<out MimicSystem>,
    private val requiredClassesExists: Boolean,
    private val priority: SubsystemPriority
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(CorrectLevelSystem::class, true, SubsystemPriority.LOWEST),
            arrayOf(CorrectClassSystem::class, true, SubsystemPriority.NORMAL),
            arrayOf(NotNeededClassSystem::class, false, SubsystemPriority.HIGH)
        )
    }

    // SUT
    private lateinit var adapter: SubsystemMetaAdapter<*>

    @Before
    fun setUp() {
        adapter = SubsystemMetaAdapter.fromClass(subsystemClass.java)
    }

    @Test
    fun `check required classes exists`() {
        assertEquals(requiredClassesExists, adapter.requiredClassesExists())
    }

    @Test
    fun `check priority`() {
        assertEquals(priority, adapter.priority)
    }
}
