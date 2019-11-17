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

import org.junit.Before;
import org.junit.Test;
import ru.endlesscode.mimic.bukkit.BukkitTestBase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VanillaLevelSystemTest extends BukkitTestBase {
    private BukkitLevelSystem levelSystem;

    @Before
    @Override
    public void setUp() {
        super.setUp();

        this.levelSystem = (VanillaLevelSystem) VanillaLevelSystem.FACTORY.get(this.player);
    }

    @Test
    public void testGetLevelMustReturnPlayerLevel() {
        int expectedLevel = 10;
        when(this.player.getLevel()).thenReturn(expectedLevel);

        assertEquals(expectedLevel, this.levelSystem.getLevel());
    }

    @Test
    public void testSetLevelMustCallVanillaMethod() {
        int newLevel = 10;
        this.levelSystem.setLevel(newLevel);

        verify(this.player).setLevel(newLevel);
    }

    @Test
    public void testSetWrongLevelMustCallVanillaMethodWithValidValue() {
        this.levelSystem.setLevel(-10);

        verify(this.player).setLevel(0);
    }

    @Test
    public void testGetExpMustReturnPlayerExp() {
        when(this.player.getLevel()).thenReturn(16);
        when(this.player.getExp()).thenReturn(.5f);

        assertEquals(21, this.levelSystem.getExp(), 0.0001);
    }

    @Test
    public void testSetExpMustCallVanillaMethodWithValidValue() {
        when(this.player.getLevel()).thenReturn(20);
        int expTo21Level = 62;

        this.levelSystem.setExp(expTo21Level / 2);
        verify(this.player).setExp(.5f);

        this.levelSystem.setExp(expTo21Level + 1);
        verify(this.player).setExp(1f);

        this.levelSystem.setExp(-1);
        verify(this.player).setExp(0f);
    }

    @Test
    public void testGetFractionalExpMustCallVanillaMethod() {
        when(this.player.getExp()).thenReturn(.4f);

        assertEquals(0.4, this.levelSystem.getFractionalExp(), 0.001);
    }

    @Test
    public void testSetFractionalExpMustCallVanillaMethod() {
        this.levelSystem.setFractionalExp(0.97);

        verify(this.player).setExp(0.97f);
    }

    @Test
    public void testSetWrongFractionalExpMustCallVanillaMethodWithRight() {
        this.levelSystem.setFractionalExp(1.2);
        verify(this.player).setExp(1);

        this.levelSystem.setFractionalExp(-1);
        verify(this.player).setExp(0);
    }

    @Test
    public void testGetExpToNextLevelMustCallVanillaMethod() {
        when(this.player.getExpToLevel()).thenReturn(7);

        assertEquals(7, this.levelSystem.getTotalExpToNextLevel(), 0.0001);
    }

    @Test
    public void testIsEnabledAlwaysTrue() {
        assertTrue(this.levelSystem.isEnabled());
    }

    @Test
    public void testGetNameAlwaysReturnVanillaLevelSystem() {
        assertEquals("Vanilla Level System", this.levelSystem.getName());
    }
}
