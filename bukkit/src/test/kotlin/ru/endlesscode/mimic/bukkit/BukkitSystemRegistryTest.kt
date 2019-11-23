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

package ru.endlesscode.mimic.bukkit

import ru.endlesscode.mimic.api.system.ClassSystem
import ru.endlesscode.mimic.api.system.LevelSystem
import ru.endlesscode.mimic.api.system.PlayerSystem
import ru.endlesscode.mimic.api.system.registry.getSystemFactory
import ru.endlesscode.mimic.api.system.registry.registerSubsystem
import ru.endlesscode.mimic.bukkit.system.vanilla.PermissionsClassSystem
import ru.endlesscode.mimic.bukkit.system.vanilla.VanillaLevelSystem
import kotlin.test.*

class BukkitSystemRegistryTest : BukkitTestBase() {

    // SUT
    private lateinit var systemRegistry: BukkitSystemRegistry

    @BeforeTest
    override fun setUp() {
        super.setUp()

        systemRegistry = BukkitSystemRegistry(plugin, server.servicesManager)
    }

    @Test
    fun `when register correct subsystem - should register it`() {
        // When
        val registered = systemRegistry.registerSubsystem(PermissionsClassSystem.FACTORY)

        // Then
        assertTrue(registered)
        assertNotNull(systemRegistry.getSystemFactory<ClassSystem>())
    }

    @Test
    fun `when no system registered - should return null for any system`() {
        // Then
        checkSystemNotExists<LevelSystem>()
    }

    @Test
    fun `when unregister all subsystems - should return null for all previously registered subsystems`() {
        // Given
        systemRegistry.registerSubsystem(VanillaLevelSystem.FACTORY)
        systemRegistry.registerSubsystem(PermissionsClassSystem.FACTORY)

        // When
        systemRegistry.unregisterAllSubsystems()

        // Then
        checkSystemNotExists<LevelSystem>()
        checkSystemNotExists<ClassSystem>()
    }

    @Test
    fun `when unregister factory - should unregister subsystem`() {
        // Given
        systemRegistry.registerSubsystem<PermissionsClassSystem>()

        // When
        systemRegistry.unregisterFactory(PermissionsClassSystem.FACTORY)

        // Then
        checkSystemNotExists<ClassSystem>()
    }

    @Test
    fun `when unregister not registered subsystem - should not throw exception`() {
        // When
        systemRegistry.unregisterFactory(PermissionsClassSystem.FACTORY)

        // Then
        checkSystemNotExists<ClassSystem>()
    }

    @AfterTest
    fun tearDown() {
        server.servicesManager.unregisterAll(plugin)
    }

    private inline fun <reified SystemT : PlayerSystem> checkSystemNotExists() {
        assertNull(systemRegistry.getSystemFactory<SystemT>())
    }
}
