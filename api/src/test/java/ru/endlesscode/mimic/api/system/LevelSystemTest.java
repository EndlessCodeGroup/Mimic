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

package ru.endlesscode.mimic.api.system;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class LevelSystemTest {
    private LevelSystem ls;

    @Before
    public void setUp() {
        this.ls = new BasicLevelSystemImpl();
    }

    @Test
    public void testGettingConverter() {
        ExpLevelConverter converter = ls.getConverter();

        assertNotNull(converter);
        assertTrue(converter instanceof BasicConverterImp);
    }

    @Test
    public void testLevelGetSet() {
        this.ls.setLevel(10);
        assertEquals(10, ls.getLevel());

        this.ls.setLevel(-10);
        assertEquals(0, ls.getLevel());
    }

    @Test
    public void testExpGetSet() {
        this.ls.setExp(9);
        assertEquals(9, ls.getExp());

        this.ls.setExp(10);
        assertEquals(9, ls.getExp());

        this.ls.setExp(0);
        assertEquals(0, ls.getExp());
    }

    @Test
    public void testIncreaseLevel() {
        this.ls.setLevel(1);
        this.ls.increaseLevel(4);

        assertEquals(5, this.ls.getLevel());
    }

    @Test
    public void testDecreaseLevel() {
        this.ls.setLevel(10);

        this.ls.decreaseLevel(6);
        assertEquals(4, this.ls.getLevel());

        this.ls.decreaseLevel(6);
        assertEquals(0, this.ls.getLevel());
    }

    @Test
    public void testDidReachLevel() {
        this.ls.setLevel(10);

        assertTrue(this.ls.didReachLevel(0));
        assertTrue(this.ls.didReachLevel(10));
        assertFalse(this.ls.didReachLevel(11));
    }

    @Test
    public void testGetFractionalExp() {
        this.ls.setExp(-5);
        assertEquals(0., this.ls.getFractionalExp(), 0.001);

        this.ls.setExp(0);
        assertEquals(0., this.ls.getFractionalExp(), 0.001);

        this.ls.setExp(5);
        assertEquals(.5, this.ls.getFractionalExp(), 0.001);

        this.ls.setExp(9);
        assertEquals(.9, this.ls.getFractionalExp(), 0.001);

        this.ls.setExp(15);
        assertEquals(.9, this.ls.getFractionalExp(), 0.001);
    }

    @Test
    public void testSetFractionalExp() {
        this.ls.setFractionalExp(-1);
        assertEquals(0, this.ls.getExp());

        this.ls.setFractionalExp(0);
        assertEquals(0, this.ls.getExp());

        this.ls.setFractionalExp(0.5123);
        assertEquals(5, this.ls.getExp());

        this.ls.setFractionalExp(1);
        assertEquals(9, this.ls.getExp());

        this.ls.setFractionalExp(2);
        assertEquals(9, this.ls.getExp());
    }

    @Test
    public void testGetTotalExp() {
        this.ls.setLevel(0);
        this.ls.setExp(0);
        assertEquals(0, this.ls.getTotalExp());

        this.ls.setLevel(5);
        this.ls.setExp(0);
        assertEquals(50, this.ls.getTotalExp());

        this.ls.setLevel(0);
        this.ls.setExp(5);
        assertEquals(5, this.ls.getTotalExp());

        this.ls.setLevel(4);
        this.ls.setExp(9);
        assertEquals(49, this.ls.getTotalExp());
    }

    @Test
    public void testSetTotalExp() {
        this.ls.setTotalExp(-10);
        assertEquals(0, this.ls.getLevel());
        assertEquals(0, this.ls.getExp());

        this.ls.setTotalExp(0);
        assertEquals(0, this.ls.getLevel());
        assertEquals(0, this.ls.getExp());

        this.ls.setTotalExp(50);
        assertEquals(5, this.ls.getLevel());
        assertEquals(0, this.ls.getExp());

        this.ls.setTotalExp(5);
        assertEquals(0, this.ls.getLevel());
        assertEquals(5, this.ls.getExp());

        this.ls.setTotalExp(49);
        assertEquals(4, this.ls.getLevel());
        assertEquals(9, this.ls.getExp());
    }

    @Test
    public void testHasExp() {
        this.ls.setLevel(1);
        this.ls.setExp(5);

        assertTrue(this.ls.hasExp(15));
        assertTrue(this.ls.hasExp(0));
        assertFalse(this.ls.hasExp(16));
    }

    @Test
    public void testGiveExp() {
        this.ls.setLevel(10);
        this.ls.setExp(6);

        this.ls.giveExp(3);
        assertEquals(10, this.ls.getLevel());
        assertEquals(9, this.ls.getExp());

        this.ls.giveExp(1);
        assertEquals(11, this.ls.getLevel());
        assertEquals(0, this.ls.getExp());

        this.ls.giveExp(15);
        assertEquals(12, this.ls.getLevel());
        assertEquals(5, this.ls.getExp());
    }

    @Test
    public void testTakeExp() {
        this.ls.setLevel(10);
        this.ls.setExp(6);

        this.ls.takeExp(6);
        assertEquals(10, this.ls.getLevel());
        assertEquals(0, this.ls.getExp());

        this.ls.takeExp(1);
        assertEquals(9, this.ls.getLevel());
        assertEquals(9, this.ls.getExp());

        this.ls.takeExp(16);
        assertEquals(8, this.ls.getLevel());
        assertEquals(3, this.ls.getExp());

        this.ls.takeExp(100);
        assertEquals(0, this.ls.getLevel());
        assertEquals(0, this.ls.getExp());
    }
}
