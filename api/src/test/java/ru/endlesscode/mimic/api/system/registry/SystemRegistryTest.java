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

package ru.endlesscode.mimic.api.system.registry;

import org.junit.Before;
import org.junit.Test;
import ru.endlesscode.mimic.api.system.BasicClassSystemImpl;
import ru.endlesscode.mimic.api.system.BasicLevelSystemImpl;
import ru.endlesscode.mimic.api.system.ClassSystem;
import ru.endlesscode.mimic.api.system.LevelSystem;
import ru.endlesscode.mimic.api.system.PlayerSystem;
import ru.endlesscode.mimic.api.system.WrongClassSystemImpl;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class SystemRegistryTest {
    private SystemRegistry registry;

    @Before
    public void setUp() {
        this.registry = spy(new BasicSystemRegistryImpl());
    }

    @Test
    public void testAddRightSubsystemByClass() {
        registry.registerSubsystem(BasicLevelSystemImpl.class);
        verify(registry).registerSystem(
                eq(LevelSystem.Factory.class),
                eq(BasicLevelSystemImpl.FACTORY),
                any(SubsystemPriority.class));

        registry.registerSubsystem(BasicClassSystemImpl.class);
        verify(registry).registerSystem(
                eq(ClassSystem.Factory.class),
                eq(BasicClassSystemImpl.FACTORY),
                any(SubsystemPriority.class));
    }

    @Test
    public void testAddRightSubsystemByInstance() {
        registry.registerSubsystem(BasicLevelSystemImpl.class, BasicLevelSystemImpl.FACTORY);
        registry.registerSubsystem(BasicClassSystemImpl.class, BasicClassSystemImpl.FACTORY);

        verify(registry, never()).getSubsystemFactory(any());
    }

    @Test(expected = SystemNotRegisteredException.class)
    public void testAddWrongSubsystemByClass() {
        registry.registerSubsystem(WrongFactoryClassSystemImpl.class);

        fail("Must be thrown exception");
    }

    @Test
    public void testAddNotNeededSubsystem() {
        assertFalse(registry.registerSubsystem(WrongClassSystemImpl.class));
    }

    @Test
    public void testGetSystemFactoryMustBeRight() {
        registry.getSystemFactory(LevelSystem.class);
        verify(registry).getFactory(LevelSystem.Factory.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetSystemFactoryMustThrowException() {
        registry.getSystemFactory(WrongFactoryClassSystemImpl.class);
    }

    @Test
    public void testGetFactoryClassMustBeRight() {
        Class<?> factoryClass = registry.getFactoryClass(LevelSystem.class);
        assertEquals(LevelSystem.Factory.class, factoryClass);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetFactoryClassWithoutInnerClasses() {
        registry.getFactoryClass(PlayerSystem.class);

        fail("Must throw exception!");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetFactoryClassWithWrongInnerClass() {
        registry.getFactoryClass(WrongFactoryClassSystemImpl.class);

        fail("Must throw exception!");
    }

    @Test
    public void testUnregisterSubsystem() {
        registry.unregisterSubsystem(BasicLevelSystemImpl.class);

        verify(registry).unregisterFactory(BasicLevelSystemImpl.FACTORY);
    }
}
