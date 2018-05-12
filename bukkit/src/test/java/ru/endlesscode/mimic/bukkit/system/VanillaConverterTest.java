/*
 * This file is part of BukkitMimic.
 * Copyright (C) 2017 EndlessCode Group and contributors
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
import org.junit.runners.*;

import java.util.*;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class VanillaConverterTest {
    private final int exp;
    private final int level;
    private final int expToNext;

    private VanillaConverter converter;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {0, 0, 7},
                {7, 1, 9},
                {315, 15, 37},
                {352, 16, 42},
                {1395, 30, 112},
                {1507, 31, 121},
                {2727, 39, 193}
        });
    }

    public VanillaConverterTest(int exp, int level, int expToNext) {
        this.exp = exp;
        this.level = level;
        this.expToNext = expToNext;
    }

    @Before
    public void setUp() {
        this.converter = VanillaConverter.getInstance();
    }

    @Test
    public void testExpToLevel() {
        assertEquals(this.level, this.converter.expToFullLevel(exp));
    }

    @Test
    public void testLevelToExp() {
        assertEquals(this.exp, this.converter.levelToExp(level));
    }

    @Test
    public void getExpToReachNextLevel() {
        assertEquals(this.expToNext, this.converter.getExpToReachNextLevel(level));
    }

    @Test
    public void getExpToReachNextLevelMustReturnMinusOne() {
        assertEquals(-1, this.converter.getExpToReachNextLevel(-1));
    }
}
