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
import ru.endlesscode.mimic.api.system.PlayerSystem;
import ru.endlesscode.mimic.api.system.WrongClassSystemImpl;

import static org.junit.Assert.*;

public class SubsystemAdapterTest {
    private SubsystemMetaAdapter<BasicLevelSystemImpl> levelSystemMeta;
    private SubsystemMetaAdapter<BasicClassSystemImpl> classSystemMeta;
    private SubsystemMetaAdapter<WrongClassSystemImpl> wrongSystemMeta;

    @Before
    public void setUp() {
        levelSystemMeta = SubsystemMetaAdapter.getNotNullMeta(BasicLevelSystemImpl.class);
        classSystemMeta = SubsystemMetaAdapter.getNotNullMeta(BasicClassSystemImpl.class);
        wrongSystemMeta = SubsystemMetaAdapter.getNotNullMeta(WrongClassSystemImpl.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGettingMetaFromWrongClassMustThrowException() {
        SubsystemMetaAdapter.getNotNullMeta(PlayerSystem.class);
    }

    @Test
    public void testGettingMetaFromRightClassMustReturnResult() {
        assertNotNull(levelSystemMeta);
        assertNotNull(classSystemMeta);
        assertNotNull(wrongSystemMeta);
    }

    @Test
    public void testCheckingClassExistence() {
        assertTrue(levelSystemMeta.requiredClassesExists());
        assertTrue(classSystemMeta.requiredClassesExists());
        assertFalse(wrongSystemMeta.requiredClassesExists());
    }

    @Test
    public void testGettingPriority() {
        assertEquals(SubsystemPriority.LOWEST, levelSystemMeta.getPriority());
        assertEquals(SubsystemPriority.NORMAL, classSystemMeta.getPriority());
        assertEquals(SubsystemPriority.HIGH, wrongSystemMeta.getPriority());
    }
}
