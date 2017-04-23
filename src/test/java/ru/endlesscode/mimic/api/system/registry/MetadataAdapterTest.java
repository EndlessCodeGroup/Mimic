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
import ru.endlesscode.mimic.api.system.*;

import static org.junit.Assert.*;

/**
 * @author Osip Fatkullin
 * @since 1.0
 */
public class MetadataAdapterTest {
    private MetadataAdapter<BasicLevelSystemImpl> levelSystemMeta;
    private MetadataAdapter<BasicClassSystemImpl> classSystemMeta;
    private MetadataAdapter<WrongClassSystemImpl> wrongSystemMeta;

    @Before
    public void setUp() {
        levelSystemMeta = MetadataAdapter.getNotNullMeta(BasicLevelSystemImpl.class);
        classSystemMeta = MetadataAdapter.getNotNullMeta(BasicClassSystemImpl.class);
        wrongSystemMeta = MetadataAdapter.getNotNullMeta(WrongClassSystemImpl.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGettingMetaFromWrongClassMustThrowException() throws Exception {
        MetadataAdapter.getNotNullMeta(PlayerSystem.class);
    }

    @Test
    public void testGettingMetaFromRightClassMustReturnResult() throws Exception {
        assertNotNull(levelSystemMeta);
        assertNotNull(classSystemMeta);
        assertNotNull(wrongSystemMeta);
    }

    @Test
    public void testCheckingClassExistence() throws Exception {
        assertTrue(levelSystemMeta.requiredClassesExists());
        assertTrue(classSystemMeta.requiredClassesExists());
        assertFalse(wrongSystemMeta.requiredClassesExists());
    }

    @Test
    public void testGettingSystemClass() throws Exception {
        assertEquals(LevelSystem.class, levelSystemMeta.getSystemClass());
        assertEquals(ClassSystem.class, classSystemMeta.getSystemClass());
        assertEquals(ClassSystem.class, wrongSystemMeta.getSystemClass());
    }

    @Test
    public void testGettingSystemName() throws Exception {
        assertEquals("LevelSystem", levelSystemMeta.getSystemName());
        assertEquals("ClassSystem", classSystemMeta.getSystemName());
        assertEquals("ClassSystem", wrongSystemMeta.getSystemName());
    }

    @Test
    public void testGettingPriority() throws Exception {
        assertEquals(SystemPriority.LOWEST, levelSystemMeta.getPriority());
        assertEquals(SystemPriority.NORMAL, classSystemMeta.getPriority());
        assertEquals(SystemPriority.HIGH, wrongSystemMeta.getPriority());
    }
}