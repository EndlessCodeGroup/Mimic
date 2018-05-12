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

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.enums.ExpSource;
import com.sucy.skill.api.player.PlayerClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import ru.endlesscode.mimic.api.system.LevelSystem;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class SkillApiLevelSystemTest extends SkillApiTestBase {
    private LevelSystem levelSystem;
    private PlayerClass playerClass;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        levelSystem = SkillApiLevelSystem.FACTORY.get(player);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDecreaseLevelMustBeUnsupported() {
        levelSystem.decreaseLevel(0);
    }

    @Test
    public void testIncreaseLevelMustCallPlayerClass() {
        prepareMainClass();

        int lvlAmount = 10;
        levelSystem.increaseLevel(lvlAmount);
        verify(playerClass).giveLevels(lvlAmount);
    }

    @Test
    public void testIncreaseLevelMustNotThrowNpe() {
        levelSystem.increaseLevel(1);
        // NPE wasn't thrown
    }

    @Test
    public void testGetLevelMustCallPlayerClass() {
        prepareMainClass();

        levelSystem.getLevel();
        //noinspection ResultOfMethodCallIgnored
        verify(playerClass).getLevel();
    }

    @Test
    public void testGetLevelMustReturnZero() {
        int actual = levelSystem.getLevel();
        assertEquals(0, actual);
    }

    @Test
    public void testSetLevelMustCallPlayerClass() {
        prepareMainClass();

        int newLevel = 10;
        levelSystem.setLevel(newLevel);
        verify(playerClass).setLevel(newLevel);
    }

    @Test
    public void testSetLevelMustNotThrowNpe() {
        levelSystem.setLevel(1);
        // NPE wasn't thrown
    }

    @Test
    public void testTakeExpMustCallPlayerClass() {
        prepareMainClass();
        when(playerClass.getRequiredExp()).thenReturn(10);

        int expAmount = 5;
        levelSystem.takeExp(expAmount);
        verify(playerClass).loseExp(0.5);
    }

    @Test
    public void testTakeExpMustNotThrowNpe() {
        levelSystem.takeExp(5);
        // NPE wasn't thrown
    }

    @Test
    public void testGiveExpMustCallPlayerClass() {
        prepareMainClass();
        int expAmount = 50;
        levelSystem.giveExp(expAmount);
        verify(playerClass).giveExp(expAmount, ExpSource.SPECIAL);
    }

    @Test
    public void testGiveExpMustNotThrowNpe() {
        levelSystem.giveExp(1);
        // NPE wasn't thrown
    }

    @Test
    public void testGetExpMustCallPlayerClass() {
        prepareMainClass();
        levelSystem.getExp();
        //noinspection ResultOfMethodCallIgnored
        verify(playerClass).getExp();
    }

    @Test
    public void testGetExpMustReturnZero() {
        int actual = levelSystem.getExp();
        assertEquals(0, actual);
    }

    @Test
    public void testSetExpMustCallPlayerClass() {
        prepareMainClass();
        int expAmount = 10;
        levelSystem.setExp(expAmount);
        verify(playerClass).setExp(expAmount);
    }

    @Test
    public void testSetExpMustNotThrowNpe() {
        levelSystem.setExp(1);
        // NPE wasn't thrown
    }

    @Test
    public void testGetExpToNextLevel() {
        prepareMainClass();
        when(playerClass.getExp()).thenReturn(5.0);
        when(playerClass.getRequiredExp()).thenReturn(10);

        int actual = levelSystem.getExpToNextLevel();
        int expected = 5;
        assertEquals(expected, actual);
    }

    @Test
    public void testGetExpToNextLevelMustReturnMinus() {
        int actual = levelSystem.getExpToNextLevel();
        assertEquals(-1, actual);
    }

    private void prepareMainClass() {
        prepareClasses("Primary");
        playerClass = data.getMainClass();
    }

    @Test
    public void testIsEnabledReturnsStatusOfSkillApi() {
        when(SkillAPI.isLoaded()).thenReturn(true).thenReturn(false);

        assertTrue(this.levelSystem.isEnabled());
        assertFalse(this.levelSystem.isEnabled());
    }

    @Test
    public void testGetNameAlwaysReturnSkillAPI() {
        assertEquals("SkillAPI", this.levelSystem.getName());
    }
}
