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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SystemPriorityTest {
    @Test
    public void testValueOf() {
        assertEquals(SubsystemPriority.HIGHEST, SubsystemPriority.fromString("HIGHEST"));
        assertEquals(SubsystemPriority.HIGHEST, SubsystemPriority.fromString("Highest"));
        assertEquals(SubsystemPriority.HIGH, SubsystemPriority.fromString("HIGH"));
        assertEquals(SubsystemPriority.HIGH, SubsystemPriority.fromString("High"));
        assertEquals(SubsystemPriority.NORMAL, SubsystemPriority.fromString("NORMAL"));
        assertEquals(SubsystemPriority.NORMAL, SubsystemPriority.fromString("Normal"));
        assertEquals(SubsystemPriority.LOW, SubsystemPriority.fromString("LOW"));
        assertEquals(SubsystemPriority.LOW, SubsystemPriority.fromString("Low"));
        assertEquals(SubsystemPriority.LOWEST, SubsystemPriority.fromString("LOWEST"));
        assertEquals(SubsystemPriority.LOWEST, SubsystemPriority.fromString("Lowest"));
    }
}
