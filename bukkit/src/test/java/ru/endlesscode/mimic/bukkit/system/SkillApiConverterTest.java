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

package ru.endlesscode.mimic.bukkit.system;

import org.junit.*;
import org.junit.runner.*;
import org.powermock.modules.junit4.*;

import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
public class SkillApiConverterTest extends SkillApiTestBase {
    private SkillApiConverter converter;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        this.converter = SkillApiConverter.getInstance();
    }

    @Test
    public void testExpToLevelWithZeroMustReturnOne() {
        double actual = converter.expToLevel(0);
        double expected = 1;
        assertEquals(expected, actual, 0.0001);
    }

    @Test
    public void testExpToLevelWithNegativeMustReturnZero() {
        double actual = converter.expToLevel(-1);
        double expected = 0;
        assertEquals(expected, actual, 0.0001);
    }

    @Test
    public void testExpToLevelMustReturnFullLevel() {
        double actual = converter.expToLevel(100);
        double expected = 5;
        assertEquals(expected, actual, 0.0001);
    }

    @Test
    public void testExpToLevelMustReturnFractionalLevel() {
        double actual = converter.expToLevel(120);
        double expected = 5.4;
        assertEquals(expected, actual, 0.0001);
    }

    @Test
    public void testLevelToExpMustReturnZero() {
        int actual = converter.levelToExp(1);
        int expected = 0;
        assertEquals(expected, actual);
    }

    @Test
    public void testLevelToExpMustReturnRightValue() {
        int actual = converter.levelToExp(5);
        int expected = 100;
        assertEquals(expected, actual);
    }

    @Test
    public void testGetExpToReachNextLevelMustReturnMinusOne() {
        int actual = converter.getExpToReachNextLevel(0);
        int expected = -1;
        assertEquals(expected, actual);
    }
}
