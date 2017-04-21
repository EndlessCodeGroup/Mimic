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

package ru.endlesscode.mimic.system.registry;

import org.junit.Test;
import ru.endlesscode.mimic.system.*;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * @author Osip Fatkullin
 * @since 1.0
 */
public class SystemRegistryTest {
    private SystemRegistry registry;

    @Test
    public void testAddRightSubsystemByClass() throws Exception {
        registry = new BasicSystemRegistryImpl();
        registry.registerSubsystem(BasicLevelSystemImpl.class);
        registry.registerSubsystem(BasicClassSystemImpl.class);

        assertNotNull("System must be registered", registry.getSystem(LevelSystem.class));
        assertNotNull("System must be registered", registry.getSystem(ClassSystem.class));
    }

    @Test(expected = SystemNotRegisteredException.class)
    public void testAddWrongSubsystemByClass() throws Exception {
        registry = new BasicSystemRegistryImpl();
        registry.registerSubsystem(WrongConstructorClassSystemImpl.class);

        fail("Must be thrown exception");
    }

    @Test
    public void testAddRightSubsystemByInstance() throws Exception {
        registry = new BasicSystemRegistryImpl();
        registry.registerSubsystem(new BasicLevelSystemImpl());
        registry.registerSubsystem(new BasicClassSystemImpl());

        assertNotNull("System must be registered", registry.getSystem(LevelSystem.class));
        assertNotNull("System must be registered", registry.getSystem(ClassSystem.class));
    }

    @Test(expected = SystemNotNeededException.class)
    public void testAddWrongSubsystemByInstance() throws Exception {
        registry = new BasicSystemRegistryImpl();
        registry.registerSubsystem(new WrongClassSystemImpl());

        fail("Must be thrown exception");
    }
}