/*
 * This file is part of MimicAPI.
 * Copyright (C) 2017 Osip Fatkullin
 * Copyright (C) 2017 EndlessCode Group and Contributors
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
import ru.endlesscode.mimic.api.system.LevelSystem;
import ru.endlesscode.mimic.api.system.WrongClassSystemImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Osip Fatkullin
 * @since 1.0
 */
public class SystemRegistryTest {
    private SystemRegistry registry;

    @Before
    public void setUp() throws Exception {
        this.registry = spy(new BasicSystemRegistryImpl());
    }

    @Test
    public void testAddRightSubsystemByClass() throws Exception {
        registry.registerSubsystem(BasicLevelSystemImpl.class);
        verify(registry).registerSystem(
                eq(BasicLevelSystemImpl.LevelSystemFactory.class),
                eq(BasicLevelSystemImpl.FACTORY),
                any(MetadataAdapter.class));

        registry.registerSubsystem(BasicClassSystemImpl.class);
        verify(registry).registerSystem(
                eq(BasicClassSystemImpl.ClassSystemFactory.class),
                eq(BasicClassSystemImpl.FACTORY),
                any(MetadataAdapter.class));
    }

    @Test
    public void testAddRightSubsystemByInstance() throws Exception {
        registry.registerSubsystem(BasicLevelSystemImpl.class, BasicLevelSystemImpl.FACTORY);
        registry.registerSubsystem(BasicClassSystemImpl.class, BasicClassSystemImpl.FACTORY);

        verify(registry, never()).getSubsystemFactory(any());
    }

    @Test(expected = SystemNotRegisteredException.class)
    public void testAddWrongSubsystemByClass() throws Exception {
        registry.registerSubsystem(WrongFactoryClassSystemImpl.class);

        fail("Must be thrown exception");
    }

    @Test(expected = SystemNotNeededException.class)
    public void testAddNotNeededSubsystem() throws Exception {
        registry.registerSubsystem(WrongClassSystemImpl.class);

        fail("Must be thrown exception");
    }

    @Test(expected = SystemNotFoundException.class)
    public void testGetSystemFactoryMustThrowException() throws Exception {
        registry.getSystemFactory(WrongFactoryClassSystemImpl.class);

        fail("Must throw exception!");
    }

    @Test
    public void testGetFactoryClassMustBeRight() throws Exception {
        Class<?> factoryClass = registry.getFactoryClass(BasicLevelSystemImpl.class);
        assertEquals(BasicLevelSystemImpl.LevelSystemFactory.class, factoryClass);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetFactoryClassWithoutInnerClasses() throws Exception {
        registry.getFactoryClass(LevelSystem.class);

        fail("Must throw exception!");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetFactoryClassWithWrongInnerClass() throws Exception {
        registry.getFactoryClass(WrongFactoryClassSystemImpl.class);

        fail("Must throw exception!");
    }
}