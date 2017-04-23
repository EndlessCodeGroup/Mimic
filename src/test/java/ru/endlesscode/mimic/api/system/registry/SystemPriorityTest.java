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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Osip Fatkullin
 * @since 1.0
 */
public class SystemPriorityTest {
    @Test
    public void testValueOf() throws Exception {
        assertEquals(SystemPriority.HIGHEST, SystemPriority.fromString("HIGHEST"));
        assertEquals(SystemPriority.HIGHEST, SystemPriority.fromString("Highest"));
        assertEquals(SystemPriority.HIGH, SystemPriority.fromString("HIGH"));
        assertEquals(SystemPriority.HIGH, SystemPriority.fromString("High"));
        assertEquals(SystemPriority.NORMAL, SystemPriority.fromString("NORMAL"));
        assertEquals(SystemPriority.NORMAL, SystemPriority.fromString("Normal"));
        assertEquals(SystemPriority.LOW, SystemPriority.fromString("LOW"));
        assertEquals(SystemPriority.LOW, SystemPriority.fromString("Low"));
        assertEquals(SystemPriority.LOWEST, SystemPriority.fromString("LOWEST"));
        assertEquals(SystemPriority.LOWEST, SystemPriority.fromString("Lowest"));
    }
}