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

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import ru.endlesscode.mimic.api.system.ClassSystem
import ru.endlesscode.mimic.api.system.LevelSystem
import ru.endlesscode.mimic.api.system.LevelSystem.Factory
import ru.endlesscode.mockito.MOCKS_ONLY_ABSTRACTS
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class SystemRegistryTest {

    // SUT
    private lateinit var registry: SystemRegistry

    @BeforeTest
    fun setUp() {
        registry = mock(defaultAnswer = MOCKS_ONLY_ABSTRACTS)
    }

    @Test
    fun `when register correct subsystem by class - should register right factory`() {
        // When
        registry.registerSubsystem<CorrectClassSystem>()

        // Then
        verify(registry).registerFactory(
            eq(ClassSystem.Factory::class.java),
            eq(CorrectClassSystem.FACTORY),
            any()
        )
    }

    @Test
    fun `when register correct subsystem by factory - should register the factory`() {
        // When
        registry.registerSubsystem(CorrectLevelSystem.FACTORY)

        // Then
        verify(registry).registerFactory(
            eq(Factory::class.java),
            eq(CorrectLevelSystem.FACTORY),
            any()
        )
    }

    @Test
    fun `when register system instead of subsystem - should throw an exception`() {
        assertFailsWith<SystemNotRegisteredException> {
            registry.registerSubsystem<LevelSystem>()
        }
    }

    @Test
    fun `when register not needed subsystem - should not register it`() {
        // When
        val registered = registry.registerSubsystem<NotNeededClassSystem>()

        // Then
        assertFalse(registered)
    }

    @Test
    fun `when get system factory - and pass wrong player system - should throw an exception`() {
        assertFailsWith<IllegalArgumentException> {
            registry.getSystemFactory(MissingFactoryPlayerSystem::class.java)
        }
    }

    @Test
    fun `when unregister subsystem - should unregister right factory`() {
        // When
        registry.unregisterSubsystem<CorrectLevelSystem>()

        // Then
        verify(registry).unregisterFactory(CorrectLevelSystem.FACTORY)
    }
}
