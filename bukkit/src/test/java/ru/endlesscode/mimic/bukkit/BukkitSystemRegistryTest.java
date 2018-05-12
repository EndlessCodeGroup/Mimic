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

package ru.endlesscode.mimic.bukkit;

import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.junit.Before;
import org.junit.Test;
import ru.endlesscode.mimic.api.system.ClassSystem;
import ru.endlesscode.mimic.api.system.LevelSystem;
import ru.endlesscode.mimic.api.system.PlayerSystem;
import ru.endlesscode.mimic.api.system.SystemFactory;
import ru.endlesscode.mimic.api.system.registry.SystemNotFoundException;
import ru.endlesscode.mimic.api.system.registry.SystemPriority;
import ru.endlesscode.mimic.bukkit.system.PermissionsClassSystem;
import ru.endlesscode.mimic.bukkit.system.TestSystem;
import ru.endlesscode.mimic.bukkit.system.VanillaLevelSystem;

import static org.junit.Assert.*;

public class BukkitSystemRegistryTest extends BukkitTestBase {
    private BukkitSystemRegistry systemRegistry;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        ServicesManager servicesManager = this.server.getServicesManager();
        servicesManager.unregisterAll(plugin);

        this.systemRegistry = new BukkitSystemRegistry(plugin, servicesManager);
    }

    @Test
    public void testRegisterRightSubsystemMustBeSuccessful() throws Exception {
        this.systemRegistry.registerSubsystem(VanillaLevelSystem.class, VanillaLevelSystem.FACTORY);
        this.systemRegistry.registerSubsystem(PermissionsClassSystem.class, PermissionsClassSystem.FACTORY);

        SystemFactory<LevelSystem> levelSystemFactory = this.systemRegistry.getSystemFactory(LevelSystem.class);
        SystemFactory<ClassSystem> classSystemFactory = this.systemRegistry.getSystemFactory(ClassSystem.class);

        assertNotNull("System must be initialized", levelSystemFactory);
        assertNotNull("System must be initialized", classSystemFactory);
    }

    @Test(expected = SystemNotFoundException.class)
    public void testGetNotRegisteredSystemMustThrowException() throws Exception {
        this.systemRegistry.getSystemFactory(TestSystem.class);
    }

    @Test
    public void testGetServicePriorityFromSystem() {
        assertEquals(ServicePriority.Lowest, BukkitSystemRegistry.servicePriorityFromSystem(SystemPriority.LOWEST));
        assertEquals(ServicePriority.Low, BukkitSystemRegistry.servicePriorityFromSystem(SystemPriority.LOW));
        assertEquals(ServicePriority.Normal, BukkitSystemRegistry.servicePriorityFromSystem(SystemPriority.NORMAL));
        assertEquals(ServicePriority.High, BukkitSystemRegistry.servicePriorityFromSystem(SystemPriority.HIGH));
        assertEquals(ServicePriority.Highest, BukkitSystemRegistry.servicePriorityFromSystem(SystemPriority.HIGHEST));
    }

    @Test
    public void testUnregisterAllSubsystemsMustBeSuccessful() throws Exception {
        this.systemRegistry.registerSubsystem(VanillaLevelSystem.class, VanillaLevelSystem.FACTORY);
        this.systemRegistry.registerSubsystem(PermissionsClassSystem.class, PermissionsClassSystem.FACTORY);

        this.systemRegistry.unregisterAllSubsystems();

        checkSystemExistence(LevelSystem.class);
        checkSystemExistence(ClassSystem.class);
    }

    @Test
    public void testUnregisterExistingFactoryMustBeSuccessful() throws Exception {
        this.systemRegistry.registerSubsystem(PermissionsClassSystem.class);
        this.systemRegistry.unregisterFactory(PermissionsClassSystem.FACTORY);

        checkSystemExistence(ClassSystem.class);
    }

    @Test
    public void testUnregisterNotExistingFactoryMustBeSuccessful() {
        this.systemRegistry.unregisterFactory(PermissionsClassSystem.FACTORY);

        checkSystemExistence(ClassSystem.class);
    }

    private <SystemT extends PlayerSystem> void checkSystemExistence(Class<SystemT> systemClass) {
        try {
            this.systemRegistry.getSystemFactory(systemClass);
        } catch (SystemNotFoundException ignored) {
            return;
        }

        fail("System must be not registered");
    }
}
