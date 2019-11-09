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
import kotlin.test.assertFalse
import kotlin.test.fail

class SystemRegistryTest {

    private lateinit var registry: SystemRegistry

    @BeforeTest
    fun setUp() {
        registry = mock(defaultAnswer = MOCKS_ONLY_ABSTRACTS)
    }

    @Test
    fun testAddRightSubsystemByClass() {
        registry.registerSubsystem<BasicClassSystemImpl>()
        verify(registry).registerFactory(
            eq(ClassSystem.Factory::class.java),
            eq(BasicClassSystemImpl.FACTORY),
            any()
        )
    }

    @Test
    fun testAddRightSubsystemByInstance() {
        registry.registerSubsystem(CorrectLevelSystem.FACTORY)
        verify(registry).registerFactory(
            eq(Factory::class.java),
            eq(CorrectLevelSystem.FACTORY),
            any()
        )
    }

    @Test(expected = SystemNotRegisteredException::class)
    fun testAddWrongSubsystemByClass() {
        registry.registerSubsystem<WrongFactoryClassSystemImpl>()
        fail("Must be thrown exception")
    }

    @Test(expected = SystemNotRegisteredException::class)
    fun testAddWrongSubsystemBsyClass() {
        registry.registerSubsystem<LevelSystem>()
        fail("Must be thrown exception")
    }

    @Test
    fun testAddNotNeededSubsystem() {
        assertFalse(registry.registerSubsystem<WrongClassSystemImpl>())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGetSystemFactoryMustThrowException() {
        registry.getSystemFactory(WrongFactoryClassSystemImpl::class.java)
    }

    @Test
    fun testUnregisterSubsystem() {
        registry.unregisterSubsystem<CorrectLevelSystem>()
        verify(registry)
            .unregisterFactory(CorrectLevelSystem.FACTORY)
    }
}
